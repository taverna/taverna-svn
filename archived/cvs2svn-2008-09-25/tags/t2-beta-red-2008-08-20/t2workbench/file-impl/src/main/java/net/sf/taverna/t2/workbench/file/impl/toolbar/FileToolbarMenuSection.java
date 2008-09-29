package net.sf.taverna.t2.workbench.file.impl.toolbar;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;

public class FileToolbarMenuSection extends AbstractMenuSection {

	public static final URI FILE_TOOLBAR_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileToolbarSection");

	public FileToolbarMenuSection() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR, 20, FILE_TOOLBAR_SECTION);
	}

}
