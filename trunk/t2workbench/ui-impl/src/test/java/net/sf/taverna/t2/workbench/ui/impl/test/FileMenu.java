package net.sf.taverna.t2.workbench.ui.impl.test;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

public class FileMenu extends AbstractMenu {

	public FileMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 10, URI
				.create("http://taverna.sf.net/2008/t2workbench/test#file"),
				"File");
	}

}
