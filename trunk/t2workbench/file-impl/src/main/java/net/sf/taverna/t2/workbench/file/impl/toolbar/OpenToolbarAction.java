package net.sf.taverna.t2.workbench.file.impl.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;

public class OpenToolbarAction extends AbstractMenuAction {

	private static final URI FILE_OPEN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileToolbarOpen");

	public OpenToolbarAction() {
		super(FileToolbarMenuSection.FILE_TOOLBAR_SECTION, FILE_OPEN_URI, 20);
	}

	@Override
	protected Action createAction() {
		return new OpenWorkflowAction();
	}

}
