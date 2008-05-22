package net.sf.taverna.t2.ui.menu;

import java.net.URI;

public class DefaultMenuBar extends AbstractMenu {

	/**
	 * The URI of a menu item representing the default menu bar. Menu items who
	 * has this URI as their {@link #getParentId()} will be shown in the top
	 * menu of the main application window.
	 */
	public static final URI DEFAULT_MENU_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultMenuBar");

	public DefaultMenuBar() {
		super(DEFAULT_MENU_BAR);
	}

}
