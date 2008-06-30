package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
public class NewWorkflowAction extends AbstractMenuAction {

	public NewWorkflowAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#new"), 10);
	}

	@Override
	public Action createAction() {
		return new AbstractAction("Workflow") {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null,
						"New Workflow Action");
			}
		};
	}

}