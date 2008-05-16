package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.ActionSPI;

public class EditCutAction implements ActionSPI {

	public List<Action> getActions() {
		Action action = new AbstractAction("Cut") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Cut");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "edit|Edit|2";
	}

	public int getMenuPosition() {
		return 10;
	}
	
	public int getToolBarPosition() {
		return 30;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}
