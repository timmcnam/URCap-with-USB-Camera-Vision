package com.ECET.CameraDrawApp.impl;

import java.net.MalformedURLException;
import java.net.URL;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.DaemonService;

public class CameraDaemonService implements DaemonService{
	
	private DaemonContribution daemonContribution;

	@Override
	public void init(DaemonContribution daemon) {
		this.daemonContribution = daemon;
        try {
            // Ensure the daemon is installed correctly
            daemonContribution.installResource(new URL("file:com/ECET/CameraDrawingApp/impl/daemon/"));
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL in daemon service: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to install daemon resource: " + e.getMessage());
        }
		
	}

	@Override
	public URL getExecutable() {
		try {
            return new URL("file:com/ECET/CameraDrawingApp/impl/daemon/camera-daemon.py");
        } catch (MalformedURLException e) {
            System.err.println("Could not load the daemon executable: " + e.getMessage());
            return null;
        }
	}
	
	public DaemonContribution getDaemon() {
        return daemonContribution;
    }

}
