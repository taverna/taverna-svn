/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.provenance.connector.configview;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * Supplies the view for any Derby specific configuration and handles the
 * setting of the jdbc url for the Derby provenance connector
 * 
 * @author Ian Dunlop
 * 
 */
public class DerbyConfigView extends ContextualView {

	public DerbyConfigView() {
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new DerbyConfigAction(this);
	}

	@Override
	public JComponent getMainFrame() {
		JTextArea label = new JTextArea();
		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File file = new File(applicationHomeDir, "db");
		label.setText("Provenance will be stored in: \n" + file.toString());
		label.setEditable(false);
		return label;
	}

	@Override
	public String getViewTitle() {
		// TODO Auto-generated method stub
		return "Derby provenance config view";
	}

	@Override
	public void refreshView() {
		// TODO Auto-generated method stub

	}

	public String getDBURL() {

		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "db");
		String jdbcString = "jdbc:derby:" + dbFile.toString()
				+ ";create=true;upgrade=true";
		return jdbcString;
	}

}
