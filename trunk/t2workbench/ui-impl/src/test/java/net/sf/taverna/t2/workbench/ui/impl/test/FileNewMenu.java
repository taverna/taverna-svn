package net.sf.taverna.t2.workbench.ui.impl.test;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;

public class FileNewMenu extends AbstractMenu {

	public FileNewMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#file"),
				10,
				URI.create("http://taverna.sf.net/2008/t2workbench/test#new"),
				"New");
	}

}
