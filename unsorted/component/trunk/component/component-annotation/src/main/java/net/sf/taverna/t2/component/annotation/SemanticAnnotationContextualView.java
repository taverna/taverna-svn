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
package net.sf.taverna.t2.component.annotation;

import static java.lang.String.format;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author David Withers
 */
public class SemanticAnnotationContextualView extends
		AbstractSemanticAnnotationContextualView {
	private static final long serialVersionUID = -322165507536778154L;
	public static final String VIEW_TITLE = "Semantic Annotations";
	private static FileManager fileManager = FileManager.getInstance();
	private static Logger logger = getLogger(SemanticAnnotationContextualView.class);

	private Profile componentProfile;

	public SemanticAnnotationContextualView(Annotated<?> selection) {
		super(true);
		super.setAnnotated(selection);
		componentProfile = getComponentProfile();
		try {
			if (componentProfile == null)
				super.setSemanticAnnotationProfiles(new ArrayList<SemanticAnnotationProfile>());
			else if (selection instanceof Dataflow)
				super.setSemanticAnnotationProfiles(componentProfile
						.getSemanticAnnotationProfiles());
			else if (selection instanceof DataflowInputPort)
				super.setSemanticAnnotationProfiles(componentProfile
						.getInputSemanticAnnotationProfiles());
			else if (selection instanceof DataflowOutputPort)
				super.setSemanticAnnotationProfiles(componentProfile
						.getOutputSemanticAnnotationProfiles());
			else if (selection instanceof Processor)
				super.setSemanticAnnotationProfiles(componentProfile
						.getActivitySemanticAnnotationProfiles());
			else
				super.setSemanticAnnotationProfiles(new ArrayList<SemanticAnnotationProfile>());

		} catch (RegistryException e) {
			logger.error("failed to look up semantic annotations", e);
		}

		super.initialise();
	}

	private Profile getComponentProfile() {
		Object dataflowSource = fileManager.getDataflowSource(fileManager
				.getCurrentDataflow());
		if (dataflowSource instanceof Version.ID) {
			Version.ID identification = (Version.ID) dataflowSource;
			try {
				Registry componentRegistry = calculateRegistry(identification
						.getRegistryBase());
				Family componentFamily = componentRegistry
						.getComponentFamily(identification.getFamilyName());
				return componentFamily.getComponentProfile();
			} catch (RegistryException e) {
				logger.warn(
						format("No component profile found for component family %s at component registry %s",
								identification.getFamilyName(),
								identification.getRegistryBase()), e);
			}
		}
		return null;
	}

	@Override
	public String getViewTitle() {
		return VIEW_TITLE;
	}

/*
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setSize(400, 200);
		ComponentVersionIdentification identification = new ComponentVersionIdentification(
				new URL("http://sandbox.myexperiment.org"),
				"SCAPE Migration Action Components", "Image To Tiff", 2);
		Dataflow dataflow = fileManager.openDataflow(new ComponentFileType(),
				identification);

		Processor processor = edits.createProcessor("processor");
		try {
			editManager.doDataflowEdit(dataflow,
					edits.getAddProcessorEdit(dataflow, processor));
		} catch (EditException e) {
			e.printStackTrace();
		}
		final SemanticAnnotationContextualView view = new SemanticAnnotationContextualView(
				processor);
		editManager.addObserver(new Observer<EditManager.EditManagerEvent>() {
			@Override
			public void notify(Observable<EditManagerEvent> arg0,
					EditManagerEvent arg1) throws Exception {
				view.refreshView();
				view.repaint();
			}
		});
		frame.add(view);
		frame.setVisible(true);
	}
*/
}
