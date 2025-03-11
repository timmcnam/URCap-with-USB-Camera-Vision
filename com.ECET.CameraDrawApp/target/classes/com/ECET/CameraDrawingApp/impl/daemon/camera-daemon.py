#!/usr/bin/env python

import sys
import os
import cv2
import numpy as np
import base64
import threading
import time
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler
from SocketServer import ThreadingMixIn  # Multithreading for XML-RPC

# ---- CAMERA CONFIG ----
FRAME_WIDTH = 1920
FRAME_HEIGHT = 1080
WHITEBOARD_WIDTH_INCH = 24.0
WHITEBOARD_HEIGHT_INCH = 18.0
MJPEG_PORT = 8080
XMLRPC_PORT = 40405

# ---- OPEN CAMERA ----
cap = cv2.VideoCapture(0)  # Use camera index 0 (change if necessary)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, FRAME_WIDTH)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, FRAME_HEIGHT)
cap.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc(*"MJPG"))

# ---- CAMERA SETTINGS ----
cap.set(cv2.CAP_PROP_AUTOFOCUS, 0)   # Disable AutoFocus (1 to enable)
cap.set(cv2.CAP_PROP_FOCUS, 50)      # Manual focus level (0 - 255)
cap.set(cv2.CAP_PROP_BRIGHTNESS, 100)
cap.set(cv2.CAP_PROP_CONTRAST, 50)
cap.set(cv2.CAP_PROP_SATURATION, 50)
cap.set(cv2.CAP_PROP_EXPOSURE, -5)
try:
    cap.set(cv2.CAP_PROP_WHITE_BALANCE_BLUE_U, 4000)
except Exception as e:
    sys.stderr.write("Warning: White balance setting is not supported.\n")

# ---- SHARED FRAME BUFFER ----
latest_frame = None
frame_lock = threading.Lock()

def capture_frames():
    """ Continuously capture frames and store them in a shared buffer. """
    global latest_frame
    while True:
        ret, frame = cap.read()
        if ret:
            with frame_lock:
                latest_frame = frame
        time.sleep(0.03)  # Approx. 30 FPS

frame_thread = threading.Thread(target=capture_frames)
frame_thread.setDaemon(True)
frame_thread.start()

# ---- MJPEG Streaming Server ----
class MJPEGStreamHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "multipart/x-mixed-replace; boundary=frame")
        self.end_headers()
        while True:
            with frame_lock:
                if latest_frame is None:
                    continue
                _, jpeg = cv2.imencode(".jpg", latest_frame)
                frame_bytes = jpeg.tostring()

            self.wfile.write("--frame\r\n")
            self.send_header("Content-Type", "image/jpeg")
            self.send_header("Content-Length", str(len(frame_bytes)))
            self.end_headers()
            self.wfile.write(frame_bytes)
            self.wfile.write("\r\n")
            time.sleep(0.1)

def start_mjpeg_server():
    """ Starts the MJPEG streaming server. """
    server = HTTPServer(("127.0.0.1", MJPEG_PORT), MJPEGStreamHandler)
    server.serve_forever()

mjpeg_thread = threading.Thread(target=start_mjpeg_server)
mjpeg_thread.setDaemon(True)
mjpeg_thread.start()

# ---- MULTITHREADED XML-RPC SERVER ----
class RequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = ("/RPC2",)

class MultithreadedSimpleXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
    pass

xmlrpc_server = MultithreadedSimpleXMLRPCServer(("127.0.0.1", XMLRPC_PORT), requestHandler=RequestHandler, allow_none=True)
xmlrpc_server.RequestHandlerClass.protocol_version = "HTTP/1.1"

# ---- OBJECT DETECTION CLASS ----
class ObjectDetector:
    def __init__(self):
        pass

    def process_frame(self):
        """Capture an image and process it for object detection."""
        with frame_lock:
            if latest_frame is None:
                return None, []
            frame = latest_frame.copy()

        # Convert to grayscale
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Apply Gaussian Blur to reduce noise
        blurred = cv2.GaussianBlur(gray, (5, 5), 0)

        # Apply Canny edge detection
        edges = cv2.Canny(blurred, 50, 150)

        # Find contours
        contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        objects = []
        for contour in contours:
            if cv2.contourArea(contour) > 500:  # Filter out small noise
                approx = cv2.approxPolyDP(contour, 0.02 * cv2.arcLength(contour, True), True)
                x, y, w, h = cv2.boundingRect(approx)

                # Store detected object coordinates
                x_in = (x / FRAME_WIDTH) * WHITEBOARD_WIDTH_INCH
                y_in = (y / FRAME_HEIGHT) * WHITEBOARD_HEIGHT_INCH
                w_in = (w / FRAME_WIDTH) * WHITEBOARD_WIDTH_INCH
                h_in = (h / FRAME_HEIGHT) * WHITEBOARD_HEIGHT_INCH

                objects.append((x_in, y_in, w_in, h_in))

                # Draw detected objects
                cv2.drawContours(frame, [approx], -1, (0, 255, 0), 2)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)

        return frame, objects

    def encode_base64(self, image):
        """Encode an image in base64 format for Java."""
        _, buffer = cv2.imencode('.jpg', image)
        return base64.b64encode(buffer.tostring())

    def detect_and_encode(self):
        """Detect objects, encode the image in base64, and return object positions."""
        frame, objects = self.process_frame()
        if frame is None:
            return None
        return self.encode_base64(frame), objects

# Instantiate the object detector
detector = ObjectDetector()

# ---- XML-RPC METHODS ----
def ping():
    """ Check if the daemon is running. """
    return True

def get_frame():
    """ Get the latest frame from the camera. """
    with frame_lock:
        if latest_frame is None:
            return None
        _, jpeg = cv2.imencode(".jpg", latest_frame)
        return xmlrpclib.Binary(jpeg.tostring())

def detect_shapes():
    """ Detect shapes and return base64-encoded image with shape coordinates. """
    base64_image, shapes = detector.detect_and_encode()
    return {"image": base64_image, "shapes": shapes}

# Register XML-RPC functions
xmlrpc_server.register_function(ping, "ping")
xmlrpc_server.register_function(get_frame, "get_frame")
xmlrpc_server.register_function(detect_shapes, "detect_shapes")

sys.stdout.write("Daemon started - MJPEG streaming on port {}\n".format(MJPEG_PORT))
sys.stdout.write("XML-RPC server running on port {}\n".format(XMLRPC_PORT))

xmlrpc_server.serve_forever()
