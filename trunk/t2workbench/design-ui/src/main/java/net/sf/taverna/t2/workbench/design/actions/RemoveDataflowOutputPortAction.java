package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

public class RemoveDataflowOutputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveDataflowOutputPortAction.class);

	private DataflowOutputPort port;

	public RemoveDataflowOutputPortAction(Dataflow dataflow, DataflowOutputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Remove Output Port");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Datalink datalink = port.getInternalInputPort().getIncomingLink();
			if (datalink == null) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveDataflowOutputPortEdit(dataflow, port));
			} else {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				editList.add(edits.getRemoveDataflowOutputPortEdit(dataflow, port));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}			
		} catch (EditException e1) {
			logger.debug("Remove dataflow output port failed", e1);
		}
	}

}
