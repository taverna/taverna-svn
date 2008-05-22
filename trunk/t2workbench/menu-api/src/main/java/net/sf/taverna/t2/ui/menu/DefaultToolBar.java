package net.sf.taverna.t2.ui.menu;

import java.net.URI;

public class DefaultToolBar extends AbstractToolBar {

	/**
	 * The URI of a tool bar item representing the default tool bar. Items who
	 * has this URI as their {@link #getParentId()} will be shown in the default
	 * toolbar of the main application window.
	 */
	public static final URI DEFAULT_TOOL_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultToolBar");

	public DefaultToolBar() {
		super(DEFAULT_TOOL_BAR);
	}

}
