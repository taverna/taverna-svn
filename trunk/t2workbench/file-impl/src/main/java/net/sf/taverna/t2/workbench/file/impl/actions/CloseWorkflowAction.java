package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.UnsavedException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

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
		Component parentComponent = null;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		}
		try {
			fileManager.closeCurrentDataflow(true);
		} catch (UnsavedException e1) {
			String msg = "Do you want to save changes before closing the workflow "
					+ fileManager.getCurrentDataflow().getLocalName() + "?";
			int ret = JOptionPane.showConfirmDialog(parentComponent, msg,
					"Save workflow?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) {
			}
			if (ret == JOptionPane.NO_OPTION) {
				try {
					fileManager.closeCurrentDataflow(false);
				} catch (UnsavedException e2) {
					logger.error("Unexpected UnsavedException while "
							+ "closing workflow", e2);
				}
			}
			if (ret == JOptionPane.YES_OPTION) {
				new SaveWorkflowAction().actionPerformed(e);
			}
		}

	}

}
