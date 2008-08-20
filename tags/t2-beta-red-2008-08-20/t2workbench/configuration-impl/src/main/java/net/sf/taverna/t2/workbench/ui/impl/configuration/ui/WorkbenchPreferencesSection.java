package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class WorkbenchPreferencesSection extends AbstractMenuSection {

	public WorkbenchPreferencesSection() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"), 100, URI.create("http://taverna.sf.net/2008/t2workbench/menu#preferences"));
	}

}
