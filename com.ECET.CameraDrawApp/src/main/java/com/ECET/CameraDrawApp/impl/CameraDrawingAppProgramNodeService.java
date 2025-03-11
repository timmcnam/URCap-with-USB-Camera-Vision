package com.ECET.CameraDrawApp.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class CameraDrawingAppProgramNodeService implements SwingProgramNodeService<CameraDrawingAppProgramNodeContribution, CameraDrawingAppProgramNodeView>{

	private final XmlRpcCameraDrawingAppDaemonInterface daemonInterface;
	
	public CameraDrawingAppProgramNodeService(XmlRpcCameraDrawingAppDaemonInterface daemonInterface) {
		this.daemonInterface = daemonInterface;
	}
	@Override
	public String getId() {
		return "CameraDrawingAppProgramNode";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setUserInsertable(true);
		
	}

	@Override
	public String getTitle(Locale locale) {
		// TODO Auto-generated method stub
		return "Camera Drawing";
	}

	@Override
	public CameraDrawingAppProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new CameraDrawingAppProgramNodeView();
	}

	@Override
	public CameraDrawingAppProgramNodeContribution createNode(ProgramAPIProvider apiProvider,
			CameraDrawingAppProgramNodeView view, DataModel model, CreationContext context) {
		return new CameraDrawingAppProgramNodeContribution(apiProvider, view, model, daemonInterface);
	}

}
