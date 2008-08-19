package net.sf.taverna.t2.workbench.file.impl.menu;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;

public class FileSaveMenuAction extends AbstractMenuAction {

	public FileSaveMenuAction() {
		super(FileSaveMenuSection.FILE_SAVE_SECTION_URI, 10);
	}

	@Override
	protected Action createAction() {
		return new SaveWorkflowAction();
	}

}
