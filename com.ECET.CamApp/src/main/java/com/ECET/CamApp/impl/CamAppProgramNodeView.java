package com.ECET.CamApp.impl;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;

public class CamAppProgramNodeView implements SwingProgramNodeView<CamAppProgramNodeContribution>{
	private JLabel imageLabel;
	private JLabel shapeLabel;

	@Override
	public void buildUI(JPanel panel, ContributionProvider<CamAppProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(640, 480));
        panel.add(imageLabel);
        
        shapeLabel = new JLabel("Detected shape: none");
        panel.add(shapeLabel);

        JButton updateBtn = new JButton("Refresh");
        
        updateBtn.setPreferredSize(new Dimension(150, 23));
        updateBtn.setMaximumSize(updateBtn.getPreferredSize());
        
        updateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                provider.get().onRefreshButtonClick();
            }
        });
        panel.add(updateBtn);
		
	}
	
	public void displayImage(String imagePath) {
        try {
            URL imageUrl = new File(imagePath).toURI().toURL();
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaled = icon.getImage().getScaledInstance(640, 480, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } catch (MalformedURLException e) {
            imageLabel.setText("Failed to load image.");
        }
    }
	
	public void setShapeText(String shape) {
	    shapeLabel.setText("Detected shape: " + shape);
	    shapeLabel.revalidate();
	    shapeLabel.repaint();
	}


}
