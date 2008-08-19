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
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

public class RemoveMergeAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveMergeAction.class);

	private Merge merge;

	public RemoveMergeAction(Dataflow dataflow, Merge merge, Component component) {
		super(dataflow, component);
		this.merge = merge;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Remove Merge");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			List<? extends MergeInputPort> inputPorts = merge.getInputPorts();
			EventForwardingOutputPort outputPort = merge.getOutputPort();
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			for (MergeInputPort inputPort : inputPorts) {
				Datalink datalink = inputPort.getIncomingLink();
				if (datalink != null) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
			}
			for (Datalink datalink : outputPort.getOutgoingLinks()) {
				editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
			}

			if (editList.isEmpty()) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveMergeEdit(dataflow, merge));
			} else {
				editList.add(edits.getRemoveMergeEdit(dataflow, merge));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}
		} catch (EditException e1) {
			logger.debug("Remove merge failed", e1);
		}
	}

}
