package net.sf.taverna.t2.workbench.ui.impl.test;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

public class FileOpenAction extends AbstractMenuAction {
	public FileOpenAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#file"),
				20);
	}

	@Override
	public Action getAction() {
		return new AbstractAction("Open") {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(Workbench.getInstance(), "Open");
			}
		};
	}

}
