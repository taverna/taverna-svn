package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.awt.Dimension;
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
		return new AbstractAction("Preferences") {
			public void actionPerformed(ActionEvent event) {
				JFrame frame =  new T2ConfigurationFrame();
				frame.setSize(new Dimension(700,500));
				frame.pack();
				frame.setVisible(true);
			}
		};
	}

	

	
}
