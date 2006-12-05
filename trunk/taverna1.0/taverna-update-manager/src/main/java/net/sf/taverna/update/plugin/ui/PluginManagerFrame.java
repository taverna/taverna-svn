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
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-05 12:24:28 $
 *               by   $Author: davidwithers $
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
 * 
 * @author David Withers
 */
public class PluginManagerFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jButton = null;

	private JButton jButton1 = null;

	private PluginManager pluginManager;

	private JScrollPane jScrollPane = null;

	private JList jList = null;

	private JButton jButton2 = null;

	private JButton jButton3 = null;

	private JButton jButton4 = null;

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
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints12.gridy = 3;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(5, 0, 0, 5);
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(5, 0, 0, 5);
			gridBagConstraints.gridy = 0;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.weighty = 1.0;
			gridBagConstraints11.gridwidth = 2;
			gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridheight = 3;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints2.gridy = 3;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 2;
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints1.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints1.insets = new Insets(5, 0, 0, 5);
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 2;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJButton(), gridBagConstraints1);
			jContentPane.add(getJButton1(), gridBagConstraints2);
			jContentPane.add(getJScrollPane(), gridBagConstraints11);
			jContentPane.add(getJButton2(), gridBagConstraints);
			jContentPane.add(getJButton3(), gridBagConstraints3);
			jContentPane.add(getJButton4(), gridBagConstraints12);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Update");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = getJList().getSelectedValue();
					if (selectedObject instanceof Plugin) {
						pluginManager.updatePlugin((Plugin) selectedObject);
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Find New Plugins");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					PluginSiteFrame pluginSiteFrame = new PluginSiteFrame(
							pluginManager);
					pluginSiteFrame.setLocationRelativeTo(PluginManagerFrame.this);
					pluginSiteFrame.setVisible(true);
				}
			});
		}
		return jButton1;
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
								getJButton2().setText("Disable");
								getJButton2().setActionCommand("disable");
							} else {
								getJButton2().setText("Enable");
								getJButton2().setActionCommand("enable");
							}
							getJButton2().setEnabled(true);
							if (pluginManager.isUpdateAvailable(plugin)) {
								getJButton().setEnabled(true);
							} else {
								getJButton().setEnabled(false);
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
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Enable");
			jButton2.setActionCommand("enable");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object selectedObject = jList.getSelectedValue();
					if (selectedObject instanceof Plugin) {
						Plugin plugin = (Plugin) selectedObject;
						if ("enable".equals(e.getActionCommand())) {
							plugin.setEnabled(true);
							jButton2.setText("Disable");
							jButton2.setActionCommand("disable");
						} else if ("disable".equals(e.getActionCommand())) {
							plugin.setEnabled(false);
							jButton2.setText("Enable");
							jButton2.setActionCommand("enable");
						}
					}
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Uninstall");
			jButton3.setEnabled(false);
		}
		return jButton3;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText("Find Updates");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (!pluginManager.checkForUpdates()) {
						JOptionPane.showMessageDialog(PluginManagerFrame.this, "No updates available");
					}
				}
			});
		}
		return jButton4;
	}

} // @jve:decl-index=0:visual-constraint="33,9"
