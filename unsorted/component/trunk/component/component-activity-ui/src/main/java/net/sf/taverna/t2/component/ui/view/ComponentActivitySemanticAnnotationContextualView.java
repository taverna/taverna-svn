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
import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
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
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 *
 *
 * @author David Withers
 */
public class ComponentActivitySemanticAnnotationContextualView extends ContextualView {

	public static final String VIEW_TITLE = "Inherited Semantic Annotations";

	private static Logger logger = Logger.getLogger(ComponentActivitySemanticAnnotationContextualView.class);

	private static EditManager editManager = EditManager.getInstance();
	private JPanel panel;

	private Annotated<?> correspondingAnnotated;

	private ComponentProfile componentProfile;
	private List<SemanticAnnotationProfile> semanticAnnotationProfiles;
	private Model model;

	public ComponentActivitySemanticAnnotationContextualView(Object selection) {
		ComponentActivity componentActivity = ComponentActivitySemanticAnnotationContextViewFactory.getContainingComponentActivity(selection);
		ComponentActivityConfigurationBean configuration = componentActivity.getConfiguration();
		Dataflow underlyingDataflow;
		try {
			underlyingDataflow = ComponentDataflowCache.getDataflow(configuration);
			if (selection instanceof ComponentActivity) {
				this.correspondingAnnotated = underlyingDataflow;
			} else if (selection instanceof ActivityInputPort) {
				String name = ((ActivityInputPort) selection).getName();
				for (DataflowInputPort dip : underlyingDataflow.getInputPorts()) {
					if (dip.getName().equals(name)) {
						this.correspondingAnnotated = dip;
						break;
					}
				}
			} else if (selection instanceof ActivityOutputPort) {
				String name = ((ActivityOutputPort) selection).getName();
				for (DataflowOutputPort dop : underlyingDataflow.getOutputPorts()) {
					if (dop.getName().equals(name)) {
						this.correspondingAnnotated = dop;
						break;
					}
				}
			}
		componentProfile = ComponentUtil.calculateFamily(configuration.getRegistryBase(), configuration.getFamilyName()).getComponentProfile();
		if (componentProfile != null) {
			if (selection instanceof ComponentActivity) {
				semanticAnnotationProfiles = componentProfile.getSemanticAnnotationProfiles();
			}
			else if (selection instanceof ActivityInputPort) {
				semanticAnnotationProfiles = componentProfile.getInputSemanticAnnotationProfiles();
			} else if (selection instanceof ActivityOutputPort) {
				semanticAnnotationProfiles = componentProfile.getOutputSemanticAnnotationProfiles();
			}
		} 
		model = ModelFactory.createDefaultModel();
		SemanticAnnotation annotation = findSemanticAnnotation(correspondingAnnotated);
		if (annotation != null && !annotation.getContent().isEmpty()) {
			StringReader stringReader = new StringReader(annotation.getContent());
			model.read(stringReader, null, "N3");
		}

		initialise();
		initView();
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return VIEW_TITLE;
	}

	@Override
	public void refreshView() {
		initialise();
	}

	@Override
	public int getPreferredPosition() {
		return 510;
	}

	private void initialise() {
		if (panel == null) {
			panel = new JPanel(new GridBagLayout());
		} else {
			panel.removeAll();
		}
		populatePanel(panel);
		revalidate();
	}

	private void populatePanel(JPanel panel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;

		Set<Statement> statements = model.listStatements().toSet();
		for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
			OntProperty predicate = semanticAnnotationProfile.getPredicate();
			Set<Statement> statementsWithPredicate = new HashSet<Statement>();
			for (Statement statement : statements) {
				Property statementPredicate = statement.getPredicate();
				if (statementPredicate.equals(predicate)) {
					statementsWithPredicate.add(statement);
				}
			}
			panel.add(new ComponentActivitySemanticAnnotationPanel(this, semanticAnnotationProfile,
					statementsWithPredicate), gbc);
			statements.removeAll(statementsWithPredicate);
		}
		if (semanticAnnotationProfiles.isEmpty()) {
			panel.add(new JLabel("No annotations were possible"));
		}
		// TODO handle any remaining statements

		gbc.weighty = 1;
		panel.add(new JPanel(), gbc);
	}

	private SemanticAnnotation findSemanticAnnotation(Annotated<?> annotated) {
		Date latestDate = null;
		SemanticAnnotation annotation = null;
		for (AnnotationChain chain : annotated.getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				AnnotationBeanSPI detail = assertion.getDetail();
				if (detail instanceof SemanticAnnotation) {
					Date assertionDate = assertion.getCreationDate();
					if ((latestDate == null) || latestDate.before(assertionDate)) {
						annotation = (SemanticAnnotation) detail;
						latestDate = assertionDate;
					}
				}
			}
		}
		return annotation;
	}

}
