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

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static net.sf.taverna.t2.component.ui.util.Utils.LONG_STRING;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class SharingPolicyChooserPanel extends JPanel implements
		Observer<RegistryChoiceMessage> {
	private static final String SHARING_LABEL = "Sharing policy:";
	private static final String READING_MSG = "Reading sharing policies";
	private static final String NO_PERMISSIONS_MSG = "No permissions available";
	private static final long serialVersionUID = 2175274929391537032L;
	private static final Logger logger = getLogger(SharingPolicyChooserPanel.class);

	private final JComboBox permissionBox = new JComboBox();
	private final SortedMap<String, SharingPolicy> permissionMap = new TreeMap<String, SharingPolicy>();

	private Registry registry;

	public SharingPolicyChooserPanel() {
		super();
		permissionBox.setPrototypeDisplayValue(LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = NONE;
		this.add(new JLabel(SHARING_LABEL), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = BOTH;
		this.add(permissionBox, gbc);

		permissionBox.setEditable(false);
	}

	@Override
	public void notify(Observable<RegistryChoiceMessage> sender,
			RegistryChoiceMessage message) {
		try {
			registry = message.getChosenRegistry();
			updateProfileModel();
		} catch (Exception e) {
			logger.error("problem when handling notification of registry", e);
		}
	}

	private void updateProfileModel() {
		permissionMap.clear();
		permissionBox.removeAllItems();
		permissionBox.addItem(READING_MSG);
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
				logger.error("problem getting permissions", e);
				throw e;
			} catch (NullPointerException e) {
				logger.error("null pointer getting permissions", e);
				throw e;
			}

			for (SharingPolicy policy : sharingPolicies)
				try {
					permissionMap.put(policy.getName(), policy);
				} catch (NullPointerException e) {
					logger.error("problem getting name of policy", e);
				}
			return null;
		}

		@Override
		protected void done() {
			permissionBox.removeAllItems();
			try {
				get();
				for (String name : permissionMap.keySet())
					permissionBox.addItem(name);
				if (!permissionMap.isEmpty()) {
					String firstKey = permissionMap.firstKey();
					permissionBox.setSelectedItem(firstKey);
				} else {
					permissionBox.addItem(NO_PERMISSIONS_MSG);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e);
				permissionBox.addItem("Unable to read permissions");
			}
			permissionBox.setEnabled(!permissionMap.isEmpty());
		}
	}
}
