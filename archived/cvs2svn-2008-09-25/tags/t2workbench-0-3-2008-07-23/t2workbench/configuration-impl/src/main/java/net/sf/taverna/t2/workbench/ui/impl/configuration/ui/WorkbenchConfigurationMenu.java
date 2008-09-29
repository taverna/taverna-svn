package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class WorkbenchConfigurationMenu extends AbstractMenuAction {

	private static final String MAC_OS_X = "Mac OS X";

	public WorkbenchConfigurationMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#preferences"),
				100);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Preferences") {
			public void actionPerformed(ActionEvent event) {
				T2ConfigurationFrame.showFrame();
			}
		};
	}

	@Override
	public boolean isEnabled() {
		return !MAC_OS_X.equalsIgnoreCase(System.getProperty("os.name"));
	}

}
