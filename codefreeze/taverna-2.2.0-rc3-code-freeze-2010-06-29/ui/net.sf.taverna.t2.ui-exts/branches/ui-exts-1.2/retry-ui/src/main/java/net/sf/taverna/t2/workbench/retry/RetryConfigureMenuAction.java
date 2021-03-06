/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.workbench.retry;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;

public class RetryConfigureMenuAction extends AbstractContextualMenuAction {
	
	
	
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	
	private static final URI RETRY_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/retryConfigure");

	private static final String RETRY_CONFIGURE = "Retry configure";

	public RetryConfigureMenuAction() {
		super(configureRunningSection, 30, RETRY_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Retries...") {
			public void actionPerformed(ActionEvent e) {
				Retry retryLayer = null;
				Processor p = (Processor) getContextualSelection().getSelection();
				for (DispatchLayer dl : p.getDispatchStack().getLayers()) {
					if (dl instanceof Retry) {
						retryLayer = (Retry) dl;
						break;
					}
				}
				if (retryLayer != null) {
				RetryConfigureAction retryConfigureAction = new RetryConfigureAction(null, null, retryLayer);
				retryConfigureAction.actionPerformed(e);
				}
			}
		};
	}
	
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Processor);
	}

}
