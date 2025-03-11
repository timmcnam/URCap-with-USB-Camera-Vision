package com.ECET.CameraDrawApp.impl;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class CameraDrawingAppProgramNodeContribution implements ProgramNodeContribution{

	@SuppressWarnings("unused")
	private final ProgramAPIProvider apiProvider;
    private final CameraDrawingAppProgramNodeView view;
    @SuppressWarnings("unused")
	private final DataModel model;
    private final XmlRpcCameraDrawingAppDaemonInterface daemonInterface;
    
    private Timer uiTimer;
    
    public CameraDrawingAppProgramNodeContribution(ProgramAPIProvider apiProvider,
										           CameraDrawingAppProgramNodeView view,
										           DataModel model,
										           XmlRpcCameraDrawingAppDaemonInterface daemonInterface) {
    	this.apiProvider = apiProvider;
        this.view = view;
        this.model = model;
        this.daemonInterface = daemonInterface;
	}
	@Override
	public void openView() {
		if (uiTimer != null) {
            uiTimer.cancel();  // Ensure no existing timer is running
        }
        
        uiTimer = new Timer(true);
        uiTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(CameraDrawingAppProgramNodeContribution.this::updateUI);
            }
        }, 0, 1000); // Update every 1 second
		
	}

	@Override
	public void closeView() {
		if (uiTimer != null) {
            uiTimer.cancel();
            uiTimer = null;
        }
		
	}

	@Override
	public String getTitle() {
		 return "Camera Drawing";
	}

	@Override
	public boolean isDefined() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void updateUI() {
        try {
            BufferedImage frame = daemonInterface.getCameraFrame(); // Now returns BufferedImage
            if (frame != null) {
                SwingUtilities.invokeLater(() -> view.updateVideoFeed(frame));
            }
        } catch (Exception e) {
            System.err.println("Error fetching video frame: " + e.getMessage());
        }
    }

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.assign("cam", "rpc_factory(\"xmlrpc\", \"" + XmlRpcCameraDrawingAppDaemonInterface.SERVER_URL + "\")");

        writer.appendLine("if cam.ping():");
        writer.appendLine("  textmsg(\"Camera Daemon Connected\")");
        writer.appendLine("else:");
        writer.appendLine("  popup(\"Camera Daemon Unreachable\", \"Error\", False, True, False)");
        writer.appendLine("  halt");

        writer.appendLine("shapes = cam.detect_shapes()");
        writer.appendLine("if str_len(to_str(shapes)) == 0 or shapes == None:");
        writer.appendLine("  popup(\"No shapes detected!\", \"Error\", False, True, False)");
        writer.appendLine("  halt");

        writer.appendLine("board_origin = p[0.5, 0.2, 0.1, 0, 3.1415, 0]");
        writer.appendLine("approach_height = 0.05");
        writer.appendLine("drawing_speed = 0.1");
        writer.appendLine("move_speed = 0.25");

        writer.appendLine("shape_idx = 0");
        writer.appendLine("while shape_idx < length(shapes):");
        writer.appendLine("  shape = shapes[shape_idx]");
        writer.appendLine("  if length(shape) > 0:");
        writer.appendLine("    pt_idx = 0");
        writer.appendLine("    pt0 = shape[pt_idx]");
        writer.appendLine("    target0 = pose_trans(board_origin, p[pt0[0]*0.0254, pt0[1]*0.0254, 0, 0, 0, 0])");

        writer.appendLine("    movej(pose_trans(target0, p[0,0,approach_height,0,0,0]), a=1.2, v=move_speed)");
        writer.appendLine("    movel(target0, a=0.5, v=drawing_speed)");
        
        writer.appendLine("    pt_idx = pt_idx + 1");
        writer.appendLine("    while pt_idx < length(shape):");
        writer.appendLine("      pt = shape[pt_idx]");
        writer.appendLine("      target_pt = pose_trans(board_origin, p[pt[0]*0.0254, pt[1]*0.0254, 0, 0, 0, 0])");
        writer.appendLine("      movel(target_pt, a=0.5, v=drawing_speed)");
        writer.appendLine("      pt_idx = pt_idx + 1");
        writer.appendLine("    end");
        
        writer.appendLine("    movel(target0, a=0.5, v=drawing_speed)");
        writer.appendLine("    movej(pose_trans(target0, p[0,0,approach_height,0,0,0]), a=1.2, v=move_speed)");
        writer.appendLine("  end");
        
        writer.appendLine("  shape_idx = shape_idx + 1");
        writer.appendLine("end");

        writer.appendLine("popup(\"Shape drawing completed!\", \"Done\", False, False, False)");
		
	}

}
