package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class FileOpenSection extends AbstractMenuSection {

	public FileOpenSection() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"),
				20,
				URI.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenSection"));
	}

}
