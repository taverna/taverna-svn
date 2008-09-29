package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

public class WorkbenchPreferncesSection extends AbstractMenuSection {

	public WorkbenchPreferncesSection() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"), 100, URI.create("http://taverna.sf.net/2008/t2workbench/menu#preferences"));
	}

}
