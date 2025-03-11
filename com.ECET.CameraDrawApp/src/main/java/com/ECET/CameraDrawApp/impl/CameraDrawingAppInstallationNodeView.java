package com.ECET.CameraDrawApp.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;

public class CameraDrawingAppInstallationNodeView implements SwingInstallationNodeView<CameraDrawingAppInstallationNodeContribution>{
	
	private JButton startCameraButton;
    private JButton stopCameraButton;
    private JButton autoFocusButton;
    private JButton increaseFocusButton;
    private JButton decreaseFocusButton;
    private JButton increaseExposureButton;
    private JButton decreaseExposureButton;
    private JLabel cameraStatusLabel;
    private JLabel videoLabel;
    
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 480;

	@Override
	public void buildUI(JPanel panel, CameraDrawingAppInstallationNodeContribution contribution) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        cameraStatusLabel = new JLabel("Status: Disconnected");
        cameraStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        startCameraButton = new JButton("Start Camera");
        stopCameraButton = new JButton("Stop Camera");
        autoFocusButton = new JButton("Toggle Auto Focus");
        increaseFocusButton = new JButton("Increase Focus");
        decreaseFocusButton = new JButton("Decrease Focus");
        increaseExposureButton = new JButton("Increase Exposure");
        decreaseExposureButton = new JButton("Decrease Exposure");

        videoLabel = new JLabel("No Video Available");
        videoLabel.setPreferredSize(new Dimension(VIDEO_WIDTH, VIDEO_HEIGHT));
        videoLabel.setMaximumSize(videoLabel.getPreferredSize());
        videoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        startCameraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.onStartCameraPressed();
            }
        });

        stopCameraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.onStopCameraPressed();
            }
        });

        autoFocusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.toggleAutoFocus();
            }
        });

        increaseFocusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.adjustFocus(1);
            }
        });

        decreaseFocusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.adjustFocus(-1);
            }
        });

        increaseExposureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.adjustExposure(1);
            }
        });

        decreaseExposureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contribution.adjustExposure(-1);
            }
        });

        // Layout Setup
        panel.add(cameraStatusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(videoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(startCameraButton);
        buttonPanel.add(stopCameraButton);
        buttonPanel.add(autoFocusButton);
        panel.add(buttonPanel);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(increaseFocusButton);
        controlPanel.add(decreaseFocusButton);
        controlPanel.add(increaseExposureButton);
        controlPanel.add(decreaseExposureButton);
        panel.add(controlPanel);
		
	}
	
	public void updateCameraStatus(String status) {
        cameraStatusLabel.setText(status);
    }

    public void updateVideoFeed(ImageIcon image) {
        videoLabel.setIcon(image);
    }

    public void setStartButtonEnabled(boolean enabled) {
        startCameraButton.setEnabled(enabled);
    }

    public void setStopButtonEnabled(boolean enabled) {
        stopCameraButton.setEnabled(enabled);
    }

}
