package com.ECET.CameraDrawApp.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

public class CameraDrawingAppProgramNodeView implements SwingProgramNodeView<CameraDrawingAppProgramNodeContribution>{
	
	private JLabel videoLabel;
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 480;


	@Override
	public void buildUI(JPanel panel, ContributionProvider<CameraDrawingAppProgramNodeContribution> provider) {
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        videoLabel = new JLabel("No Video Feed");
        videoLabel.setPreferredSize(new Dimension(VIDEO_WIDTH, VIDEO_HEIGHT));
        videoLabel.setMaximumSize(videoLabel.getPreferredSize());
        videoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(videoLabel);
    }

    /**
     * Updates the video feed with a BufferedImage.
     * @param image The BufferedImage to display.
     */
    public void updateVideoFeed(BufferedImage image) {
        if (image != null) {
            videoLabel.setIcon(new ImageIcon(image));
        } else {
            videoLabel.setText("No Video Available");
        }
    }
}
