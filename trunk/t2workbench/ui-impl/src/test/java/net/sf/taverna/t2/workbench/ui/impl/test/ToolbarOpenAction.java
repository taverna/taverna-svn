package net.sf.taverna.t2.workbench.ui.impl.test;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

public class ToolbarOpenAction extends AbstractMenuAction {
	public ToolbarOpenAction() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR, 20);
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
