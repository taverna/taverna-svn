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
import static java.awt.event.ItemEvent.SELECTED;
import static net.sf.taverna.t2.component.ui.util.Utils.LONG_STRING;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class LicenseChooserPanel extends JPanel implements
		Observer<RegistryChoiceMessage> {
	private static final long serialVersionUID = 2175274929391537032L;
	private static final Logger logger = getLogger(LicenseChooserPanel.class);

	private JComboBox licenseBox = new JComboBox();

	private SortedMap<String, License> licenseMap = new TreeMap<String, License>();

	private Registry registry;

	public LicenseChooserPanel() {
		super();
		licenseBox.setPrototypeDisplayValue(LONG_STRING);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = NONE;
		this.add(new JLabel("License:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = BOTH;
		this.add(licenseBox, gbc);

		licenseBox.setEditable(false);
		licenseBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED)
					setLicense(licenseMap.get(licenseBox.getSelectedItem()));
			}
		});
	}

	protected void setLicense(License license) {
		if (license != null)
			licenseBox.setToolTipText("<html>" + license.getDescription()
					+ "</html>");
		else
			licenseBox.setToolTipText(null);
	}

	@Override
	public void notify(Observable<RegistryChoiceMessage> sender,
			RegistryChoiceMessage message) throws Exception {
		try {
			registry = message.getChosenRegistry();
			updateLicenseModel();
		} catch (Exception e) {
			logger.error("failure when handling license choice", e);
		}
	}

	private void updateLicenseModel() {
		licenseMap.clear();
		licenseBox.removeAllItems();
		licenseBox.setToolTipText(null);
		licenseBox.addItem("Reading licenses");
		licenseBox.setEnabled(false);
		new LicenseUpdater().execute();
	}

	public License getChosenLicense() {
		if (licenseBox.getSelectedIndex() < 0)
			return null;
		Object selectedItem = licenseBox.getSelectedItem();
		return licenseMap.get(selectedItem);
	}

	private class LicenseUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			List<License> licenses;
			if (registry == null)
				return null;
			try {
				licenses = registry.getLicenses();
				if (licenses == null)
					return null;
			} catch (RegistryException e) {
				logger.error("failure when reading licenses from registry", e);
				return null;
			} catch (NullPointerException e) {
				logger.error("unexpected exception when reading licenses", e);
				return null;
			}
			for (License license : licenses)
				try {
					String name = license.getName();
					licenseMap.put(name, license);
				} catch (NullPointerException e) {
					logger.error("could not get name of license", e);
				}
			return null;
		}

		@Override
		protected void done() {
			licenseBox.removeAllItems();
			for (String name : licenseMap.keySet())
				licenseBox.addItem(name);
			if (licenseMap.isEmpty()) {
				licenseBox.addItem("No licenses available");
				licenseBox.setEnabled(false);
				return;
			}

			String firstKey = licenseMap.firstKey();
			License preferredLicense = null;
			try {
				preferredLicense = registry.getPreferredLicense();
			} catch (RegistryException e) {
				logger.error("failed to get preferred license", e);
			}
			if (preferredLicense != null) {
				licenseBox.setSelectedItem(preferredLicense.getName());
				setLicense(preferredLicense);
			} else {
				licenseBox.setSelectedItem(firstKey);
				setLicense(licenseMap.get(firstKey));
			}
			licenseBox.setEnabled(true);
		}
	}
}
