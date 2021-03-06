/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

package net.sf.taverna.t2.workbench.run;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Dialog to give user feedback about saving the cached reference service data.
 * 
 * @author David Withers
 */
public class ReferenceServiceShutdownDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JProgressBar progressBar;

	private JLabel remainingTime;

	private JButton abortButton;

	private JButton cancelButton;
	
	private boolean confirmShutdown = true;

	private int initialCacheSize;

	private long startTime = System.currentTimeMillis();

	public ReferenceServiceShutdownDialog() {
		super((Frame) null, "Saving Data", true);
		setUndecorated(true);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagLayout topPanelGridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		progressBar = new JProgressBar();
		remainingTime = new JLabel("Time remaining (estimating)");
		remainingTime.setFont(new Font("SansSerif", Font.PLAIN, 12));
		JLabel title = new JLabel("Taverna is waiting to shut down...");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14));

		abortButton = new JButton("Shutdown now");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane
						.showConfirmDialog(
								ReferenceServiceShutdownDialog.this,
								"If you close Taverna now you will lose some workflow data.\nAre you sure you want to close now?",
								"Confirm Shutdown", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					setVisible(false);
				}
			}
		});
		
		cancelButton = new JButton("Cancel shutdown");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmShutdown = false;
				setVisible(false);
			}
		});

		JLabel message = new JLabel(
				"Workflow data is being written to the database");

		JPanel topPanel = new JPanel(topPanelGridbag);
		topPanel.setBackground(Color.WHITE);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(20, 30, 0, 30);
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1d;
		c.weighty = 0d;
		c.gridwidth = 2;
		topPanelGridbag.setConstraints(title, c);
		topPanel.add(title);

		c.insets = new Insets(10, 30, 20, 30);
		topPanelGridbag.setConstraints(message, c);
		topPanel.add(message);

		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(topPanel, c);
		add(topPanel);

		c.insets = new Insets(30, 30, 0, 30);
		gridbag.setConstraints(progressBar, c);
		add(progressBar);

		c.insets = new Insets(10, 30, 0, 30);
		gridbag.setConstraints(remainingTime, c);
		add(remainingTime);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.insets = new Insets(10, 20, 10, 20);
		c.weightx = 0.5;
		c.weighty = 1d;
		c.gridx = 0;
		c.gridwidth = 1;
		gridbag.setConstraints(cancelButton, c);
		add(cancelButton);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.insets = new Insets(10, 20, 10, 20);
		c.weighty = 1d;
		c.weightx = 0.5;
		c.gridx = 1;
		gridbag.setConstraints(abortButton, c);
		add(abortButton);

		setSize(400, 230);
	}

	public void setInitialCacheSize(int initialCacheSize) {
		this.initialCacheSize = initialCacheSize;
		startTime = System.currentTimeMillis();
		progressBar.setMaximum(initialCacheSize);
	}

	public void setCurrentCacheSize(int currentCacheSize) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		int cacheElementsCleared = initialCacheSize - currentCacheSize;
		long timeLeft = (long) ((elapsedTime / (float) cacheElementsCleared) * currentCacheSize);
		progressBar.setValue(cacheElementsCleared);
		remainingTime.setText("Time remaining " + (timeLeft / 1000) + "s");
	}

	/**
	 * Returns <code>true</code> if it's OK to proceed with the shutdown.
	 * 
	 * @return <code>true</code> if it's OK to proceed with the shutdown
	 */
	public boolean confirmShutdown() {
		return confirmShutdown;
	}

}
