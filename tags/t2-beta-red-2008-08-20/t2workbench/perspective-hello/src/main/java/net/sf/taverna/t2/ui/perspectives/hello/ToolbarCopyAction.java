package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class ToolbarCopyAction extends AbstractMenuAction {

	public ToolbarCopyAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/test#clipboardToolbar"),
				20);
	}

	@Override
	public Action createAction() {
		return new AbstractAction("Copy") {

			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "Copy");
			}
		};
	}

}
