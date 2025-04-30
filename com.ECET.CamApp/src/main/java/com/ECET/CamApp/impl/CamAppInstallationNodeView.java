package com.ECET.CamApp.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

public class CamAppInstallationNodeView implements SwingInstallationNodeView<CamAppInstallationNodeContribution>{
	
	boolean serverStarted = false;
	private JButton start;
	private JButton stop;
	private JButton check;
	private JLabel serverStatus;
	private JLabel cameraStatus;

	@Override
	public void buildUI(JPanel panel, CamAppInstallationNodeContribution contribution) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(info("To start the XML-RPC Server, press Start Server."));
		panel.add(createVerticalSpacer(20));
		
		panel.add(statusBox(contribution));
		panel.add(createVerticalSpacer(20));
		
		panel.add(xmlrpcServerCallBtns(contribution));
		panel.add(createVerticalSpacer(20));
		
		panel.add(info("To check the camera is working, press Check Cam"));
		
	}
	
	private Box info(String desc) {
		Box infoBox = Box.createHorizontalBox();
		infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel label = new JLabel(desc);
		
		infoBox.add(label);
		return infoBox;
	}
	
	private Component createHorizontalSpacer(int width) {
		return Box.createRigidArea(new Dimension(width, 0));
	}
	
	private Component createVerticalSpacer(int height) {
		return Box.createRigidArea(new Dimension(0, height));
	}
	
	private Box xmlrpcServerCallBtns(final CamAppInstallationNodeContribution contribution) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		start = new JButton("Start Server");
		stop = new JButton("Stop Server");
		check = new JButton("Check Camera");
		
		start.setPreferredSize(new Dimension(300, 50));
		start.setMaximumSize(start.getPreferredSize());
		
		stop.setPreferredSize(new Dimension(300, 50));
		stop.setMaximumSize(start.getPreferredSize());
		
		check.setPreferredSize(new Dimension(300, 50));
		check.setMaximumSize(check.getPreferredSize());
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				contribution.onStartCLick();
				
				
				
			}
		});
		
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				contribution.onStopCLick();
				
				
			}
		});
		
		check.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				contribution.onCheckClick();
				
				
			}
		});
		
		box.add(start);
		box.add(createHorizontalSpacer(2));
		box.add(stop);
		box.add(createHorizontalSpacer(2));
		box.add(check);
		
		return box;
	}
	
	private Box statusBox(final CamAppInstallationNodeContribution contribution) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		serverStatus = new JLabel("Server Status: ");
		cameraStatus = new JLabel("Camera Status: ");
		box.add(serverStatus);
		box.add(createHorizontalSpacer(10));
		box.add(cameraStatus);

		
		return box;
		
	}
	
	public void setServerStatusLabel(String text) {
		serverStatus.setText("Server Status: " + text);
	}

	public void setCameraStatusLabel(String text) {
		cameraStatus.setText("Camera Status: " + text);
	}

	
	public void setStartButtonEnabled (boolean enabled) {
		start.setEnabled(enabled);
	}
	
	public void setStopButtonEnabled (boolean enabled) {
		stop.setEnabled(enabled);
	}
	
	public void setCheckCamButtonEnabled (boolean enabled) {
		
	}
	

}
