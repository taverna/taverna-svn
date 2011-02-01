/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sequencefile.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivity;
import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean;
import net.sf.taverna.t2.activities.sequencefile.actions.SequenceFileActivityConfigurationAction;
import net.sf.taverna.t2.activities.sequencefile.servicedescriptions.SequenceFileActivityIcon;
import net.sf.taverna.t2.activities.sequencefile.servicedescriptions.SequenceFileTemplateService;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Contextual view for SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileActivityContextualView extends
		HTMLBasedActivityContextualView<SequenceFileActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;

	public SequenceFileActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		// configuration
		html.append("<tr><th>");
		html.append("File Format");
		html.append("</th><th>");
		html.append("Sequence Type");
		html.append("</th></tr>");
		html.append("<tr><td>");
		html.append(getConfigBean().getFileFormat());
		html.append("</td><td>");
		html.append(getConfigBean().getSequenceType());
		html.append("</td></tr>");
		// input ports
		html.append("<tr><th>");
		html.append("Input Port Name");
		html.append("</th><th>");
		html.append("Depth");
		html.append("</th></tr>");
		for (Port port : getActivity().getInputPorts()) {
			html.append("<tr><td>");
			html.append(port.getName());
			html.append("</td><td>");
			html.append(port.getDepth());
			html.append("</td></tr>");
		}
		// output ports
		html.append("<tr><th>");
		html.append("Output Port Name");
		html.append("</th><th>");
		html.append("Depth");
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
		return SequenceFileTemplateService.SERVICE_NAME;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new SequenceFileActivityConfigurationAction((SequenceFileActivity) getActivity(),
				owner);
	}

	@Override
	public String getBackgroundColour() {
		return SequenceFileActivityIcon.COLOUR_HTML;
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
