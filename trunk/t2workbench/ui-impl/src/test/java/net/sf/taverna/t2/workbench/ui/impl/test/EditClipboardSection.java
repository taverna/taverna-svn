package net.sf.taverna.t2.workbench.ui.impl.test;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class EditClipboardSection extends AbstractMenuSection {
	public EditClipboardSection() {
		super(URI
				.create("http://taverna.sf.net/2008/t2workbench/test#edit"), 20, URI
				.create("http://taverna.sf.net/2008/t2workbench/test#clipboard"));
	}
}
