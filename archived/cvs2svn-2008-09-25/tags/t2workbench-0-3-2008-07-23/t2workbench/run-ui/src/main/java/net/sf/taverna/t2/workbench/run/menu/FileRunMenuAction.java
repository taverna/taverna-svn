package net.sf.taverna.t2.workbench.run.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.run.actions.RunWorkflowAction;

public class FileRunMenuAction extends AbstractMenuAction {

	private static final URI FILE_RUN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileRun");

	public FileRunMenuAction() {
		super(FileRunMenuSection.FILE_RUN_SECTION_URI, 10, FILE_RUN_URI);
	}

	@Override
	protected Action createAction() {
		return new RunWorkflowAction();
	}

}
