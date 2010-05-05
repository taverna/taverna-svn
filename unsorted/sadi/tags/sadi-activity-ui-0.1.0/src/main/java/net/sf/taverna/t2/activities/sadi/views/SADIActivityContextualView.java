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
package net.sf.taverna.t2.activities.sadi.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.actions.SADIActivityConfigurationAction;
import net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIActivityIcon;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class SADIActivityContextualView extends HTMLBasedActivityContextualView<SADIActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;

	public SADIActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<tr><td>");
		html.append("Service URI");
		html.append("</td><td>");
		html.append(getConfigBean().getServiceURI());
		html.append("</td></tr>");
		html.append("<tr><th>");
		html.append("Input Port Name");
		html.append("</th><th>");
		html.append("Port Depth");
		html.append("</th></tr>");
		for (Port port : getActivity().getInputPorts()) {
			html.append("<tr><td>");
			html.append(port.getName());
			html.append("</td><td>");
			html.append(port.getDepth());
			html.append("</td></tr>");
		}
		html.append("<tr><th>");
		html.append("Output Port Name");
		html.append("</th><th>");
		html.append("Port Depth");
		html.append("</th></tr>");
		for (Port port : getActivity().getOutputPorts()) {
			html.append("<tr><td>");
			html.append(port.getName());
			html.append("</td><td>");
			html.append(port.getDepth());
			html.append("</td></tr>");
		}
		return html.toString();
	}

	@Override
	public String getViewTitle() {
		return "SADI service";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new SADIActivityConfigurationAction((SADIActivity) getActivity(), owner);
	}

	@Override
	public String getBackgroundColour() {
		return SADIActivityIcon.COLOUR_HTML;
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
