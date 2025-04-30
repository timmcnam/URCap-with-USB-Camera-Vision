package com.ECET.CamApp.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class CamAppProgramNodeService implements SwingProgramNodeService<CamAppProgramNodeContribution, CamAppProgramNodeView>{

	@Override
	public String getId() {
		return "Camera App";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		
	}

	@Override
	public String getTitle(Locale locale) {
		return "Camera Feed";
	}

	@Override
	public CamAppProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new CamAppProgramNodeView();
	}

	@Override
	public CamAppProgramNodeContribution createNode(ProgramAPIProvider apiProvider, CamAppProgramNodeView view,
			DataModel model, CreationContext context) {
		return new CamAppProgramNodeContribution(apiProvider, view, model);
	}

}
