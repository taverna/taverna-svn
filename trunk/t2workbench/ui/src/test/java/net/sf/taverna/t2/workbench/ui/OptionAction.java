package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.WorkbenchAction;

public class OptionAction implements WorkbenchAction {

	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new AbstractAction("Option 1") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Workbench.getInstance(), "Option 1");
			}
			
		});
		actions.add(new AbstractAction("Option 2") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Workbench.getInstance(), "Option 2");
			}
			
		});
		actions.add(new AbstractAction("Option 3") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Workbench.getInstance(), "Option 3");
			}
			
		});
		return actions;
	}

	public String getMenu() {
		return "option|Option|20";
	}

	public int getMenuPosition() {
		return 10;
	}
	
	public int getToolBarPosition() {
		return 40;
	}

	public boolean isToggleAction() {
		return false;
	}

}
