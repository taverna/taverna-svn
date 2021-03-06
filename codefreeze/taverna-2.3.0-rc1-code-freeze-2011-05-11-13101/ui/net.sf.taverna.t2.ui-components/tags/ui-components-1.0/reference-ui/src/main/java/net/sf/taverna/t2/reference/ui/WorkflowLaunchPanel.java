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
package net.sf.taverna.t2.reference.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.annotation.annotationbeans.Author;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionSPI;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionsSPIRegistry;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

/**
 * A simple workflow launch panel, uses a tabbed layout to display a set of
 * named InputConstructionPanel instances, and a 'run workflow' button. Also
 * shows a tabbed pane picture of the workflow, the author and the description
 * 
 * @author Tom Oinn
 * @author David Withers
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public abstract class WorkflowLaunchPanel extends JPanel {

	private static Logger logger = Logger.getLogger(WorkflowLaunchPanel.class);
	/**
	 * Maps original dataflows to their copies - required because the
	 * WunWorkflowAction copies the dataflow before sending it here so you lose
	 * the connection with the dataflow that the {@link GraphController} has
	 */
	private static Map<Dataflow, Dataflow> dataflowCopyMap = new HashMap<Dataflow, Dataflow>();

	private static final String LAUNCH_WORKFLOW = "Run workflow";

	private final ImageIcon launchIcon = new ImageIcon(getClass().getResource(
			"/icons/start_task.gif"));

	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private final Action launchAction;

	private static final Map<Dataflow, Map<String, RegistrationPanel>> workflowInputPanelMap = new HashMap<Dataflow, Map<String, RegistrationPanel>>();
	private final Map<String, RegistrationPanel> inputPanelMap;
	private final Map<String, T2Reference> inputMap = new HashMap<String, T2Reference>();
	/**
	 * Holds the previous user inputs for a particular workflow. The Dataflow is
	 * the original one so need to use the workflowInputPanelMap to find it
	 */
	private static Map<Dataflow, Map<String, T2Reference>> previousInputsMap = new HashMap<Dataflow, Map<String, T2Reference>>();

	private final JTabbedPane tabs;
	private final Map<String, RegistrationPanel> tabComponents = new HashMap<String, RegistrationPanel>();

	private WorkflowInstanceFacade facade;
	private ReferenceService referenceService;
	private InvocationContextImpl invocationContext;
	private final Dataflow dataflow;
	
	private final static String NO_WORKFLOW_DESCRIPTION = "No description";
	private static final String NO_WORKFLOW_AUTHOR = "No author";

	private DialogTextArea workflowDescriptionArea;
	private DialogTextArea workflowAuthorArea;

	private AnnotationTools annotationTools = new AnnotationTools();
	private JSVGCanvas createWorkflowGraphic;

	public WorkflowLaunchPanel(Dataflow dtfl, ReferenceService refService) {
		super(new BorderLayout());
		
		this.dataflow = dtfl;
		this.referenceService = refService;
		
		JPanel workflowPart = new JPanel(new GridLayout(3,1));
		JPanel portsPart = new JPanel(new BorderLayout());

		createWorkflowGraphic = createWorkflowGraphic(dataflow);
		createWorkflowGraphic.setBorder(new TitledBorder("Diagram"));
		
		workflowPart.add(createWorkflowGraphic);

		workflowDescriptionArea = new DialogTextArea(NO_WORKFLOW_DESCRIPTION, 5, 40);
		workflowDescriptionArea.setBorder(new TitledBorder("Workflow description"));
		workflowDescriptionArea.setEditable(false);
		workflowDescriptionArea.setLineWrap(true);
		workflowDescriptionArea.setWrapStyleWord(true);
		
		workflowPart.add(new JScrollPane(workflowDescriptionArea));

		workflowAuthorArea = new DialogTextArea(NO_WORKFLOW_AUTHOR, 1, 40);
		workflowAuthorArea.setBorder(new TitledBorder("Workflow author"));
		workflowAuthorArea.setEditable(false);
		workflowAuthorArea.setLineWrap(true);
		workflowAuthorArea.setWrapStyleWord(true);
		
		workflowPart.add(new JScrollPane(workflowAuthorArea));

		Dataflow key = dataflowCopyMap.get(dataflow);
		if (workflowInputPanelMap.containsKey(key)) {
			inputPanelMap = workflowInputPanelMap.get(key);
		} else {
			inputPanelMap = new HashMap<String, RegistrationPanel>();
			workflowInputPanelMap.put(key, inputPanelMap);
		}

		launchAction = new AbstractAction(LAUNCH_WORKFLOW, launchIcon) {
			public void actionPerformed(ActionEvent ae) {
				
				// Create provenance connector and facade, similar as in RunWorkflowAction
				
				// TODO check if the database has been created and create if needed
				// if provenance turned on then add an IntermediateProvLayer to each
				// Processor
				ProvenanceConnector provenanceConnector = null;
				
				// FIXME: All these run-stuff should be done in a general way so it
				// could also be used when running workflows non-interactively
				if (DataManagementConfiguration.getInstance().isProvenanceEnabled()) {
					String connectorType = DataManagementConfiguration
							.getInstance().getConnectorType();

					for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
							.getInstance().getInstances()) {
						if (connectorType.equalsIgnoreCase(factory
								.getConnectorType())) {
							provenanceConnector = factory.getProvenanceConnector();
						}
					}

					// slight change, the init is outside but it also means that the
					// init call has to ensure that the dbURL is set correctly
					try {
						if (provenanceConnector != null) {
							provenanceConnector.init();
							provenanceConnector
									.setReferenceService(referenceService);
						}
					} catch (Exception except) {

					}				
				}
				invocationContext = new InvocationContextImpl(
						referenceService, provenanceConnector);
				if (provenanceConnector != null) {
					provenanceConnector.setInvocationContext(invocationContext);
				}
				// Workflow run id will be set on the invocation context from the facade
				try {
					facade = new EditsImpl().createWorkflowInstanceFacade(
							dataflow, invocationContext, "");
				} catch (InvalidDataflowException ex) {
					InvalidDataflowReport.invalidDataflow(ex.getDataflowValidationReport());
					return;
				}	
				
				registerInputs(facade.getDataflow());
				handleLaunch(inputMap);
			}
		};

		new JTabbedPane();

		String wfDescription = annotationTools.getAnnotationString(dataflow, FreeTextDescription.class, "");
		setWorkflowDescription(wfDescription);

		String wfAuthor = annotationTools.getAnnotationString(dataflow, Author.class, "");
		setWorkflowAuthor(wfAuthor);

		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JButton(launchAction));
		toolBar.add(new JButton(new AbstractAction("Cancel", WorkbenchIcons.closeIcon) {

			public void actionPerformed(ActionEvent e) {
				handleCancel();
			}}));
		
		JToolBar loadButtonsBar = new JToolBar();
		loadButtonsBar.setFloatable(false);
		ReferenceActionsSPIRegistry spiRegistry = ReferenceActionsSPIRegistry.getInstance();
		for (ReferenceActionSPI spi : spiRegistry.getInstances()) {
			ReferenceActionSPI action = (ReferenceActionSPI) spi.getAction();
			action.setInputPanelMap(inputPanelMap);
			JButton loadButton = new JButton((AbstractAction) action);
			loadButtonsBar.add(loadButton);
		}
		
		JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.add(loadButtonsBar, BorderLayout.WEST);
		toolBarPanel.add(toolBar, BorderLayout.EAST);
		toolBarPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
		portsPart.add(toolBarPanel, BorderLayout.SOUTH);
		
		// Construct tab container
		tabs = new JTabbedPane();
		portsPart.add(tabs, BorderLayout.CENTER);
		
		workflowPart.setPreferredSize(new Dimension(300,500));
		portsPart.setPreferredSize(new Dimension(500,500));
		
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.X_AXIS));

		overallPanel.add(workflowPart);
		overallPanel.add(portsPart);

		this.add(new JScrollPane(overallPanel), BorderLayout.CENTER);
		this.revalidate();
	}

	/**
	 * Creates an SVGCanvas loaded with the SVGDocument for the Dataflow.
	 * 
	 * @param dataflow
	 * @return
	 */
	private JSVGCanvas createWorkflowGraphic(Dataflow dataflow) {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		SVGGraphController graphController = GraphViewComponent.graphControllerMap
				.get(dataflowCopyMap.get(dataflow));
		if (graphController != null) {
			SVGDocument svgDoc = graphController.getSVGDocument();
			svgCanvas.setDocument((SVGDocument) svgDoc.cloneNode(true));
		}
		return svgCanvas;
	}
	
	public static Map<Dataflow, Dataflow> getDataflowCopyMap() {
		return dataflowCopyMap;
	}

	public synchronized void addInput(final String inputName,
			final int inputDepth) {
		addInput(inputName, inputDepth, null, null);
	}

	public void addInput(final String inputName, final int inputDepth,
			String inputDescription, String inputExample) {
		// Don't do anything if we already have this tab
		Dataflow dataflow = dataflowCopyMap.get(this.dataflow);
		//workflow input panel has to be there or else something has gone wrong
		if (workflowInputPanelMap.containsKey(dataflow)) {
			Map<String, RegistrationPanel> map = workflowInputPanelMap
					.get(dataflow);
			if (map.isEmpty()) {
				map = new HashMap<String, RegistrationPanel>();
				workflowInputPanelMap.put(dataflow, map);
			}
			RegistrationPanel value = map.get(inputName);
			if ((value == null) || (value.getDepth() != inputDepth)) {
				value = new RegistrationPanel(inputDepth, inputName, inputDescription, inputExample);
				map.put(inputName, value);
				inputPanelMap.put(inputName, value);
			} else {
				value.setStatus("Drag to re-arrange, or drag files, URLs, or text to add",
				null);
				value.setDescription(inputDescription);
				value.setExample(inputExample);
			}
			inputMap.put(inputName, null);
			tabComponents.put(inputName, value);
			tabs.addTab(inputName, value);
		} else {
			logger.warn("There is no registration panel for the workflow");
		}

	}

	public synchronized void removeInputTab(final String inputName) {
		// Only do something if we have this tab to begin with
		if (inputMap.containsKey(inputName) == false) {
			return;
		} else {
			Component component = tabComponents.get(inputName);
			tabComponents.remove(inputName);
			inputMap.remove(inputName);
			tabs.remove(component);
		}
	}

	private void registerInputs(Dataflow dataflow) {
		for (String input : inputMap.keySet()) {
			RegistrationPanel registrationPanel = tabComponents.get(input);
			Object userInput = registrationPanel.getUserInput();
			int inputDepth = registrationPanel.getDepth();
			T2Reference reference = referenceService.register(userInput,
					inputDepth, true, invocationContext);
			inputMap.put(input, reference);
		}
		Dataflow dataflowOrig = dataflowCopyMap.get(dataflow);
		previousInputsMap.put(dataflowOrig, inputMap);
	}

	/**
	 * Called when the run workflow action has been performed
	 * 
	 * @param workflowInputs
	 *            a map of named inputs in the form of T2Reference instances
	 */
	public abstract void handleLaunch(Map<String, T2Reference> workflowInputs);
	
	public abstract void handleCancel();
	
	private static void selectTopOfTextArea(DialogTextArea textArea) {
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
	}

	public void setWorkflowDescription(String workflowDescription) {
		if ((workflowDescription != null) && (workflowDescription.length() > 0)) {
			this.workflowDescriptionArea
					.setText(workflowDescription);
			selectTopOfTextArea(this.workflowDescriptionArea);
		}
	}

	void setWorkflowAuthor(String workflowAuthor) {
		if ((workflowAuthor != null) && (workflowAuthor.length() > 0)) {
			this.workflowAuthorArea.setText(workflowAuthor);
			selectTopOfTextArea(this.workflowAuthorArea);
		}
	}

	public String getWorkflowDescription() {
		return workflowDescriptionArea.getText();
	}

	@Override
	protected void finalize() throws Throwable {
		createWorkflowGraphic.stopProcessing();
		super.finalize();
	}

	public WorkflowInstanceFacade getFacade() {
		return facade;
	}
	
}
