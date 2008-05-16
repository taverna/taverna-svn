package net.sf.taverna.t2.workbench.ui;


import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.ActionSPI;

public class FileTestAction implements ActionSPI {

	public List<Action> getActions() {
		Action action = new AbstractAction("Test Workflow") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Test Workflow Action");
			}
			
		};
		return Collections.singletonList(action);
	}

	public String getMenu() {
		return "file1|File|50.test|Test|30";
	}

	public int getMenuPosition() {
		return 10;
	}
	
	public int getToolBarPosition() {
		return 0;
	}
	
	public boolean isToggleAction() {
		return true;
	}

}
