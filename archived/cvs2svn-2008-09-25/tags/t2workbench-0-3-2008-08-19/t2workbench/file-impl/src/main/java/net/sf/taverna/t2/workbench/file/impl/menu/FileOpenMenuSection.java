package net.sf.taverna.t2.workbench.file.impl.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class FileOpenMenuSection extends AbstractMenuSection {

	public static final URI FILE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#file");
	public static final URI FILE_OPEN_SECTION_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenSection");

	public FileOpenMenuSection() {
		super(FILE_URI, 20, FILE_OPEN_SECTION_URI);
	}

}
