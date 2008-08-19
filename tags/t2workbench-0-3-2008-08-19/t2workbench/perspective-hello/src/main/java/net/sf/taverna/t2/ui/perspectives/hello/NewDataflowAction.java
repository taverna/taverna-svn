package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class NewDataflowAction extends AbstractMenuAction {

	public NewDataflowAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#new"), 20);
	}

	@Override
	public Action createAction() {
		return new AbstractAction("Dataflow") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null,
						"New Dataflow Action");
			}
		};
	}

}
