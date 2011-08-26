package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class CloseAllWorkflowsAction extends AbstractAction {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_ALL_WORKFLOWS = "Close all workflows";
	private FileManager fileManager = FileManager.getInstance();

	public CloseAllWorkflowsAction() {
		super(CLOSE_ALL_WORKFLOWS, WorkbenchIcons.deleteIcon);
	}

	public void actionPerformed(ActionEvent e) {
		List<Dataflow> dataflows = fileManager.getOpenDataflows();
		Collections.reverse(dataflows);
		CloseWorkflowAction closeWorkflowAction = new CloseWorkflowAction();
		for (Dataflow dataflow : dataflows) {
			boolean success = closeWorkflowAction.closeWorkflow(e, dataflow);
			if (!success) {
				break;
			}
		}
	}

}
