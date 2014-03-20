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
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.lineageservice.utils.DataflowInvocation;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

/**
 * This component contains a tabbed pane, where each tab displays results for one of
 * the output ports of a workflow, and a set of 'save results' buttons that save results 
 * from all ports in a certain format.
 * 
 * @author David Withers
 * @author Alex Nenadic
 *
 */
public class WorkflowResultsComponent extends JPanel implements UIComponentSPI, ResultListener {

	private static Logger logger = Logger
		.getLogger(WorkflowResultsComponent.class);


	private static final long serialVersionUID = 988812623494396366L;
	
	// Invocation context
	private InvocationContext context = null;
	
	// The map contains a mapping for each port to a T2Reference pointing to the port's result(s)
	private HashMap<String, T2Reference> resultReferencesMap = new HashMap<String, T2Reference>();
	
	private HashMap<String, T2Reference> inputReferencesMap = new HashMap<String, T2Reference> ();
	
	// Per-port boolean values indicating if all results have been received per port
	private HashMap<String, Boolean> receivedAllResultsForPort = new HashMap<String, Boolean>();
	
	// Tabbed pane - each tab contains a results tree and a RenderedResultComponent, 
	// which in turn contains the currently selected result node rendered according 
	// to its mime type and a button for saving the selected individual result
	private JTabbedPane tabbedPane;
	
	// Panel containing the save buttons
	private JPanel saveButtonsPanel;

	private WorkflowInstanceFacade facade;
	private Dataflow dataflow;
	
	private JButton saveButton;

	private String runId;

	private ReferenceService referenceService;

	// This is needed for "Save data as OPM" action so that we know if
	// we should try to get the OPM graph or not (if provanance was not
	// enabled there is no point in trying to save data as OPM as it will be missing)
	private boolean isProvenanceEnabledForRun;
		
	// Registry of all existing 'save results' actions, each one can save results
	// in a different format
	private static SaveAllResultsSPIRegistry saveAllResultsRegistry = SaveAllResultsSPIRegistry.getInstance();	
	
	private Map<String, PortResultsViewTab> inputPortTabMap = new HashMap<String, PortResultsViewTab>();
	private Map<String, PortResultsViewTab> outputPortTabMap = new HashMap<String, PortResultsViewTab>();
	
	public WorkflowResultsComponent(ReferenceService referenceService) {
		super(new BorderLayout());
		this.referenceService = referenceService;		
		setBorder(new EtchedBorder());
		tabbedPane = new JTabbedPane();
		saveButtonsPanel = new JPanel(new GridBagLayout());
		add(saveButtonsPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	// Constructor used for showing results for an old run when data
	// is obtained from provenance
	public WorkflowResultsComponent(Dataflow dataflow, String runId,
			ReferenceService rs) {
		this(rs);
		this.dataflow = dataflow;
		this.runId = runId;
		this.isProvenanceEnabledForRun = true; // for a previous run provenance is always turned on
		populateResultsFromProvenance();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Results View Component";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}
	
	private void populateSaveButtonsPanel() {
		GridBagConstraints gbc = new GridBagConstraints();
		
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0,10,0,0);
		saveButtonsPanel.add(new JLabel("Workflow results"), gbc);
		
		saveButton = new JButton(new SaveAllAction("Save all values", this));
		gbc.gridx++;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		saveButtonsPanel.add(saveButton, gbc);
		
	}

	public void register(WorkflowInstanceFacade facade, boolean isProvenanceEnabledForRun)
			throws EditException {
		
		clear();
		
		this.facade = facade;
		this.dataflow = facade.getDataflow();
		this.runId = facade.getWorkflowRunId();
		this.isProvenanceEnabledForRun = isProvenanceEnabledForRun;
		
		populateSaveButtonsPanel();
		
		// Input ports
		List<DataflowInputPort> dataflowInputPorts = new ArrayList<DataflowInputPort>(facade
				.getDataflow().getInputPorts());
		Collections.sort(dataflowInputPorts, new Comparator<DataflowInputPort>() {

			public int compare(DataflowInputPort o1, DataflowInputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});	
		for (DataflowInputPort dataflowInputPort : dataflowInputPorts) {
			String portName = dataflowInputPort.getName();
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowInputPort.getName(), dataflowInputPort.getDepth());
			
			inputPortTabMap.put(portName, resultTab);
			
			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab, "Input port " + portName);
		}
		
		// Output ports
		List<DataflowOutputPort> dataflowOutputPorts = new ArrayList<DataflowOutputPort>(facade
				.getDataflow().getOutputPorts());
		Collections.sort(dataflowOutputPorts, new Comparator<DataflowOutputPort>() {
			public int compare(DataflowOutputPort o1, DataflowOutputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
						
			// Initially we have no results for a port
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.FALSE));
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort.getName(),
					dataflowOutputPort.getDepth());
			outputPortTabMap.put(portName, resultTab);

			// Per-port tree model listens for results coming out of the data facade
			facade.addResultListener(resultTab.getResultModel());
			// This component also listens to the results coming out in order to know
			// when receiving of results has finished
			facade.addResultListener(this);
			
			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab, "Output port " + portName);
		}
		// Select the first output port tab
		if (!dataflowOutputPorts.isEmpty()){
			PortResultsViewTab tab = outputPortTabMap.get(dataflowOutputPorts.get(0).getName());
			tabbedPane.setSelectedComponent(tab);
		}	
		
		revalidate();
	}
	
	public void populateResultsFromProvenance() {
		
		String connectorType = DataManagementConfiguration.getInstance().getConnectorType();
		ProvenanceAccess provenanceAccess = null;
		try {
			provenanceAccess = new ProvenanceAccess(connectorType);
		}
		catch (Exception e) {
			logger.error("Unable to estable provenance access", e);
			return;
		}
		
		
		populateSaveButtonsPanel();

		// Get data for inputs and outputs ports
		DataflowInvocation dataflowInvocation = provenanceAccess.getDataflowInvocation(runId);
		if (dataflowInvocation != null) {
		    String inputsDataBindingId = dataflowInvocation.getInputsDataBindingId();
		    String outputsDataBindingId = dataflowInvocation.getOutputsDataBindingId();

		    Map<Port, T2Reference> dataBindings = new HashMap<Port, T2Reference>();
		    if (inputsDataBindingId != null){
			dataBindings.putAll(provenanceAccess
					    .getDataBindings(inputsDataBindingId));
		    }
		
		    if (outputsDataBindingId != null && !outputsDataBindingId.equals(inputsDataBindingId)){
			dataBindings.putAll(provenanceAccess
					    .getDataBindings(outputsDataBindingId));
		    }
		
		    // Input ports
		    List<DataflowInputPort> dataflowInputPorts = new ArrayList<DataflowInputPort>(dataflow.getInputPorts());
		    Collections.sort(dataflowInputPorts, new Comparator<DataflowInputPort>() {
			    public int compare(DataflowInputPort o1, DataflowInputPort o2) {
				return o1.getName().compareTo(o2.getName());
			    }});
		    for (DataflowInputPort dataflowInputPort : dataflowInputPorts) {
			String portName = dataflowInputPort.getName();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowInputPort.getName(), dataflowInputPort.getDepth());
			inputPortTabMap.put(portName, resultTab);
			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab, "Input port " + portName);
		    }
		
		    // Output ports
		    List<DataflowOutputPort> dataflowOutputPorts = new ArrayList<DataflowOutputPort>(dataflow.getOutputPorts());
		    Collections.sort(dataflowOutputPorts, new Comparator<DataflowOutputPort>() {
			public int compare(DataflowOutputPort o1, DataflowOutputPort o2) {
			    return o1.getName().compareTo(o2.getName());
			}});	
		    for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort.getName(), dataflowOutputPort.getDepth());		
			outputPortTabMap.put(portName, resultTab);
			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab, "Output port " + portName);
		    }		
		    // Select the first output port tab
		    if (!dataflowOutputPorts.isEmpty()){
			PortResultsViewTab tab = outputPortTabMap.get(dataflowOutputPorts.get(0).getName());
			tabbedPane.setSelectedComponent(tab);
		    }

		    for (java.util.Map.Entry<Port, T2Reference> entry : dataBindings
			     .entrySet()) {		
			if (entry.getKey().isInputPort()) { // input port

			    PortResultsViewTab resultTab = inputPortTabMap.get(entry.getKey().getPortName());
			    WorkflowResultTreeModel treeModel = resultTab.getResultModel();
			    treeModel.createTree(entry.getValue(), getContext(), ((WorkflowResultTreeNode) treeModel.getRoot()));				
			    // Need to refresh the tree model we have just changed by adding result nodes
			    resultTab.getModel().reload();
			    resultTab.expandTree(); // tree will be collapsed after reloading

			}
			else{ // output port
			    PortResultsViewTab resultTab = outputPortTabMap.get(entry.getKey().getPortName());
			    WorkflowResultTreeModel treeModel = resultTab.getResultModel();
			    treeModel.createTree(entry.getValue(), getContext(), ((WorkflowResultTreeNode) treeModel.getRoot()));	
			    // Need to refresh the tree model we have just changed by adding result nodes
			    resultTab.getModel().reload();
			    resultTab.expandTree(); // tree will be collapsed after reloading
			}
		    }
		}
	}
	
	public InvocationContext getContext() {
		if (context == null) {
			InvocationContext dummyContext = new DummyContext(referenceService);
			context = dummyContext;
		}
		return context;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public void selectWorkflowPortTab(DataflowPort port) {
		PortResultsViewTab tab;
		if (port instanceof DataflowInputPort) {
			tab = inputPortTabMap.get(port.getName());
		} else {
			tab = outputPortTabMap.get(port.getName());
		}
		if (tab != null) {
			tabbedPane.setSelectedComponent(tab);
		}
	}
	
	public void clear() {
		saveButtonsPanel.removeAll();
		tabbedPane.removeAll();
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {
		
		if (context == null || context instanceof DummyContext) {
			// Set the real invocation context		
			setContext(token.getContext());
		}
			
		// If we have finished receiving results - token.getIndex().length is 0
		if (token.getIndex().length == 0){
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.TRUE));
			// We know that at this point the token.getData() contains a T2Reference to 
			// all result(s)
			T2Reference resultsRef = token.getData();
			// Put the resultsRef in the resultReferencesMap
			resultReferencesMap.put(portName, resultsRef);
		}
		
		// If this is the last token for all ports - update the save buttons' state
		 boolean receivedAll = true;
		 for (String pName : receivedAllResultsForPort.keySet()){
		 	if (!receivedAllResultsForPort.get(pName).booleanValue()){
		 		receivedAll = false;
		 		break;
		 	}
		 }
		 if (receivedAll){

				for (DataflowInputPort dataflowInputPort : dataflow.getInputPorts()) {
					String name = dataflowInputPort.getName();
					inputReferencesMap.put(name, facade.getPushedDataMap().get(name));
			}
			saveButton.setEnabled(true);
			saveButton.setFocusable(false);
		 }
	}
	
	@SuppressWarnings("serial")
	private class SaveAllAction extends AbstractAction {
		
		//private WorkflowResultsComponent parent;

		public SaveAllAction(String name, WorkflowResultsComponent resultViewComponent) {
			super(name);
			//this.parent = resultViewComponent;
			putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
		}

		public void actionPerformed(ActionEvent e) {
			
			String title = "Workflow run data saver";
			
			final JDialog dialog = new HelpEnabledDialog(MainWindow.getMainWindow(), title, true);
			dialog.setResizable(true);
			dialog.setLocationRelativeTo(MainWindow.getMainWindow());
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation.setText("Select the workflow input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			explanation.setFocusable(false);
			explanation.setFont(new JLabel().getFont()); // make the font the same as for other components in the dialog
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<String, JCheckBox> ();
			final Map<String, JCheckBox> outputChecks = new HashMap<String, JCheckBox> ();
			final Map<JCheckBox, T2Reference> checkReferences =
				new HashMap<JCheckBox, T2Reference>();
			final Map<String, T2Reference> chosenReferences =
				new HashMap<String, T2Reference> ();
			final Set<Action> actionSet = new HashSet<Action>();

			ItemListener listener = new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					JCheckBox source = (JCheckBox) e.getItemSelectable();
					if (inputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (outputChecks.containsKey(source.getText())) {
								outputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					if (outputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (inputChecks.containsKey(source.getText())) {
								inputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					chosenReferences.clear();
					for (JCheckBox checkBox : checkReferences.keySet()) {
						if (checkBox.isSelected()) {
							chosenReferences.put(checkBox.getText(),
									checkReferences.get(checkBox));
						}
					}
				}
				
			};
			JPanel portsPanel = new JPanel();
			portsPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(5,10,5,10)), new EtchedBorder(EtchedBorder.LOWERED)));
			portsPanel.setLayout(new GridBagLayout());
			if (!dataflow.getInputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel("Workflow inputs:"), gbc);				
				//JPanel inputsPanel = new JPanel();
				//WeakHashMap<String, T2Reference> pushedDataMap =  null;

				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (DataflowInputPort port : dataflow.getInputPorts()) {
					String portName = port.getName();
					T2Reference o = inputReferencesMap.get(portName);
					if (o == null) {
						WorkflowResultTreeNode root = (WorkflowResultTreeNode) inputPortTabMap.get(portName).getResultModel().getRoot();
						o = root.getReference();
					}
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
							.setSelected(!resultReferencesMap.containsKey(portName));
					checkBox.addItemListener(listener);
					inputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
					checkReferences.put(checkBox, o);
				}
				gbc.insets = new Insets(0,10,0,10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			if (!dataflow.getOutputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel("Workflow outputs:"), gbc);
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (DataflowOutputPort port : dataflow.getOutputPorts()) {
					String portName = port.getName();
					T2Reference o = resultReferencesMap.get(portName);
					if (o == null) {
						WorkflowResultTreeNode root = (WorkflowResultTreeNode) outputPortTabMap.get(portName).getResultModel().getRoot();
						o = root.getReference();
					}
//					resultReferencesMap.put(portName, null);
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
								.setSelected(true);
						
					checkReferences.put(checkBox, o);
					checkBox.addItemListener(listener);
					outputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
				}
				gbc.insets = new Insets(0,10,0,10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			panel.add(new JScrollPane(portsPanel), BorderLayout.CENTER);
			chosenReferences.clear();
			for (JCheckBox checkBox : checkReferences.keySet()) {
				if (checkBox.isSelected()) {
					chosenReferences.put(checkBox.getText(),
							checkReferences.get(checkBox));
				}
			}


			JPanel buttonsBar = new JPanel();
			buttonsBar.setLayout(new FlowLayout());
			// Get all existing 'Save result' actions
			List<SaveAllResultsSPI> saveActions = saveAllResultsRegistry.getSaveResultActions();
			for (SaveAllResultsSPI spi : saveActions){
				spi.setProvenanceEnabledForRun(isProvenanceEnabledForRun);
				spi.setRunId(runId);
				spi.setDataflow(dataflow);
				AbstractAction action = spi.getAction();
				actionSet.add(action);
				JButton saveButton = new JButton((AbstractAction) action);
				if (action instanceof SaveAllResultsSPI) {
					((SaveAllResultsSPI)action).setChosenReferences(chosenReferences);
					((SaveAllResultsSPI)action).setParent(dialog);			
					((SaveAllResultsSPI)action).setReferenceService(referenceService);
					((SaveAllResultsSPI)action).setInvocationContext(getContext());
				}
				//saveButton.setEnabled(true);
				buttonsBar.add(saveButton);
			}
			JButton cancelButton = new JButton("Cancel", WorkbenchIcons.closeIcon);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
				
			});
			buttonsBar.add(cancelButton);
			panel.add(buttonsBar, BorderLayout.SOUTH);
			panel.setPreferredSize(new Dimension(900,500));
			panel.revalidate();
			dialog.add(panel);
			dialog.pack();
			dialog.setVisible(true);
		}
		
	}

	public void pushInputData(WorkflowDataToken token, String portName) {
		WorkflowResultTreeModel model = inputPortTabMap.get(portName).getResultModel();
		if (model != null) {
			model.resultTokenProduced(token, portName);
		}
	}


}
