package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

public class AdvancedMenu extends AbstractMenu {
	public AdvancedMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 1000, URI
				.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
				"Advanced");
	}
	
}
