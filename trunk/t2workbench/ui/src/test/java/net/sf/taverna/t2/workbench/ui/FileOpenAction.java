package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class FileOpenAction implements WorkbenchAction {

	public List<Action> getActions() {
		Action action = new AbstractAction("Open Workflow") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Workbench.getInstance(), "Open Workflow Action");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "file|File|1";
	}

	public int getMenuPosition() {
		return 21;
	}
	
	public int getToolBarPosition() {
		return 10;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}
