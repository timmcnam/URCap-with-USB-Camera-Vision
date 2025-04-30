package com.ECET.CamApp.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.xmlrpc.XmlRpcException;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class CamAppProgramNodeContribution implements ProgramNodeContribution{
	
	private final ProgramAPIProvider apiProvider;
	private final CamAppProgramNodeView view;
	@SuppressWarnings("unused")
	private final DataModel model;
	private final CamXmlRpcInterface daemonStatusMonitor;
	@SuppressWarnings("unused")
	private String imagePath = "/tmp/captured.jpg"; // default fallback
	private Timer imageUpdateTimer;
	private String lastShape = "none";
	private static final long UPDATE_INTERVAL = 250; // quarter a second

	public CamAppProgramNodeContribution(ProgramAPIProvider apiProvider,
										 CamAppProgramNodeView view,
										 DataModel model) {
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.daemonStatusMonitor = getInstallation().getDaemonStatusMonitor();
	}
	
	@Override
	public void openView() {
		daemonStatusMonitor.startMonitorThread();
		
		startImageUpdateTimer();
		
	}
	
	

	private void startImageUpdateTimer() {
		imageUpdateTimer = new Timer(true);
		imageUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				updateUI();				
			}
		}, 0, UPDATE_INTERVAL);
		
	}
	
	private void stopImageUpdateTimer() {
		if (imageUpdateTimer != null) {
			imageUpdateTimer.cancel();
			imageUpdateTimer = null;
		}
		
	}

	private void updateUI() {
	    try {
	        // Snap a new frame
	        if (!daemonStatusMonitor.captureImage()) {
	            System.err.println("Daemon failed to capture image");
	            return;
	        }
	        // Load the latest image file and show it
	        String path = daemonStatusMonitor.getImagePath();
	        view.displayImage(path);

	    } catch (Exception e) {
	        System.err.println("Failed to update image: " + e.getMessage());
	    }
	}
	
	private void updateShape() {
		// Ask the daemon what shape it saw
		try {
			String shape = daemonStatusMonitor.detectShapeType();
			System.out.println("CamApp: detectShapeType() -> " + shape);
			lastShape = (shape == null || shape.isEmpty()) ? "none" : shape;
			// Update the label in your UI
			view.setShapeText(lastShape);
		} catch (XmlRpcException | RpcResponseException  ex) {
			System.err.println("CamApp: detectShapeType() RPC failed: " + ex);
			view.setShapeText("error");
		}
	}

	@Override
	public void closeView() {
		daemonStatusMonitor.stopMonitorThread();
		stopImageUpdateTimer();
		
	}
	
	public void onRefreshButtonClick() {
		updateUI();
        updateShape();
    }
	

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Camera Feed";
	}

	@Override
	public boolean isDefined() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// lastShape holds the camera’s decision
        writer.assign("shape", "\"" + lastShape + "\"");
        writer.appendLine("if shape == \"square\":");
        writer.appendLine("  DrawSquare()");   // Polyscope sub-program
        writer.appendLine("elif shape == \"circle\":");
        writer.appendLine("  DrawCircle()");
        writer.appendLine("elif shape == \"triangle\":");
        writer.appendLine("  DrawTriangle()");
        writer.appendLine("else:");
        writer.appendLine("  popup(\"No valid shape: \" + shape, \"Error\", blocking=True)");
        writer.appendLine("end");
		
	}
	
	private CamAppInstallationNodeContribution getInstallation() {
		return apiProvider.getProgramAPI().getInstallationNode(CamAppInstallationNodeContribution.class);
	}

}
