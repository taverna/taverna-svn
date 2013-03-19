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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 *
 *
 * @author David Withers
 */
public class SemanticAnnotationContextualView extends AbstractSemanticAnnotationContextualView {

	public static final String VIEW_TITLE = "Semantic Annotations";

	private static FileManager fileManager = FileManager.getInstance();

	private static Logger logger = Logger.getLogger(SemanticAnnotationContextualView.class);

	private ComponentProfile componentProfile;
	public SemanticAnnotationContextualView(Annotated<?> selection) {
		super(true);
		super.setAnnotated(selection);
		componentProfile = getComponentProfile();
		if (componentProfile != null) {
			if (selection instanceof Dataflow) {
				super.setSemanticAnnotationProfiles(componentProfile.getSemanticAnnotationProfiles());
			} else if (selection instanceof DataflowInputPort) {
				super.setSemanticAnnotationProfiles(componentProfile.getInputSemanticAnnotationProfiles());
			} else if (selection instanceof DataflowOutputPort) {
				super.setSemanticAnnotationProfiles(componentProfile.getOutputSemanticAnnotationProfiles());
			} else if (selection instanceof Processor) {
				super.setSemanticAnnotationProfiles(componentProfile
						.getActivitySemanticAnnotationProfiles());
			} else {
				super.setSemanticAnnotationProfiles(new ArrayList<SemanticAnnotationProfile>());
			}
		} else {
			super.setSemanticAnnotationProfiles(new ArrayList<SemanticAnnotationProfile>());
		}

		super.populateModel();

		super.initialise();
		super.initView();
	}

	private ComponentProfile getComponentProfile() {
		Object dataflowSource = fileManager.getDataflowSource(fileManager.getCurrentDataflow());
		if (dataflowSource instanceof ComponentVersionIdentification) {
			ComponentVersionIdentification identification = (ComponentVersionIdentification) dataflowSource;
			try {
				ComponentRegistry componentRegistry = ComponentUtil.calculateRegistry(identification
						.getRegistryBase()); 
				ComponentFamily componentFamily = componentRegistry
						.getComponentFamily(identification.getFamilyName());
				return componentFamily.getComponentProfile();
			} catch (ComponentRegistryException e) {
				logger.warn("No component profile found for component family "
						+ identification.getFamilyName() + " at component registry "
						+ identification.getRegistryBase(), e);
			}
		}
		return null;
	}

	@Override
	public String getViewTitle() {
		return VIEW_TITLE;
	}

/*	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setSize(400, 200);
		ComponentVersionIdentification identification = new ComponentVersionIdentification(
				new URL("http://sandbox.myexperiment.org"),
				"SCAPE Migration Action Components", "Image To Tiff", 2);
		Dataflow dataflow = fileManager.openDataflow(new ComponentFileType(), identification);

		Processor processor = edits.createProcessor("processor");
		try {
			editManager.doDataflowEdit(dataflow, edits.getAddProcessorEdit(dataflow, processor));
		} catch (EditException e) {
			e.printStackTrace();
		}
		final SemanticAnnotationContextualView view = new SemanticAnnotationContextualView(
				processor);
		editManager.addObserver(new Observer<EditManager.EditManagerEvent>() {
			@Override
			public void notify(Observable<EditManagerEvent> arg0, EditManagerEvent arg1)
					throws Exception {
				view.refreshView();
				view.repaint();
			}
		});
		frame.add(view);
		frame.setVisible(true);
	}
*/
}
