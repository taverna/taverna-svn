package net.sf.taverna.t2.workbench.file.impl.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.AbstractDataflowEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class WorkflowsMenu extends AbstractMenuCustom {

	private FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();
	private JMenu workflowsMenu;

	private final class NoWorkflowsOpen extends AbstractAction {
		private NoWorkflowsOpen() {
			super("No workflows open");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			// No-op
		}
	}

	private final class SwitchWorkflowAction extends AbstractAction {
		private final Dataflow dataflow;

		private SwitchWorkflowAction(String name, Dataflow dataflow) {
			super(name);
			this.dataflow = dataflow;
		}

		public void actionPerformed(ActionEvent e) {
			fileManager.setCurrentDataflow(dataflow);
		}
	}

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEvent) {
				updateWorkflowsMenu();
				// TODO: Don't rebuild whole menu
			}
		}
	}

	public WorkflowsMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 900);
		fileManager.addObserver(fileManagerObserver);
	}

	@Override
	protected Component createCustomComponent() {
		workflowsMenu = new JMenu("Workflows");
		updateWorkflowsMenu();
		return workflowsMenu;
	}

	protected void updateWorkflowsMenu() {
		workflowsMenu.removeAll();
		int i = 0;
		for (final Dataflow dataflow : fileManager.getOpenDataflows()) {
			String name = ++i + " " + dataflow.getLocalName();
			workflowsMenu.add(new SwitchWorkflowAction(name, dataflow));
		}
		if (i == 0) {
			workflowsMenu.add(new NoWorkflowsOpen());
		}
		
		workflowsMenu.revalidate();
	}
}
