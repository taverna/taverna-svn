package net.sf.taverna.t2.workbench.file.impl.menu;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveAllWorkflowsAction;

public class FileSaveAllMenuAction extends AbstractMenuAction {

	public FileSaveAllMenuAction() {
		super(FileSaveMenuSection.FILE_SAVE_SECTION_URI, 30);
	}

	@Override
	protected Action createAction() {
		return new SaveAllWorkflowsAction();
	}

	
}
