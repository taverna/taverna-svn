package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.design.Tools;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

public class RemoveDatalinkAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveDatalinkAction.class);

	private Datalink datalink;

	public RemoveDatalinkAction(Dataflow dataflow, Datalink datalink, Component component) {
		super(dataflow, component);
		this.datalink = datalink;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Remove Datalink");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			editManager.doDataflowEdit(dataflow, Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
		} catch (EditException e1) {
			logger.debug("Remove processor failed", e1);
		}
	}

}
