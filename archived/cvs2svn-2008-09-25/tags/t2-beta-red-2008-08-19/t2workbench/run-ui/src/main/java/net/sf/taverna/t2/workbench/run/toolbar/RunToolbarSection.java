package net.sf.taverna.t2.workbench.run.toolbar;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;

public class RunToolbarSection extends AbstractMenuSection {

	public static final URI RUN_TOOLBAR_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#runToolbarSection");

	public RunToolbarSection() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR, 30, RUN_TOOLBAR_SECTION);
	}

}
