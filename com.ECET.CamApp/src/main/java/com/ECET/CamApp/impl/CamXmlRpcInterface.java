package com.ECET.CamApp.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class CamXmlRpcInterface {
	private static final int PORT = 40405;
	private static final String HOST_IP = "127.0.0.1";
	private static final XmlRpcClient XML_RPC_CLIENT = new XmlRpcClient();
	private static final XmlRpcClientConfigImpl XML_RPC_CLIENT_CONFIG = new XmlRpcClientConfigImpl();
	
	private final AtomicBoolean isDaemonReachable = new AtomicBoolean(false);
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> scheduledAtFixedRate;

	public CamXmlRpcInterface() {
		setupXmlRpcClient();
		startMonitorThread();
	}
	
	public static String getDaemonUrl() {
		return "http://" + HOST_IP + ":" + PORT + "/RPC2";
	}
	
	private static void setupXmlRpcClient() {
		try {
			XML_RPC_CLIENT_CONFIG.setEnabledForExceptions(true);
			XML_RPC_CLIENT_CONFIG.setServerURL(new URL(getDaemonUrl()));
			XML_RPC_CLIENT_CONFIG.setConnectionTimeout(1000); //1s
			XML_RPC_CLIENT.setConfig(XML_RPC_CLIENT_CONFIG);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void startMonitorThread() {
		Runnable containerMonitorRunnable = new Runnable() {
			
			@Override
			public void run() {
				isDaemonReachable.set(CamXmlRpcInterface.this.tryExecuteIsReachable());
			}
		};
		
		stopMonitorThread(); // stop if already running
		scheduledAtFixedRate = executorService.scheduleWithFixedDelay(containerMonitorRunnable, 0, 1, TimeUnit.SECONDS);
	}
	
	private boolean tryExecuteIsReachable() {
		try {
			return (Boolean) XML_RPC_CLIENT.execute("isReachable", new ArrayList<>());
		} catch (XmlRpcException ignored) {
			return false;
		}
	}
	
	public boolean isDaemonReachable() {
		return isDaemonReachable.get();
	}
	
	public void stopMonitorThread() {
		if (scheduledAtFixedRate != null) {
			scheduledAtFixedRate.cancel(true);
		}
	}
	
	//RPC methods
	
	public boolean captureImage() throws XmlRpcException, RpcResponseException {
		Object result = XML_RPC_CLIENT.execute("captureImage", new ArrayList<>());
		return parseBoolean(result);
	}
	
	public String getImagePath() throws XmlRpcException, RpcResponseException {
		Object result = XML_RPC_CLIENT.execute("getImagePath", new ArrayList<>());
		return parseString(result);
	}
		
	public String detectShapeType() throws XmlRpcException, RpcResponseException {
        Object result = XML_RPC_CLIENT.execute("detectShapeType", new ArrayList<>());
        return parseString(result);
    }
	
	public boolean isCameraAvailable() throws XmlRpcException, RpcResponseException {
		Object result = XML_RPC_CLIENT.execute("isCameraAvailable", new Object[]{});
		return parseBoolean(result);
	}
	
	
	//util methods
	
	private String parseString(Object response) throws RpcResponseException {
		if (response instanceof String) {
			return (String) response;
		}
		throw new RpcResponseException("Expected String but got: " + response);
	}
	
	private boolean parseBoolean(Object response) throws RpcResponseException {
		if (response instanceof Boolean) {
			return (Boolean) response;
		}
		throw new RpcResponseException("Expected Boolean but got: " + response);
	}
	
	
	
	
	
}
