package com.ECET.CameraDrawApp.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class XmlRpcCameraDrawingAppDaemonInterface {
	static final String SERVER_URL = "http://127.0.0.1:40405/RPC2";
    private final XmlRpcClient client;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private boolean daemonReachable;

    public XmlRpcCameraDrawingAppDaemonInterface() throws Exception {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(SERVER_URL));
        client = new XmlRpcClient();
        client.setConfig(config);
        startDaemonMonitor();
    }

    private void startDaemonMonitor() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                daemonReachable = ping();
            } catch (Exception e) {
                daemonReachable = false;
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Checks if the daemon is reachable.
     * @return True if daemon is reachable, false otherwise.
     */
    public boolean isDaemonReachable() {
        return daemonReachable;
    }

    /**
     * Sends a ping request to the daemon.
     * @return True if daemon responds, false otherwise.
     */
    public boolean ping() {
        try {
            return (boolean) client.execute("ping", new Object[]{});
        } catch (XmlRpcException e) {
            return false;
        }
    }

    /**
     * Retrieves an image from the camera as a BufferedImage.
     * The image is received as a Base64-encoded string and converted back to BufferedImage.
     */
    public BufferedImage getCameraFrame() throws XmlRpcException {
        try {
            Object response = client.execute("GetImage", new ArrayList<>());
            if (response instanceof String) {
                byte[] imageBytes = Base64.decodeBase64((String) response);
                return ImageIO.read(new ByteArrayInputStream(imageBytes));
            } else {
                throw new XmlRpcException("Invalid response type from daemon. Expected Base64 string.");
            }
        } catch (Exception e) {
            throw new XmlRpcException("Error retrieving camera frame: " + e.getMessage());
        }
    }

    /**
     * Retrieves detected shapes from the camera feed.
     * The data is structured as a list of coordinate points.
     */
    public List<List<Double>> detectShapes() throws XmlRpcException {
        try {
            Object response = client.execute("detect_shapes", new ArrayList<>());
            if (response instanceof Object[]) {
                List<List<Double>> shapes = new ArrayList<>();
                for (Object item : (Object[]) response) {
                    if (item instanceof List) {
                        List<?> rawList = (List<?>) item;
                        List<Double> shape = new ArrayList<>();
                        for (Object point : rawList) {
                            if (point instanceof Double) {
                                shape.add((Double) point);
                            } else {
                                throw new XmlRpcException("Invalid data type in shape coordinates.");
                            }
                        }
                        shapes.add(shape);
                    } else {
                        throw new XmlRpcException("Invalid shape data structure.");
                    }
                }
                return shapes;
            } else {
                throw new XmlRpcException("Invalid response type from daemon.");
            }
        } catch (Exception e) {
            throw new XmlRpcException("Error retrieving detected shapes: " + e.getMessage());
        }
    }

    /**
     * Enables or disables auto-focus.
     * @param enable True to enable auto-focus, False to disable.
     */
    public void setAutoFocus(boolean enable) throws XmlRpcException {
        try {
            List<Object> params = new ArrayList<>();
            params.add(enable);
            client.execute("setEnableAutoFocus", params);
        } catch (Exception e) {
            throw new XmlRpcException("Error setting auto-focus: " + e.getMessage());
        }
    }

    /**
     * Sets the focus level of the camera.
     * @param value Focus level (0-100).
     */
    public void setFocusLevel(int value) throws XmlRpcException {
        try {
            List<Object> params = new ArrayList<>();
            params.add(value);
            client.execute("setFocusValue", params);
        } catch (Exception e) {
            throw new XmlRpcException("Error setting focus level: " + e.getMessage());
        }
    }

    /**
     * Sets the exposure level of the camera.
     * @param value Exposure level (0-100).
     */
    public void setExposureLevel(int value) throws XmlRpcException {
        try {
            List<Object> params = new ArrayList<>();
            params.add(value);
            client.execute("setExposureValue", params);
        } catch (Exception e) {
            throw new XmlRpcException("Error setting exposure level: " + e.getMessage());
        }
    }

    public void stopMonitor() {
        executorService.shutdown();
    }

}
