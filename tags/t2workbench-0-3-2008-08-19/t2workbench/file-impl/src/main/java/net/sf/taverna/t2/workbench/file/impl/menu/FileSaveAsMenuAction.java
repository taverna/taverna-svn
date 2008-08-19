package net.sf.taverna.t2.workbench.file.impl.menu;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAsAction;

public class FileSaveAsMenuAction extends AbstractMenuAction {

	public FileSaveAsMenuAction() {
		super(FileSaveMenuSection.FILE_SAVE_SECTION_URI, 20);
	}

	@Override
	protected Action createAction() {
		return new SaveWorkflowAsAction();
	}

	
}
