package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class CloseWorkflowAction extends AbstractAction {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_WORKFLOW = "Close workflow";
	private FileManager fileManager = FileManager.getInstance();

	public CloseWorkflowAction() {
		super(CLOSE_WORKFLOW, WorkbenchIcons.deleteIcon);
	}

	public void actionPerformed(ActionEvent e) {
		closeWorkflow(e, fileManager.getCurrentDataflow());
	}

	public boolean closeWorkflow(ActionEvent event, Dataflow dataflow) {
		Component parentComponent = null;
		if (dataflow == null) {
			logger.warn("Attempted to close a null dataflow");
			return false;
		}
		if (event.getSource() instanceof Component) {
			parentComponent = (Component) event.getSource();
		}
		try {
			fileManager.closeDataflow(dataflow, true);
			return true;
		} catch (UnsavedException e1) {
			fileManager.setCurrentDataflow(dataflow);
			String msg = "Do you want to save changes before closing the workflow "
					+ dataflow.getLocalName() + "?";
			int ret = JOptionPane.showConfirmDialog(parentComponent, msg,
					"Save workflow?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) {
				return false;
			} else if (ret == JOptionPane.NO_OPTION) {
				try {
					fileManager.closeDataflow(dataflow, false);
					return true;
				} catch (UnsavedException e2) {
					logger.error("Unexpected UnsavedException while "
							+ "closing workflow", e2);
					return false;
				}
			} else if (ret == JOptionPane.YES_OPTION) {
				new SaveWorkflowAction().actionPerformed(event);
				return true;
			} else {
				logger.error("Unknown return from JOptionPane: " + ret);
				return false;
			}
		}
	}
}
