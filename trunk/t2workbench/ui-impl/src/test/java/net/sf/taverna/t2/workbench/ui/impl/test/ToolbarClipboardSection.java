package net.sf.taverna.t2.workbench.ui.impl.test;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;

public class ToolbarClipboardSection extends AbstractMenuSection {
	public ToolbarClipboardSection() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR,
			20,
			URI.create("http://taverna.sf.net/2008/t2workbench/test#clipboardToolbar"));
	}
}
