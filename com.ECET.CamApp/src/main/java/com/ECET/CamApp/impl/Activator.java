package com.ECET.CamApp.impl;

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
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		CamDaemonService daemonService = new CamDaemonService();
		CamAppInstallationNodeService inststallationNodeService = new CamAppInstallationNodeService(daemonService);
		
		bundleContext.registerService(SwingInstallationNodeService.class, inststallationNodeService, null);
		bundleContext.registerService(SwingProgramNodeService.class, new CamAppProgramNodeService(), null);
		bundleContext.registerService(DaemonService.class, daemonService, null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		
	}
}

