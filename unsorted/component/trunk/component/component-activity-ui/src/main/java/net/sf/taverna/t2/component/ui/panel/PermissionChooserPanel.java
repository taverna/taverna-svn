/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentGroup;

/**
 *
 *
 * @author David Withers
 *
 */
public class PermissionChooserPanel extends JPanel {

	private static final String UPDATE = "View, Download and Update";
	private static final String DOWNLOAD = "View and Download";

	private final List<MyExperimentGroup> groups;
	private final List<JCheckBox> groupsSelections;
	private final ButtonGroup permissionButtons;
	private JRadioButton privatePermission, publicPermission;

	public PermissionChooserPanel(MyExperimentComponentRegistry registry) {
		this.setLayout(new GridBagLayout());
		groups = registry.getGroups();
		groupsSelections = new ArrayList<JCheckBox>();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		add(new JLabel("Sharing:"), gbc);

		permissionButtons = new ButtonGroup();

		privatePermission = new JRadioButton("Only I can view and download");
		permissionButtons.add(privatePermission);
		gbc.gridx = 1;
		add(privatePermission, gbc);

		publicPermission = new JRadioButton("Anyone can view and download");
		permissionButtons.add(publicPermission);
		gbc.gridx = 2;
		gbc.weightx = groups.isEmpty() ? 1 : 0;
		add(publicPermission, gbc);

		publicPermission.setSelected(true);

		if (!groups.isEmpty()) {
			final JPanel groupPermissionPanel = new JPanel(new GridBagLayout());

			gbc.gridx = 3;
			gbc.weightx = 1;
			JButton groupPermissionButton = new JButton("Set group sharing");
			groupPermissionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int answer = JOptionPane.showConfirmDialog(null, groupPermissionPanel,
							"Group Sharing Permissions", JOptionPane.OK_CANCEL_OPTION);
					if (answer != JOptionPane.OK_OPTION) {
						groupsSelections.clear();
					}

				}
			});
			add(groupPermissionButton, gbc);


			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.weightx = 1;

			for (final MyExperimentGroup group : groups) {
				JPanel groupPanel = new JPanel();
				JCheckBox groupSelection = new JCheckBox(group.toString());
				groupsSelections.add(groupSelection);
				groupPanel.add(groupSelection);
				final JComboBox editPermission = new JComboBox(new String[] {DOWNLOAD, UPDATE});
				if (group.hasEditPermission()) {
					editPermission.setSelectedItem(UPDATE);
				}
				editPermission.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						group.setEditPermission(editPermission.getSelectedItem().equals(UPDATE));
					}
				});
				groupPanel.add(editPermission);
				groupPermissionPanel.add(groupPanel, c);
			}
		}

	}

	public List<MyExperimentGroup> getSelectedGroups() {
		List<MyExperimentGroup> selectedGroups = new ArrayList<MyExperimentGroup>();
		for (int i = 0; i < groupsSelections.size(); i++) {
			if (groupsSelections.get(i).isSelected()) {
				selectedGroups.add(groups.get(i));
			}
		}
		return selectedGroups;
	}

}
