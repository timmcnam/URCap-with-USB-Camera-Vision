package com.ECET.CameraDrawApp.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.ur.urcap.api.contribution.DaemonService;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;

/**
 * Hello world activator for the OSGi bundle URCAPS contribution
 *
 */
public class Activator implements BundleActivator {
	
	private CameraDaemonService daemonService;
	private CameraDrawingAppInstallationNodeService installationNodeService;
	private CameraDrawingAppProgramNodeService programNodeService;
	private XmlRpcCameraDrawingAppDaemonInterface daemonInterface;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Camera Registering");
		// Initialize the daemon service
		daemonService = new CameraDaemonService();
		
		// Initialize the XML-RPC interface for communication with the daemon
		daemonInterface = new XmlRpcCameraDrawingAppDaemonInterface();

		// Initialize installation and program node services
		installationNodeService = new CameraDrawingAppInstallationNodeService(daemonService, daemonInterface);
		programNodeService = new CameraDrawingAppProgramNodeService(daemonInterface);

		// Register services
		bundleContext.registerService(SwingInstallationNodeService.class, installationNodeService, null);
		bundleContext.registerService(SwingProgramNodeService.class, programNodeService, null);
		bundleContext.registerService(DaemonService.class, daemonService, null);
			
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		
	}
}

