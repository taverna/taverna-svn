package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class EditCutAction extends AbstractMenuAction {

	public EditCutAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#clipboard"),
				10);
	}

	@Override
	public Action createAction() {
		return new AbstractAction("Cut") {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Cut");
			}
		};
	}
}
