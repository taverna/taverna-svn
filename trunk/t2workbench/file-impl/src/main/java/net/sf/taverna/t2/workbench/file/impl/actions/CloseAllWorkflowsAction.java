package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
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
	private CloseWorkflowAction closeWorkflowAction = new CloseWorkflowAction();

	public CloseAllWorkflowsAction() {
		super(CLOSE_ALL_WORKFLOWS, WorkbenchIcons.deleteIcon);
	}

	public void actionPerformed(ActionEvent event) {
		Component parentComponent = null;
		if (event.getSource() instanceof Component) {
			parentComponent = (Component) event.getSource();
		}
		closeAllWorkflows(parentComponent);
	}

	public boolean closeAllWorkflows(Component parentComponent) {
		List<Dataflow> dataflows = fileManager.getOpenDataflows();
		Collections.reverse(dataflows);

		for (Dataflow dataflow : dataflows) {
			boolean success = closeWorkflowAction.closeWorkflow(
					parentComponent, dataflow);
			if (!success) {
				return false;
			}
		}
		return true;
	}

}
