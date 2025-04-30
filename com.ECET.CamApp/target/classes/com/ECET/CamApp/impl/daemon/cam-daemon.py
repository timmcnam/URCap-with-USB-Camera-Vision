#!/usr/bin/env python

import sys
import cv2
import numpy as np
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn

# Global variables
image_path = "/tmp/captured.jpg"
last_detected_shapes = []

def isReachable():
	return True

# helper to brighten dark areas
def adjust_gamma(image, gamma=1.3):
	inv = 1.0 / gamma
	table = np.array([((i / 255.0) ** inv) * 255
                      	for i in np.arange(256)]).astype("uint8")
	return cv2.LUT(image, table)

def capture_image():
	cap = cv2.VideoCapture(0)
	if not cap.isOpened():
		return False

	# set manual exposure & resolution
	cap.set(cv2.CAP_PROP_FRAME_WIDTH,  640)
	cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
	cap.set(cv2.CAP_PROP_AUTO_EXPOSURE, 0.25)  
	cap.set(cv2.CAP_PROP_EXPOSURE, 0.1)   # tweak this until bright enough
	ret, frame = cap.read()
	cap.release()

	if not ret:
        	return False

	# boost midtones so dim frames pop
	frame = adjust_gamma(frame, gamma=1.3)

	# write out for Java/Swing to pick up
	cv2.imwrite(image_path, frame)
	return True



def detect_shape_type():
	try:
		img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
		if img is None:
			return ""

		# binarize
		_, thresh = cv2.threshold(img, 127, 255,
				          cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

		# findContours: handle both 2-tuple and 3-tuple returns
		cnt_data = cv2.findContours(thresh,
		                            cv2.RETR_EXTERNAL,
		                            cv2.CHAIN_APPROX_SIMPLE)
		if len(cnt_data) == 3:
			_, contours, _ = cnt_data
		else:
			contours, _ = cnt_data

		if not contours:
		    return ""

		cnt = max(contours, key=cv2.contourArea)
		peri   = cv2.arcLength(cnt, True)
		approx = cv2.approxPolyDP(cnt, 0.02 * peri, True)
		verts  = len(approx)

		if verts == 3:
			return "triangle"
		elif verts == 4:
			return "square"
		elif verts > 8:
			return "circle"
		else:
			return "polygon"
		
	except Exception as e:
		print("Daemon detect_shape_type() ERROR: {}".format(e))
		sys.stdout.flush()
		return ""



def get_image_path():
	return image_path

def is_camera_available():
	cap = cv2.VideoCapture(0)
	if cap.isOpened():
		cap.release()
		return True
	return False

# XML-RPC server 

sys.stdout.write("CamDaemon daemon started\n")
sys.stderr.write("CamDaemon daemon started\n")

class MultithreadedXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer):
	pass

server = MultithreadedXMLRPCServer(("127.0.0.1", 40405))
server.RequestHandlerClass.protocol_version = "HTTP/1.1"


# Register functions 
server.register_function(isReachable, "isReachable")
server.register_function(capture_image, "captureImage")
server.register_function(detect_shape_type, "detectShapeType")
server.register_function(get_image_path, "getImagePath")
server.register_function(is_camera_available, "isCameraAvailable")

server.serve_forever()

