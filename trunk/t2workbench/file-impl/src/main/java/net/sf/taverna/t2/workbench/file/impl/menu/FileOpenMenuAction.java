package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;

public class FileOpenMenuAction extends AbstractMenuAction {

	private static final URI FILE_OPEN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpen");

	public FileOpenMenuAction() {
		super(FileOpenMenuSection.FILE_OPEN_SECTION_URI, FILE_OPEN_URI, 20);
	}

	@Override
	protected Action createAction() {
		return new OpenWorkflowAction();
	}

}
