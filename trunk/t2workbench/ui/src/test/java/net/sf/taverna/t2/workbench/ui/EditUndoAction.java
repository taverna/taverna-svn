package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.WorkbenchAction;

public class EditUndoAction implements WorkbenchAction {

	public List<Action> getActions() {
		Action action = new AbstractAction("Undo") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Undo");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "edit|Edit|2";
	}

	public int getMenuPosition() {
		return 1;
	}
	
	public int getToolBarPosition() {
		return 0;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}
