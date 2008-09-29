package net.sf.taverna.t2.workbench.run.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class FileRunMenuSection extends AbstractMenuSection {

	public static final URI FILE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#file");
	public static final URI FILE_RUN_SECTION_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileRunSection");

	public FileRunMenuSection() {
		super(FILE_URI, 40, FILE_RUN_SECTION_URI);
	}

}
