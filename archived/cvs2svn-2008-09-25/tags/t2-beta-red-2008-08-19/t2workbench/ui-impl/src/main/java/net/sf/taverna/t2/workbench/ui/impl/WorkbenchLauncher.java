package net.sf.taverna.t2.workbench.ui.impl;

import net.sf.taverna.raven.launcher.Launchable;

public class WorkbenchLauncher implements Launchable {

	public int launch(String[] args) throws Exception {
		Workbench workbench = Workbench.getInstance();
		workbench.setVisible(true);
		return 0;
	}

}
