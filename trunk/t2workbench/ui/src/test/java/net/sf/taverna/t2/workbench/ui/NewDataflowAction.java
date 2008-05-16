package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.ActionSPI;

public class NewDataflowAction implements ActionSPI {

	public List<Action> getActions() {
		Action action = new AbstractAction("Dataflow") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "New Dataflow Action");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "file.new|New|20";
	}

	public int getMenuPosition() {
		return 10;
	}
	
	public int getToolBarPosition() {
		return 0;
	}
	
	public boolean isToggleAction() {
		return false;
	}

}
