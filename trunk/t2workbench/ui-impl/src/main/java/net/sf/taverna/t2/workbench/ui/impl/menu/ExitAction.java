package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

/**
 * Exit the workbench
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class ExitAction extends AbstractMenuAction {

	public ExitAction() {
		super(URI
				.create("http://taverna.sf.net/2008/t2workbench/menu#file"), 10000);
	}

	@Override
	public Action getAction() {
		return new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				Workbench.getInstance().exit();
			}
		};
	}

}
