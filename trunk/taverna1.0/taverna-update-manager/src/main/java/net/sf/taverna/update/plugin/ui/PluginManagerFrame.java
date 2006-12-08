/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: PluginManagerFrame.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-08 16:41:46 $
 *               by   $Author: sowen70 $
 * Created on 27 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.update.plugin.Plugin;
import net.sf.taverna.update.plugin.PluginManager;

/**
 * GUI component for the <code>PluginManager</code>.
 * 
 * @author David Withers
 */
public class PluginManagerFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton updateButton = null;

	private JButton findPluginsButton = null;

	private PluginManager pluginManager;

	private JScrollPane jScrollPane = null;

	private JList jList = null;

	private JButton enableButton = null;

	private JButton uninstallButton = null;

	private JButton findUpdatesButton = null;

	/**
	 * This is the default constructor
	 */
	public PluginManagerFrame(PluginManager pluginManager) {
		super();
		this.pluginManager = pluginManager;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(613, 444);
		this.setContentPane(getJContentPane());
		this.setTitle("Plugin Manager");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints findUpdatesConstraints = new GridBagConstraints();
			findUpdatesConstraints.gridx = 0;
			findUpdatesConstraints.insets = new Insets(5, 5, 5, 5);
			findUpdatesConstraints.gridy = 3;
			GridBagConstraints uninstallButtonConstraints = new GridBagConstraints();
			uninstallButtonConstraints.gridx = 2;
			uninstallButtonConstraints.anchor = GridBagConstraints.NORTHEAST;
			uninstallButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
			uninstallButtonConstraints.insets = new Insets(5, 0, 0, 5);
			uninstallButtonConstraints.gridy = 1;
			GridBagConstraints enableButtonConstraints = new GridBagConstraints();
			enableButtonConstraints.gridx = 2;
			enableButtonConstraints.anchor = GridBagConstraints.NORTHEAST;
			enableButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
			enableButtonConstraints.insets = new Insets(5, 0, 0, 5);
			enableButtonConstraints.gridy = 0;
			GridBagConstraints scrollPaneConstraints = new GridBagConstraints();
			scrollPaneConstraints.fill = GridBagConstraints.BOTH;
			scrollPaneConstraints.gridy = 0;
			scrollPaneConstraints.weightx = 1.0;
			scrollPaneConstraints.weighty = 1.0;
			scrollPaneConstraints.gridwidth = 2;
			scrollPaneConstraints.insets = new Insets(5, 5, 5, 5);
			scrollPaneConstraints.gridx = 0;
			scrollPaneConstraints.gridheight = 3;
			scrollPaneConstraints.anchor = GridBagConstraints.NORTHWEST;
			GridBagConstraints findPluginsConstraints = new GridBagConstraints();
			findPluginsConstraints.gridx = 1;
			findPluginsConstraints.anchor = GridBagConstraints.WEST;
			findPluginsConstraints.insets = new Insets(5, 5, 5, 5);
			findPluginsConstraints.gridy = 3;
			GridBagConstraints updateButtonConstraints = new GridBagConstraints();
			updateButtonConstraints.gridx = 2;
			updateButtonConstraints.gridwidth = 1;
			updateButtonConstraints.anchor = GridBagConstraints.NORTHEAST;
			updateButtonConstraints.insets = new Insets(5, 0, 0, 5);
			updateButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
			updateButtonConstraints.gridy = 2;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getUpdateButton(), updateButtonConstraints);
			jContentPane.add(getFindPluginsButton(), findPluginsConstraints);
			jContentPane.add(getJScrollPane(), scrollPaneConstraints);
			jContentPane.add(getEnableButton(), enableButtonConstraints);
			jContentPane.add(getUninstallButton(), uninstallButtonConstraints);
			jContentPane.add(getFindUpdatesButton(), findUpdatesConstraints);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getUpdateButton() {
		if (updateButton == null) {
			updateButton = new JButton();
			updateButton.setText("Update");
			updateButton.setEnabled(false);
			updateButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = getJList().getSelectedValue();
					if (selectedObject instanceof Plugin) {
						pluginManager.updatePlugin((Plugin) selectedObject);
					}
					jList.setSelectedValue(selectedObject, true);
				}
			});
		}
		return updateButton;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jList.setModel(new PluginListModel(pluginManager));
			jList.setCellRenderer(new PluginListCellRenderer(pluginManager));
			jList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						Object selectedObject = jList.getSelectedValue();
						if (selectedObject instanceof Plugin) {
							Plugin plugin = (Plugin) selectedObject;
							if (plugin.isEnabled()) {
								getEnableButton().setText("Disable");
								getEnableButton().setActionCommand("disable");
							} else {
								getEnableButton().setText("Enable");
								getEnableButton().setActionCommand("enable");
							}
							getEnableButton().setEnabled(true);
							if (pluginManager.isUpdateAvailable(plugin)) {
								getUpdateButton().setEnabled(true);
							} else {
								getUpdateButton().setEnabled(false);
							}
						}
					}
				}

			});
			if (jList.getComponentCount() > 0) {
				jList.setSelectedIndex(0);
			}
		}
		return jList;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getEnableButton() {
		if (enableButton == null) {
			enableButton = new JButton();
			enableButton.setText("Enable");
			enableButton.setEnabled(false);
			enableButton.setActionCommand("enable");
			enableButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = jList.getSelectedValue();
					if (selectedObject instanceof Plugin) {
						Plugin plugin = (Plugin) selectedObject;
						if ("enable".equals(e.getActionCommand())) {
							plugin.setEnabled(true);
							enableButton.setText("Disable");
							enableButton.setActionCommand("disable");
						} else if ("disable".equals(e.getActionCommand())) {
							plugin.setEnabled(false);
							enableButton.setText("Enable");
							enableButton.setActionCommand("enable");
						}
					}
					jList.setSelectedValue(selectedObject, true);
				}
			});
		}
		return enableButton;
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getUninstallButton() {
		if (uninstallButton == null) {
			uninstallButton = new JButton();
			uninstallButton.setText("Uninstall");
			uninstallButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = jList.getSelectedIndex();
					Object selectedObject = jList.getSelectedValue();
					if (selectedObject instanceof Plugin) {
						pluginManager.removePlugin((Plugin) selectedObject);
						pluginManager.savePlugins();
						}
					int listSize = jList.getModel().getSize();
					if (listSize > index) {
						jList.setSelectedIndex(index);
					} else {
						jList.setSelectedIndex(listSize - 1);
					}
				}
			});
		}
		return uninstallButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getFindPluginsButton() {
		if (findPluginsButton == null) {
			findPluginsButton = new JButton();
			findPluginsButton.setText("Find New Plugins");
			findPluginsButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = getJList().getSelectedValue();
					PluginSiteFrame pluginSiteFrame = new PluginSiteFrame(PluginManagerFrame.this);
					pluginSiteFrame.setLocationRelativeTo(PluginManagerFrame.this);
					pluginSiteFrame.setVisible(true);
					if (selectedObject != null) {
						jList.setSelectedValue(selectedObject, true);
					} else {
						jList.setSelectedIndex(0);
					}
				}
			});
		}
		return findPluginsButton;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFindUpdatesButton() {
		if (findUpdatesButton == null) {
			findUpdatesButton = new JButton();
			findUpdatesButton.setText("Find Updates");
			findUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = getJList().getSelectedValue();
					if (!pluginManager.checkForUpdates()) {
						JOptionPane.showMessageDialog(PluginManagerFrame.this, "No updates available");
					}
					if (selectedObject != null) {
						jList.setSelectedValue(selectedObject, true);
					} else {
						jList.setSelectedIndex(0);
					}
				}
			});
		}
		return findUpdatesButton;
	}

} // @jve:decl-index=0:visual-constraint="33,9"
