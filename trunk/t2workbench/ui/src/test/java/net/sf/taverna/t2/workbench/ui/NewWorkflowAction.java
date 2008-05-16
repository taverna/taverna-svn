package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class NewWorkflowAction implements WorkbenchAction {

	public List<Action> getActions() {
		Action action = new AbstractAction("Workflow") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "New Workflow Action");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "file.new";
	}

	public int getMenuPosition() {
		return 20;
	}
	
	public int getToolBarPosition() {
		return 0;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}