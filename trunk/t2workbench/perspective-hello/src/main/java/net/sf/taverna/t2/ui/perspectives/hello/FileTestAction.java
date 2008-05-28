package net.sf.taverna.t2.ui.perspectives.hello;


import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class FileTestAction extends AbstractMenuAction {

	public FileTestAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"),
				50);
	}

	@Override
	public Action getAction() {
		return new AbstractAction("Test") {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Test");
			}
		};
	}
}
