/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.run.cleanup;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

/**
 * Dialog that warns if there are running workflows while the workbench is being
 * shutdown and gives the user a change to cancel.
 * 
 * @author David Withers
 */
public class WorkflowRunStatusShutdownDialog extends HelpEnabledDialog {

	private static final long serialVersionUID = 1L;

	private JButton abortButton;

	private JButton cancelButton;

	private boolean confirmShutdown = true;

	public WorkflowRunStatusShutdownDialog(int runningWorkflows, int pausedWorkflows) {
		super((Frame) null, "Workflows still running", true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);

		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		JLabel title = new JLabel("Running or paused workflows detected.");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14));

		abortButton = new JButton("Shutdown now");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane
						.showConfirmDialog(
								WorkflowRunStatusShutdownDialog.this,
								"If you close Taverna now all workflows will be cancelled.\nAre you sure you want to close now?",
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

		StringBuilder sb = new StringBuilder();
		sb.append("There ");
		if (runningWorkflows > 0) {
			sb.append(runningWorkflows > 1 ? "are " : "is ");
			sb.append(runningWorkflows);
			sb.append(" running ");
			sb.append(runningWorkflows > 1 ? "workflows" : "workflow");
		} else {
			sb.append(pausedWorkflows > 1 ? "are " : "is ");
		}
		if (pausedWorkflows > 0) {
			if (runningWorkflows > 0) {
				sb.append(" and ");
			}
			sb.append(pausedWorkflows);
			sb.append(" paused ");
			sb.append(pausedWorkflows > 1 ? "workflows" : "workflow");
		}
		JLabel message = new JLabel(sb.toString());

		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBackground(Color.WHITE);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(20, 30, 20, 30);
		c.weightx = 1d;
		c.weighty = 0d;
		topPanel.add(title, c);

		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 0;
		add(topPanel, c);

		c.insets = new Insets(20, 20, 20, 20);
		c.weighty = 1d;
		c.weighty = 1d;
		add(message, c);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.insets = new Insets(10, 20, 10, 20);
		c.weightx = 0.5;
		c.weighty = 0d;
		c.gridx = 0;
		c.gridwidth = 1;
		add(cancelButton, c);

		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridx = 1;
		add(abortButton, c);

		setSize(400, 230);

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
