package com.ECET.CamApp.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class CamAppInstallationNodeService implements SwingInstallationNodeService<CamAppInstallationNodeContribution, CamAppInstallationNodeView>{

	private final CamDaemonService daemonService;
	
	public CamAppInstallationNodeService(CamDaemonService daemonService) {
		this.daemonService = daemonService;
	}
	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		
	}

	@Override
	public String getTitle(Locale locale) {
		return "Vision Camera";
	}

	@Override
	public CamAppInstallationNodeView createView(ViewAPIProvider apiProvider) {
		return new CamAppInstallationNodeView();
	}

	@Override
	public CamAppInstallationNodeContribution createInstallationNode(InstallationAPIProvider apiProvider,
			CamAppInstallationNodeView view, DataModel model, CreationContext context) {
		return new CamAppInstallationNodeContribution(apiProvider, view, daemonService, new CamXmlRpcInterface(), context, model);
	}

}
