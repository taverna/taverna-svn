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
package net.sf.taverna.t2.activities.apiconsumer.views;

import java.awt.Frame;

import javax.help.CSH;
import javax.swing.Action;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigurationBean;
import net.sf.taverna.t2.activities.apiconsumer.actions.ApiConsumerActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A simple non editable HTML table view over a {@link ApiConsumerActivity}.
 * Clicking on the 'Configure' button shows the editable
 * {@link ApiConsumerConfigView}
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class ApiConsumerContextualView extends
		HTMLBasedActivityContextualView<ApiConsumerActivityConfigurationBean> {

	public ApiConsumerContextualView(Activity<?> activity) {
		super(activity);
		init();
	}

	private void init() {
		CSH
		.setHelpIDString(
				this,
		"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ApiConsumerContextualView");
	}

	@Override
	protected String getRawTableRowsHtml() {
		
		ApiConsumerActivityConfigurationBean bean = getConfigBean();
		String html = "";
		
		html += "<tr><td>Class name</td>"
		+ "<td>" + bean.getClassName() + "</td></tr>";
		
		html += "<tr><td>Method name</td>"
		+ "<td>" + bean.getMethodName() + "</td></tr>";
		
		html += "<tr><td>Method description</td>"
		+ "<td>" + bean.getDescription() + "</td></tr>";
		
		html += "<tr><td>Is method constructor?</td>"
		+ "<td>" + bean.isMethodConstructor() + "</td></tr>";
		
		html += "<tr><td>Is method static?</td>"
		+ "<td>" + bean.isMethodStatic() + "</td></tr>";
	
		html += "<tr><td>Method parameters</td><td> ";
		for (int i = 0; i< bean.getParameterTypes().length; i++){
			html += bean.getParameterNames()[i] +": " + bean.getParameterTypes()[i];
			for (int j=0;j<bean.getParameterDimensions()[i];j++){
				html += "[]";
			}
			html += "<br>";
		}
		html += "</td></tr>";
		
		html += "<tr><td>Method return type</td><td> ";
		html += bean.getReturnType() ;
		for (int j=0;j<bean.getReturnDimension();j++){
			html += "[]";
		}
		html += "</td></tr>";

		return html;
	}

	@Override
	protected String getViewTitle() {
		return "API consumer activity";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new ApiConsumerActivityConfigurationAction(
				(ApiConsumerActivity) getActivity(), owner);
	}
	
}
