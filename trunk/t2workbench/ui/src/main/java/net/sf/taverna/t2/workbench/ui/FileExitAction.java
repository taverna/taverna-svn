package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class FileExitAction implements WorkbenchAction {

	public List<Action> getActions() {
		Action action = new AbstractAction("Exit") {

			public void actionPerformed(ActionEvent arg0) {
				Workbench.getInstance().exit();
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "file|File|0";
	}

	public int getMenuPosition() {
		return Integer.MAX_VALUE;
	}
	
	public int getToolBarPosition() {
		return -1;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}
