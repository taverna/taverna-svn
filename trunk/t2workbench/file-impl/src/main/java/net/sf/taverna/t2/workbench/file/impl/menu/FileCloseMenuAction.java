package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.CloseWorkflowAction;

public class FileCloseMenuAction extends AbstractMenuAction {

	private static final URI FILE_CLOSE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileClose");

	public FileCloseMenuAction() {
		super(FileOpenMenuSection.FILE_URI, FILE_CLOSE_URI, 30);
	}

	@Override
	protected Action createAction() {
		return new CloseWorkflowAction();
	}

}
