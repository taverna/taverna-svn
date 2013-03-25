/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import uk.org.taverna.scufl2.api.activity.Activity;

/**
 *
 *
 * @author David Withers
 */
public abstract class MultiPageActivityConfigurationPanel extends ActivityConfigurationPanel {

	private JTabbedPane tabbedPane;

	/**
	 * Constructs a new <code>MultiPageActivityConfigurationPanel</code>.
	 * @param activity
	 */
	public MultiPageActivityConfigurationPanel(Activity activity) {
		super(activity);
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void addPage(String name, Component component) {
		tabbedPane.addTab(name, component);
	}

	public void removePage(String name) {
		tabbedPane.removeTabAt(tabbedPane.indexOfTab(name));
	}

	public void removeAllPages() {
		tabbedPane.removeAll();
	}

}
