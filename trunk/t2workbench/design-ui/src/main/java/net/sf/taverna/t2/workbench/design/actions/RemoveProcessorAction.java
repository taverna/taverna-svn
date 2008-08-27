package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * Action for removing a processor from the dataflow.
 *
 * @author David Withers
 */
public class RemoveProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveProcessorAction.class);

	private Processor processor;

	public RemoveProcessorAction(Dataflow dataflow, Processor processor, Component component) {
		super(dataflow, component);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Remove Processor");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			List<? extends ProcessorInputPort> inputPorts = processor.getInputPorts();
			List<? extends ProcessorOutputPort> outputPorts = processor.getOutputPorts();
			List<? extends Condition> controlledPreconditions = processor.getControlledPreconditionList();
			List<? extends Condition> preconditions = processor.getPreconditionList();
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			for (ProcessorInputPort inputPort : inputPorts) {
				Datalink datalink = inputPort.getIncomingLink();
				if (datalink != null) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
			}
			for (ProcessorOutputPort outputPort : outputPorts) {
				for (Datalink datalink : outputPort.getOutgoingLinks()) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
			}
			for (Condition condition : controlledPreconditions) {
				editList.add(edits.getRemoveConditionEdit(condition.getControl(), condition.getTarget()));
			}
			for (Condition condition : preconditions) {
				editList.add(edits.getRemoveConditionEdit(condition.getControl(), condition.getTarget()));
			}

			if (editList.isEmpty()) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveProcessorEdit(dataflow, processor));
			} else {
				editList.add(edits.getRemoveProcessorEdit(dataflow, processor));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}
		} catch (EditException e1) {
			logger.debug("Remove processor failed", e1);
		}
	}

}
