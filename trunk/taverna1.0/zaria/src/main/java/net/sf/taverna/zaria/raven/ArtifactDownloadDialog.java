package net.sf.taverna.zaria.raven;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

public class ArtifactDownloadDialog extends JDialog {

	private static Artifact artifact = null;
	private static ArtifactDownloadDialog dialog;
	
	public static Artifact showDialog(Component frameComp,
			Component locationComp,	String labelText, String title,
			String[] suggestedGroups, String[] suggestedVersions) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
		dialog = new ArtifactDownloadDialog(frame, locationComp, labelText, title, 
					suggestedGroups, suggestedVersions);
		dialog.setVisible(true);
		return artifact;
	}
	
	private ArtifactDownloadDialog(Frame frame, Component locationComp, String labelText, String title, String[] suggestedGroups, String[] suggestedVersions) {
		super(frame, title, true);
		
		// Content area
		JPanel sP = new JPanel();
		sP.setLayout(new BoxLayout(sP, BoxLayout.PAGE_AXIS));
		
		final JComboBox groupID = new JComboBox(suggestedGroups);
		groupID.setEditable(true);
		final JComboBox version = new JComboBox(suggestedVersions);
		version.setEditable(true);
		final JTextArea artifactID = new JTextArea("");
		sP.add(groupID);
		sP.add(artifactID);
		sP.add(version);
		
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("set".equals(e.getActionCommand()) &&
						artifactID.getText().equals("") == false &&
						groupID.getSelectedItem() != null &&
						version.getSelectedItem() != null) {
						artifact = new BasicArtifact((String)groupID.getSelectedItem(),
								artifactID.getText(), (String)version.getSelectedItem());
				}
				else {
					artifact = null;
				}
				ArtifactDownloadDialog.dialog.setVisible(false);
			}
		};
		
		// Buttons
		JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(listener);
        final JButton setButton = new JButton("Download");
        setButton.setActionCommand("set");
        setButton.addActionListener(listener);
        getRootPane().setDefaultButton(setButton);		
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPanel.add(setButton);
        
        // Show the thing
        getContentPane().add(new JLabel(labelText), BorderLayout.NORTH);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(sP, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(locationComp);
	}
	
	
}
