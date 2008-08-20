package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

public class HelpMenu extends AbstractMenu {

	public static final URI HELP_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#help");

	public HelpMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 1024, HELP_URI, "Help");
	}

}
