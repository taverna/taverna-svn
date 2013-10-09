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
package net.sf.taverna.t2.component.ui.view;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.annotation.AbstractSemanticAnnotationContextualView;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author David Withers
 */
public class ComponentActivitySemanticAnnotationContextualView extends AbstractSemanticAnnotationContextualView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7403728889085410126L;

	public static final String VIEW_TITLE = "Inherited Semantic Annotations";

	private static Logger logger = Logger.getLogger(ComponentActivitySemanticAnnotationContextualView.class);

	private Profile componentProfile;

	public ComponentActivitySemanticAnnotationContextualView(Object selection) {
		super(false);
		ComponentActivity componentActivity = ComponentActivitySemanticAnnotationContextViewFactory.getContainingComponentActivity(selection);
		ComponentActivityConfigurationBean configuration = componentActivity.getConfiguration();
		Dataflow underlyingDataflow;
		try {
			underlyingDataflow = ComponentDataflowCache.getDataflow(configuration);
			if (selection instanceof ComponentActivity) {
				super.setAnnotated(underlyingDataflow);
			} else if (selection instanceof ActivityInputPort) {
				String name = ((ActivityInputPort) selection).getName();
				for (DataflowInputPort dip : underlyingDataflow.getInputPorts()) {
					if (dip.getName().equals(name)) {
						super.setAnnotated(dip);
						break;
					}
				}
			} else if (selection instanceof ActivityOutputPort) {
				String name = ((ActivityOutputPort) selection).getName();
				for (DataflowOutputPort dop : underlyingDataflow.getOutputPorts()) {
					if (dop.getName().equals(name)) {
						super.setAnnotated(dop);
						break;
					}
				}
			}
		componentProfile = ComponentUtil.calculateFamily(configuration.getRegistryBase(), configuration.getFamilyName()).getComponentProfile();
		if (componentProfile != null) {
			if (selection instanceof ComponentActivity) {
				super.setSemanticAnnotationProfiles(componentProfile.getSemanticAnnotationProfiles());
			}
			else if (selection instanceof ActivityInputPort) {
				super.setSemanticAnnotationProfiles(componentProfile.getInputSemanticAnnotationProfiles());
			} else if (selection instanceof ActivityOutputPort) {
				super.setSemanticAnnotationProfiles(componentProfile.getOutputSemanticAnnotationProfiles());
			}
		} 

		super.initialise();

		} catch (RegistryException e) {
			logger.error(e);
		}
	}

	@Override
	public String getViewTitle() {
		return VIEW_TITLE;
	}

}
