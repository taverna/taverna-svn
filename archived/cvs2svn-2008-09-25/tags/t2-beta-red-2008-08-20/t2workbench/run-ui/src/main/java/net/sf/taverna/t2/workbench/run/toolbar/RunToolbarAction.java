package net.sf.taverna.t2.workbench.run.toolbar;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.run.actions.RunWorkflowAction;

public class RunToolbarAction extends AbstractMenuAction {

	private static final URI RUN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#runToolbarRun");

	public RunToolbarAction() {
		super(RunToolbarSection.RUN_TOOLBAR_SECTION, 10, RUN_URI);
	}

	@Override
	protected Action createAction() {
		return new RunWorkflowAction();
	}

}
