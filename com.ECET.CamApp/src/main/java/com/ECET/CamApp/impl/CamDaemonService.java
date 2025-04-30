package com.ECET.CamApp.impl;

import java.net.MalformedURLException;
import java.net.URL;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.DaemonService;

public class CamDaemonService implements DaemonService{
	
	private DaemonContribution daemonContribution;
	
	public CamDaemonService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(DaemonContribution daemonContribution) {
		this.daemonContribution = daemonContribution;
		try {
			daemonContribution.installResource(new URL("file:com/ECET/CamApp/impl/daemon/"));
		} catch (MalformedURLException e) {}
	}

	@Override
	public URL getExecutable() {
		try {
			return new URL("file:com/ECET/CamApp/impl/daemon/cam-daemon.py"); // Python executable
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public DaemonContribution getDaemon( ) {
		return daemonContribution;
	}

}
