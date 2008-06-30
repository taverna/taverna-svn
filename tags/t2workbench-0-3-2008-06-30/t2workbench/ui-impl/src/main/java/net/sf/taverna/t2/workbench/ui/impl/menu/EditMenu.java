package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

public class EditMenu extends AbstractMenu {

	public EditMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 20, URI
				.create("http://taverna.sf.net/2008/t2workbench/menu#edit"),
				"Edit");
	}

}
