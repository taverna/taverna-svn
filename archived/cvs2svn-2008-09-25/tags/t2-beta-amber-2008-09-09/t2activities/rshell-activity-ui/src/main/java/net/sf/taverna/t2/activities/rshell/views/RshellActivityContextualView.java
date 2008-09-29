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

import javax.help.CSH;
import javax.swing.Action;

import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A simple non-editable HTML table view over a {@link RshellActivity}.

 * @author Alex Nenadic
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
		CSH
		.setHelpIDString(
				this,
		"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.RshellContextualView");
	}
	
	@Override
	protected String getRawTableRowsHtml() {
		// FIXME: fill in the table rows.
		String html = "";
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Rshell activity";
	}
	
	@Override
	public Action getConfigureAction(Frame owner) {
		//return new RshellActivityConfigurationAction(
			//	(RshellActivity) getActivity(), owner);
		return null;
	}

}
