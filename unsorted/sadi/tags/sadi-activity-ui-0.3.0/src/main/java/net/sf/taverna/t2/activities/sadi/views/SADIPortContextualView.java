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
package net.sf.taverna.t2.activities.sadi.views;

import javax.swing.JComponent;

import net.sf.taverna.t2.activities.sadi.SADIActivityInputPort;
import net.sf.taverna.t2.activities.sadi.SADIActivityPort;
import net.sf.taverna.t2.activities.sadi.views.SADIHtmlPanel.Table;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * A contextual view for {@link SADIActivityPort}s.
 *
 * @author David Withers
 */
public class SADIPortContextualView extends ContextualView {

	private static final long serialVersionUID = 1L;
	
	private final SADIActivityPort activityPort;
	private SADIHtmlPanel mainFrame;

	/**
	 * Constructs a new SADIPortContextualView.
	 * 
	 * @param outputPort
	 */
	public SADIPortContextualView(SADIActivityPort activityPort) {
		this.activityPort = activityPort;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		if (mainFrame == null) {
			mainFrame = new SADIHtmlPanel();
			refreshView();
		}
		return mainFrame;
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public String getViewTitle() {
		if (activityPort instanceof SADIActivityInputPort) {
			return "SADI input : " + activityPort.getName();
		} else {
			return "SADI output : " + activityPort.getName();
		}
	}

	@Override
	public void refreshView() {
		mainFrame.clearTables();
		buildHtmlTables();
		mainFrame.update();
	}

	private void buildHtmlTables() {
		Table table = mainFrame.createTable();
		if (activityPort.getOntProperty() != null) {
			table.addProperty("Property", activityPort.getOntProperty().getURI(), "even");
		}
		table.addProperty("Type", activityPort.getOntClass().getURI(), "even");
		table.addProperty("Depth", String.valueOf(activityPort.getDepth()), "even");
	}

}
