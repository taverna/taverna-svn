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
 * Filename           $RCSfile: PluginListCellRenderer.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-11 15:33:31 $
 *               by   $Author: sowen70 $
 * Created on 28 Nov 2006
 *****************************************************************/
package net.sf.taverna.update.plugin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.AbstractBorder;

import net.sf.taverna.update.plugin.Plugin;
import net.sf.taverna.update.plugin.PluginManager;

/**
 * 
 * @author David Withers
 */
public class PluginListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private PluginManager pluginManager;

	private JLabel name = null;

	private JLabel description = null;

	private JLabel version = null;

	private JLabel status = null;

	/**
	 * This is the default constructor
	 */
	public PluginListCellRenderer(PluginManager pluginManager) {
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
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		gridBagConstraints15.gridx = 0;
		gridBagConstraints15.gridwidth = 2;
		gridBagConstraints15.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints15.insets = new Insets(3, 3, 3, 3);
		gridBagConstraints15.gridy = 2;
		status = new JLabel();
		status.setFont(getFont().deriveFont(Font.BOLD));
		status.setForeground(Color.BLUE);
		status.setText("status");
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.insets = new Insets(3, 8, 3, 3);
		gridBagConstraints7.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints7.fill = GridBagConstraints.NONE;
		gridBagConstraints7.gridy = 0;
		version = new JLabel();
		version.setFont(getFont().deriveFont(Font.PLAIN));
		version.setText("version");
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
		gridBagConstraints1.gridwidth = 2;
		gridBagConstraints1.gridy = 1;
		description = new JLabel();
		description.setFont(getFont().deriveFont(Font.PLAIN));
		description.setText("plugin description");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridy = 0;
		name = new JLabel();
		name.setFont(getFont().deriveFont(Font.BOLD));
		name.setText("plugin name");
		this.setSize(297, 97);
		this.setLayout(new GridBagLayout());
		this.setBorder(new AbstractBorder() {
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				Color oldColor = g.getColor();
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
				g.setColor(oldColor);
			}
		});
		this.add(name, gridBagConstraints);
		this.add(description, gridBagConstraints1);
		this.add(version, gridBagConstraints7);
		this.add(status, gridBagConstraints15);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if (value instanceof Plugin) {
			Plugin plugin = (Plugin) value;
			name.setText(plugin.getName());
			version.setText(plugin.getVersion());
			description.setText("<html>"+plugin.getDescription());
			if (pluginManager.isUpdateAvailable(plugin)) {
				status.setText("An update is available for this plugin");
			} else if (!plugin.isEnabled()) {
				status.setText("This plugin is disabled");
			} else {
				status.setText("");
			}
		}
		return this;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
