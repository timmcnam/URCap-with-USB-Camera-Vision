package com.ECET.CameraDrawApp.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class CameraDrawingAppInstallationNodeService implements SwingInstallationNodeService<CameraDrawingAppInstallationNodeContribution, CameraDrawingAppInstallationNodeView>{

	private final CameraDaemonService daemonService;
    private final XmlRpcCameraDrawingAppDaemonInterface daemonInterface;
    
    public CameraDrawingAppInstallationNodeService(CameraDaemonService daemonService, XmlRpcCameraDrawingAppDaemonInterface daemonInterface) {
    	this.daemonService = daemonService;
        this.daemonInterface = daemonInterface;
	}
	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle(Locale locale) {
		return "Camera Daemon";
	}

	@Override
	public CameraDrawingAppInstallationNodeView createView(ViewAPIProvider apiProvider) {
		return new CameraDrawingAppInstallationNodeView();
	}

	@Override
	public CameraDrawingAppInstallationNodeContribution createInstallationNode(InstallationAPIProvider apiProvider,
			CameraDrawingAppInstallationNodeView view, DataModel model, CreationContext context) {
		return new CameraDrawingAppInstallationNodeContribution(apiProvider, view, model, daemonService, daemonInterface);
	}

}
