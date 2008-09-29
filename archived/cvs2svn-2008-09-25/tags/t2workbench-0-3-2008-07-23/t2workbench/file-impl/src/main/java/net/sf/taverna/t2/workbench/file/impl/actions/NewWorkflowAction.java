package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

public class NewWorkflowAction extends AbstractAction {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(NewWorkflowAction.class);
	private static final String NEW_WORKFLOW = "New workflow";
	private FileManager fileManager = FileManager.getInstance();

	public NewWorkflowAction() {
		super(NEW_WORKFLOW, WorkbenchIcons.newIcon);
		putValue(Action.SHORT_DESCRIPTION, NEW_WORKFLOW);
	}

	public void actionPerformed(ActionEvent e) {
		fileManager.newDataflow();
	}

}
