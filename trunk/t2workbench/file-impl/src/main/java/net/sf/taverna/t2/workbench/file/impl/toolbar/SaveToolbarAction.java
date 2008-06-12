package net.sf.taverna.t2.workbench.file.impl.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;

public class SaveToolbarAction extends AbstractMenuAction {

	private static final URI FILE_SAVE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileToolbarSave");

	public SaveToolbarAction() {
		super(FileToolbarMenuSection.FILE_TOOLBAR_SECTION, FILE_SAVE_URI, 30);
	}

	@Override
	protected Action createAction() {
		return new SaveWorkflowAction();
	}

}
