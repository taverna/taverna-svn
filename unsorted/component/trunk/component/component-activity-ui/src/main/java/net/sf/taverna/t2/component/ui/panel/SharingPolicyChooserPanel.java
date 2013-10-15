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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class SharingPolicyChooserPanel extends JPanel implements
		Observer<RegistryChoiceMessage> {
	private static final long serialVersionUID = 2175274929391537032L;
	private static final Logger logger = Logger
			.getLogger(SharingPolicyChooserPanel.class);

	private JComboBox permissionBox = new JComboBox();

	private SortedMap<String, SharingPolicy> permissionMap = new TreeMap<String, SharingPolicy>();

	private Registry registry;

	public SharingPolicyChooserPanel() {
		super();
		permissionBox.setPrototypeDisplayValue(Utils.LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Sharing policy:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		this.add(permissionBox, gbc);

		permissionBox.setEditable(false);
	}

	@Override
	public void notify(Observable<RegistryChoiceMessage> sender,
			RegistryChoiceMessage message) throws Exception {
		try {
			this.registry = message.getChosenRegistry();
			this.updateProfileModel();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void updateProfileModel() {
		permissionMap.clear();
		permissionBox.removeAllItems();
		permissionBox.addItem("Reading sharing policies");
		permissionBox.setEnabled(false);
		new SharingPolicyUpdater().execute();
	}

	public SharingPolicy getChosenPermission() {
		if (permissionBox.getSelectedIndex() < 0)
			return null;
		return permissionMap.get(permissionBox.getSelectedItem());
	}

	private class SharingPolicyUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			List<SharingPolicy> sharingPolicies;
			if (registry == null)
				return null;
			try {
				sharingPolicies = registry.getPermissions();
				if (sharingPolicies == null)
					return null;
			} catch (RegistryException e) {
				logger.error(e);
				return null;
			} catch (NullPointerException e) {
				logger.error(e);
				return null;
			}

			for (SharingPolicy p : sharingPolicies)
				try {
					permissionMap.put(p.getName(), p);
				} catch (NullPointerException e) {
					logger.error(e);
				}
			return null;
		}

		@Override
		protected void done() {
			permissionBox.removeAllItems();
			for (String name : permissionMap.keySet())
				permissionBox.addItem(name);
			if (!permissionMap.isEmpty()) {
				String firstKey = permissionMap.firstKey();
				permissionBox.setSelectedItem(firstKey);
				permissionBox.setEnabled(true);
			} else {
				permissionBox.addItem("No permissions available");
				permissionBox.setEnabled(false);
			}
		}
	}
}
