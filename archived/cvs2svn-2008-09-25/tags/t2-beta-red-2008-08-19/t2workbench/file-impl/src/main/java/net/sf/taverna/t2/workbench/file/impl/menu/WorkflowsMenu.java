package net.sf.taverna.t2.workbench.file.impl.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.AbstractDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class WorkflowsMenu extends AbstractMenuCustom {

	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	private JMenu workflowsMenu;

	public WorkflowsMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 900);
		fileManager.addObserver(fileManagerObserver);
		editManager.addObserver(editManagerObserver);
	}

	@Override
	protected Component createCustomComponent() {
		workflowsMenu = new JMenu("Workflows");
		updateWorkflowsMenu();
		return workflowsMenu;
	}

	public void updateWorkflowsMenu() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateWorkflowsMenuUI();
			}
		});
	}

	protected void updateWorkflowsMenuUI() {
		workflowsMenu.removeAll();
		ButtonGroup workflowsGroup = new ButtonGroup();

		int i = 0;
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		for (final Dataflow dataflow : fileManager.getOpenDataflows()) {
			String name = dataflow.getLocalName();
			if (fileManager.isDataflowChanged(dataflow)) {
				name = "*" + name;
			}
			// A counter
			name = ++i + " " + name;

			SwitchWorkflowAction switchWorkflowAction = new SwitchWorkflowAction(
					name, dataflow);
			JRadioButtonMenuItem switchWorkflowMenuItem = new JRadioButtonMenuItem(
					switchWorkflowAction);
			workflowsGroup.add(switchWorkflowMenuItem);
			if (dataflow.equals(currentDataflow)) {
				switchWorkflowMenuItem.setSelected(true);
			}
			workflowsMenu.add(switchWorkflowMenuItem);
		}
		if (i == 0) {
			workflowsMenu.add(new NoWorkflowsOpen());
		}

		workflowsMenu.revalidate();
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				updateWorkflowsMenu();
			}
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
}
