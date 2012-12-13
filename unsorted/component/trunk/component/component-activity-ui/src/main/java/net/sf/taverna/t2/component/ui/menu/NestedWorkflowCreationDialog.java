/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.annotationbeans.AbstractTextualValueAssertion;
import net.sf.taverna.t2.component.ui.menu.component.ComponentServiceCreatorAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class NestedWorkflowCreationDialog extends HelpEnabledDialog {
	
	private static Logger logger = Logger
	.getLogger(NestedWorkflowCreationDialog.class);
	
	private static FileManager fm = FileManager.getInstance();
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();
	
	private static AnnotationTools at = new AnnotationTools();
	
	private List<TokenProcessingEntity> includedProcessors = new ArrayList<TokenProcessingEntity>();
	private List<? extends Processor> allProcessors;
	private List<TokenProcessingEntity> includableProcessors = new ArrayList<TokenProcessingEntity>();
	
	private static Comparator<TokenProcessingEntity> processorComparator = new Comparator<TokenProcessingEntity>(){

		@Override
		public int compare(TokenProcessingEntity o1, TokenProcessingEntity o2) {
			return o1.getLocalName().compareTo(o2.getLocalName());
		}};
		
	private static ListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	private static ListCellRenderer processorRenderer = new ListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			TokenProcessingEntity p = (TokenProcessingEntity) value;
			return defaultRenderer.getListCellRendererComponent(list, p.getLocalName(), index, isSelected, cellHasFocus);
		}};

	private JList includableList = new JList();
	private JList includedList = new JList();
	private Object selectedObject;
	private final Dataflow currentDataflow;
	
	public NestedWorkflowCreationDialog(Frame owner, Object o, Dataflow dataflow) {
		super(owner, "Nested workflow creation", true, null);
		
		
		selectedObject = o;
		this.currentDataflow = dataflow;
		
		allProcessors = dataflow.getProcessors();
		
		this.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		JPanel processorChoice = createProcessorChoicePanel(o, dataflow);
		this.add(processorChoice, BorderLayout.CENTER);

		JButton okButton = new JButton(new OKAction(this));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(this));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(this));
		buttonPanel.add(cancelButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		this.setSize(new Dimension(500, 800));
	}

	private JPanel createProcessorChoicePanel(Object o, Dataflow dataflow) {
		JPanel result = new JPanel();
		result.setLayout(new GridLayout(0,2));
			
		JPanel includedProcessors = createIncludedProcessorsPanel();
		JPanel includableProcessors = createIncludableProcessorsPanel();
		result.add(includableProcessors);
		result.add(includedProcessors);
		resetLists();
		return result;
	}

	private JPanel createIncludableProcessorsPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(new JLabel("Possible services"), BorderLayout.NORTH);
		includableList.setModel(new DefaultComboBoxModel(includableProcessors.toArray()));
		includableList.setCellRenderer(processorRenderer);
		result.add(new JScrollPane(includableList), BorderLayout.CENTER);
		
		JButton includeButton = new JButton(new AbstractAction("Include"){

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Object o : includableList.getSelectedValues()) {
					if (o instanceof TokenProcessingEntity) {
						includedProcessors.add((TokenProcessingEntity) o);
					}
				}				
				calculateIncludableProcessors();
				updateLists();
			}});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(includeButton);
		result.add(buttonPanel, BorderLayout.SOUTH);
		return result;
	}

	private void resetLists() {
		includedProcessors.clear();
		updateLists();		
	}
	
	private JPanel createIncludedProcessorsPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(new JLabel("Included services"), BorderLayout.NORTH);
		includedList.setModel(new DefaultComboBoxModel(includedProcessors.toArray()));
		includedList.setCellRenderer(processorRenderer);
		result.add(new JScrollPane(includedList), BorderLayout.CENTER);
		
		JButton excludeButton = new JButton(new AbstractAction("Exclude"){

			@Override
			public void actionPerformed(ActionEvent e) {
				for (Object o : includedList.getSelectedValues()) {
					if (o instanceof TokenProcessingEntity) {
						includedProcessors.remove((TokenProcessingEntity) o);
					}
				}	
				calculateIncludableProcessors();
				updateLists();
			}});
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(excludeButton);
		excludeButton.setEnabled(false);
		result.add(buttonPanel, BorderLayout.SOUTH);
		return result;
	}
	
	private void updateLists() {
		calculateIncludableProcessors();
		Collections.sort(includedProcessors, processorComparator);
		Collections.sort(includableProcessors, processorComparator);
		includedList.setModel(new DefaultComboBoxModel(includedProcessors.toArray()));
		includableList.setModel(new DefaultComboBoxModel(includableProcessors.toArray()));
	}
	
	public void calculateIncludableProcessors() {
		includableProcessors.clear();
		if (includedProcessors.isEmpty()) {
			includableProcessors.addAll(allProcessors);
		} else {
			for (TokenProcessingEntity p : includedProcessors) {
				considerNearestUpstream(p);
				considerNearestDownstream(p);
			}
		}
		Collections.sort(includableProcessors, processorComparator);
	}

	private void considerNearestDownstream(TokenProcessingEntity investigate) {

			if (investigate instanceof Processor) {
				Processor processor = (Processor) investigate;
				List<? extends Condition> controlledConditions = processor
						.getControlledPreconditionList();
				for (Condition condition : controlledConditions) {
					Processor downstreamProc = condition.getTarget();
					considerInclusion(downstreamProc);
				}
			}

			for (EventForwardingOutputPort outputPort : investigate
					.getOutputPorts()) {
				for (Datalink datalink : outputPort.getOutgoingLinks()) {
					EventHandlingInputPort sink = datalink.getSink();
					if (sink instanceof ProcessorInputPort) {
						Processor downstreamProc = ((ProcessorInputPort) sink)
								.getProcessor();
						considerInclusion(downstreamProc);
					} else if (sink instanceof MergeInputPort) {
						Merge merge = ((MergeInputPort) sink).getMerge();
						considerInclusion(merge);
						// The merge it self doesn't count as a processor
					} else {
						// Ignore dataflow ports
					}
				}
			}
	}

	private void considerNearestUpstream (TokenProcessingEntity investigate) {

			if (investigate instanceof Processor) {
				Processor processor = (Processor) investigate;
				List<? extends Condition> preConditions = processor
						.getPreconditionList();
				for (Condition condition : preConditions) {
					Processor upstreamProc = condition.getControl();
					considerInclusion(upstreamProc);
				}
			}
			for (EventHandlingInputPort inputPort : investigate.getInputPorts()) {
				Datalink incomingLink = inputPort.getIncomingLink();
				if (incomingLink == null) {
					continue;
				}
				EventForwardingOutputPort source = incomingLink.getSource();
				if (source instanceof ProcessorOutputPort) {
					Processor upstreamProc = ((ProcessorOutputPort) source)
							.getProcessor();
					considerInclusion(upstreamProc);
				} else if (source instanceof MergeOutputPort) {
					Merge merge = ((MergeOutputPort) source).getMerge();
					considerInclusion(merge);
				} else {
					// Ignore
				}
			}
	}
	
	private void considerInclusion(TokenProcessingEntity p) {
		if (!includedProcessors.contains(p) && !includableProcessors.contains(p)) {
			includableProcessors.add(p);
		}
	}

	private final class OKAction extends AbstractAction {
		private final JDialog dialog;

		private OKAction(JDialog dialog) {
			super("OK");
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			
			if (includedProcessors.isEmpty()) {
				JOptionPane.showMessageDialog(null, "At least one service must be included in the nested workflow", "Nested workflow creation", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			final List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

			HashMap<Object,Object> oldNewMapping = new HashMap<Object,Object>();
			
			HashMap<Datalink, String> linkProcessorPortMapping = new HashMap<Datalink, String> ();
			
			Processor nestingProcessor = edits.createProcessor(Tools.uniqueProcessorName("nested", currentDataflow));
			if (includedProcessors.size() == 1) {
				TokenProcessingEntity includedEntity = includedProcessors.get(0);
				if (includedEntity instanceof Processor) {
					Processor includedProcessor = (Processor) includedEntity;
					for (Class c : at.getAnnotatingClasses(includedProcessor)) {
						AnnotationBeanSPI annotation = at.getAnnotation(includedProcessor, c);
						if ((annotation != null) && (annotation instanceof AbstractTextualValueAssertion)){
							currentWorkflowEditList.add(at.setAnnotationString(nestingProcessor, c, ((AbstractTextualValueAssertion)annotation).getText()));
						}
					}
				}
			}
			
			Dataflow nestedDataflow = edits.createDataflow();
			for (TokenProcessingEntity p : includedProcessors) {
				try {
					if (p instanceof Processor) {
						currentWorkflowEditList.add(edits.getRemoveProcessorEdit(currentDataflow, (Processor) p));
						Processor newProcessor = ComponentServiceCreatorAction.pasteProcessor(ComponentServiceCreatorAction.copyProcessor((Processor) p), nestedDataflow);
						oldNewMapping.put(p, newProcessor);
						for (ProcessorInputPort pip : ((Processor) p).getInputPorts()) {
							for (ProcessorInputPort newPip : newProcessor.getInputPorts()) {
								if (pip.getName().equals(newPip.getName())) {
									oldNewMapping.put (pip, newPip);
									break;
								}
							}
						}
						for (ProcessorOutputPort pop : ((Processor) p).getOutputPorts()) {
							for (ProcessorOutputPort newPop : newProcessor.getOutputPorts()) {
								if (pop.getName().equals(newPop.getName())) {
									oldNewMapping.put (pop, newPop);
									break;
								}
							}
						}
					} else if (p instanceof Merge) {
						currentWorkflowEditList.add(edits.getRemoveMergeEdit(currentDataflow, (Merge) p));
						Merge newMerge = edits.createMerge(nestedDataflow);
						edits.getAddMergeEdit(nestedDataflow, newMerge).doEdit();
						oldNewMapping.put(p, newMerge);
						for (MergeInputPort mip : ((Merge) p).getInputPorts()) {
							MergeInputPort newMip = edits.createMergeInputPort(newMerge, mip.getName(), mip.getDepth());
							edits.getAddMergeInputPortEdit(newMerge, newMip).doEdit();
							oldNewMapping.put(mip, newMip);
						}
						oldNewMapping.put(((Merge)p).getOutputPort(), newMerge.getOutputPort());
					}
				} catch (Exception e1) {
					logger.error(e1);
				}
			}
			HashSet<String> inputPortNames = new HashSet<String>();
			HashSet<String> outputPortNames = new HashSet<String>();
			for (Datalink dl : currentDataflow.getLinks()) {
				if (oldNewMapping.containsKey(dl.getSource()) && oldNewMapping.containsKey(dl.getSink())) {
					// Internal to nested workflow
					Datalink newDatalink = edits.createDatalink((EventForwardingOutputPort) oldNewMapping.get(dl.getSource()),
							(EventHandlingInputPort) oldNewMapping.get(dl.getSink()));
					try {
						edits.getConnectDatalinkEdit(newDatalink).doEdit();
					} catch (EditException e1) {
						logger.error(e1);
					}
				} else if (oldNewMapping.containsKey(dl.getSource())) {
					// Coming out of nested workflow
					String portName = Tools.uniqueObjectName(dl.getSource().getName(), outputPortNames);
					outputPortNames.add(portName);
					linkProcessorPortMapping.put(dl, portName);
					DataflowOutputPort dop = edits.createDataflowOutputPort(portName, nestedDataflow);
					try {
						edits.getAddDataflowOutputPortEdit(nestedDataflow, dop).doEdit();
						Datalink newDatalink = edits.createDatalink((EventForwardingOutputPort) oldNewMapping.get(dl.getSource()),
																	dop.getInternalInputPort());
						edits.getConnectDatalinkEdit(newDatalink).doEdit();
					} catch (EditException e1) {
						logger.error(e1);
					}
				} else if (oldNewMapping.containsKey(dl.getSink())) {
					// Coming into nested workflow
					String portName = Tools.uniqueObjectName(dl.getSink().getName(), inputPortNames);
					inputPortNames.add(portName);
					linkProcessorPortMapping.put(dl, portName);
					DataflowInputPort dip = edits.createDataflowInputPort(portName,
								dl.getResolvedDepth(), dl.getResolvedDepth(), nestedDataflow);
					try {
						edits.getAddDataflowInputPortEdit(nestedDataflow, dip).doEdit();
						Datalink newDatalink = edits.createDatalink(dip.getInternalOutputPort(),
								(EventHandlingInputPort) oldNewMapping.get(dl.getSink()));
						edits.getConnectDatalinkEdit(newDatalink).doEdit();
					} catch (EditException e1) {
						logger.error(e1);
					}				}
			}
			HashSet<Condition> alreadyConsidered = new HashSet<Condition>();
			for (Processor p : currentDataflow.getProcessors()) {
				boolean isTargetMoved = oldNewMapping.containsKey(p);
				for (Condition c : p.getPreconditionList()) {
					if (alreadyConsidered.contains(c)) {
						continue;
					} else {
						alreadyConsidered.add(c);
					}
					Processor pre = c.getControl();
					boolean isControlMoved = oldNewMapping.containsKey(pre);
					if (isTargetMoved && isControlMoved) {
						// Add in new condition
						try {
							edits.getCreateConditionEdit((Processor) oldNewMapping.get(pre), (Processor) oldNewMapping.get(p)).doEdit();
						} catch (EditException e1) {
							logger.error(e1);
						}
					} else if (isTargetMoved) {
						currentWorkflowEditList.add(edits.getRemoveConditionEdit(pre, p));
						currentWorkflowEditList.add(edits.getCreateConditionEdit(pre, nestingProcessor));
					} else if (isControlMoved) {
						currentWorkflowEditList.add(edits.getRemoveConditionEdit(pre, p));
						currentWorkflowEditList.add(edits.getCreateConditionEdit(nestingProcessor, p));
					}
				}
			}
			DataflowActivity da = new DataflowActivity();
			try {
				da.configure(nestedDataflow);
			} catch (ActivityConfigurationException e1) {
				logger.error(e1);
			}
			try {
				edits.getAddActivityEdit(nestingProcessor, da).doEdit();
				edits.getDefaultDispatchStackEdit(nestingProcessor).doEdit();
				for (ActivityInputPort aip : da.getInputPorts()) {
					ProcessorInputPort pip = edits.createProcessorInputPort(nestingProcessor, aip.getName(), aip.getDepth());
					edits.getAddProcessorInputPortEdit(nestingProcessor, pip).doEdit();
					edits.getAddActivityInputPortMappingEdit(da, aip.getName(), aip.getName()).doEdit();
				}
				for (OutputPort aop : da.getOutputPorts()) {
					ProcessorOutputPort pop = edits.createProcessorOutputPort(nestingProcessor, aop.getName(), aop.getDepth(), aop.getGranularDepth());
					edits.getAddProcessorOutputPortEdit(nestingProcessor, pop).doEdit();
					edits.getAddActivityOutputPortMappingEdit(da, aop.getName(), aop.getName()).doEdit();
				}
			} catch (EditException e1) {
				logger.error(e1);
			}

			
			currentWorkflowEditList.add(edits.getAddProcessorEdit(currentDataflow, nestingProcessor));

			
			for (Datalink dl : currentDataflow.getLinks()) {
				if (oldNewMapping.containsKey(dl.getSource()) && oldNewMapping.containsKey(dl.getSink())) {
					// Internal to nested workflow
					currentWorkflowEditList.add(edits.getDisconnectDatalinkEdit(dl));
				} else if (oldNewMapping.containsKey(dl.getSource())) {
					// Coming out of nested workflow
					String portName = linkProcessorPortMapping.get(dl);
					ProcessorOutputPort nestedPort = null;
					for (ProcessorOutputPort pop : nestingProcessor.getOutputPorts()) {
						if (pop.getName().equals(portName)) {
							nestedPort = pop;
							break;
						}
					}
					if (nestedPort != null) {
						Datalink replacementDatalink = edits.createDatalink(nestedPort, dl.getSink());
						currentWorkflowEditList.add(edits.getDisconnectDatalinkEdit(dl));
						currentWorkflowEditList.add(edits.getConnectDatalinkEdit(replacementDatalink));
					}
				} else if (oldNewMapping.containsKey(dl.getSink())) {
					// Coming into nested workflow
					String portName = linkProcessorPortMapping.get(dl);
					ProcessorInputPort nestedPort = null;
					for (ProcessorInputPort pip : nestingProcessor.getInputPorts()) {
						if (pip.getName().equals(portName)) {
							nestedPort = pip;
							break;
						}
					}
					if (nestedPort != null) {
						Datalink replacementDatalink = edits.createDatalink(dl.getSource(), nestedPort);
						currentWorkflowEditList.add(edits.getDisconnectDatalinkEdit(dl));
						currentWorkflowEditList.add(edits.getConnectDatalinkEdit(replacementDatalink));
					}
				}
			}
			try {
				GraphController gc = GraphViewComponent.graphControllerMap.get(currentDataflow);
				gc.setExpandNestedDataflow(nestedDataflow, true);
				em.doDataflowEdit(currentDataflow, new CompoundEdit(currentWorkflowEditList));
				gc.redraw();
			} catch (EditException e1) {
				logger.error(e1);
			}
			dialog.setVisible(false);
		}
	}
	
	private final class ResetAction extends AbstractAction {
		private final JDialog dialog;

		private ResetAction(JDialog dialog) {
			super("Reset");
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			resetLists();
		}

	}

	private final class CancelAction extends AbstractAction {
		private final JDialog dialog;

		private CancelAction(JDialog dialog) {
			super("Cancel");
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}

	}
}
