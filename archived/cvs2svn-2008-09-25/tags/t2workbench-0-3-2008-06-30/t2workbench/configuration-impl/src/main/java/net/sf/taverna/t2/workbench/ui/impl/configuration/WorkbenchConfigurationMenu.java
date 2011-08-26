package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class WorkbenchConfigurationMenu extends AbstractMenuAction {

	public WorkbenchConfigurationMenu() {
		super(URI
				.create("http://taverna.sf.net/2008/t2workbench/menu#preferences"), 100);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Preferences...") {
			public void actionPerformed(ActionEvent event) {
				JFrame tempFrame = new JFrame();
				tempFrame.getContentPane().add(new WorkbenchConfigurationUIFactory().getConfigurationPanel());
				tempFrame.setSize(300,200);
				tempFrame.setVisible(true);
			}
		};
	}

	

	
}
