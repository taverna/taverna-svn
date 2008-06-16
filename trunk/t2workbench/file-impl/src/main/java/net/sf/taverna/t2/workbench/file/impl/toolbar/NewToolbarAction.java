package net.sf.taverna.t2.workbench.file.impl.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.NewWorkflowAction;

public class NewToolbarAction extends AbstractMenuAction {

	private static final URI FILE_NEW_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileToolbarNew");

	public NewToolbarAction() {
		super(FileToolbarMenuSection.FILE_TOOLBAR_SECTION, 10, FILE_NEW_URI);
	}

	@Override
	protected Action createAction() {
		return new NewWorkflowAction();
	}

}
