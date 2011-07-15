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
package net.sf.taverna.t2.activities.rshell.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.rshell.RShellPortSymanticTypeBean;
import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.rshell.RshellConnectionSettings;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * A simple non-editable HTML table view over a {@link RshellActivity}.
 * 
 * @author Alex Nenadic
 * @author Ian Dunlop
 * 
 */
public class RshellActivityContextualView extends
		HTMLBasedActivityContextualView<RshellActivityConfigurationBean> {

	private static final long serialVersionUID = -2423232268033935502L;

	public RshellActivityContextualView(Activity<?> activity) {
		super(activity);
		init();
	}

	private void init() {

	}

	@Override
	protected String getRawTableRowsHtml() {
		RshellConnectionSettings connectionSettings = getConfigBean()
				.getConnectionSettings();

		String html = "";
		html = html + "<tr><th>Input Port Name</th>" + "<th>Semantic Type</th>"
				+ "</tr>";
		for (ActivityInputPortDefinitionBean bean : getConfigBean()
				.getInputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>";

			for (RShellPortSymanticTypeBean inputType : getConfigBean()
					.getInputSymanticTypes()) {
				if (bean.getName().equalsIgnoreCase(inputType.getName())) {
					html = html + inputType.getSymanticType().description + "</td></tr>";
					break;
				}
			}
		}
		html = html + "<tr><th>Output Port Name</th>"
				+ "<th>Semantic Type</th>" + "</tr>";
		for (ActivityOutputPortDefinitionBean bean : getConfigBean()
				.getOutputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>";
			for (RShellPortSymanticTypeBean outputType : getConfigBean()
					.getOutputSymanticTypes()) {
				if (bean.getName().equalsIgnoreCase(outputType.getName())) {
					html = html + outputType.getSymanticType().description + "</td></tr>";
					break;
				}
			}
		}
		if (connectionSettings != null) {		
			String username = connectionSettings.getUsername();
			if (username != null) {
				html = html + "<tr><th>Connection Settings</th></tr>"
				+ "<tr><td>User</td><td>" + username
				+ "</td></tr>";			
			}
			String password = connectionSettings.getPassword();
			if (password != null) {
				
				html = html + "<tr><td>Password</td><td>"
				+ password + "</td></tr>";
			}
			String host = connectionSettings.getHost();
			if (host != null) {
				
				html = html + "<tr><td>Host</td><td>" + host
				+ "</td></tr>";
				int port = connectionSettings.getPort();
				
				html = html + "<tr><td>Port</td><td>" + port
				+ "</td></tr>";
				boolean keepSessionAlive = connectionSettings.isKeepSessionAlive();
				html = html + "<tr><td>Keep Session Alive</td><td>"
				+ keepSessionAlive + "</td></tr>";
				
			}
		}
		return html;
	}

	@Override
	public String getViewTitle() {
		return "Rshell activity";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new RshellActivityConfigurationAction(
				(RshellActivity) getActivity(), owner);
		// return null;
	}

}
