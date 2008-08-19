package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class SaveAllWorkflowsAction extends AbstractAction {

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			updateEnabled();
		}
	}

	private final SaveWorkflowAction saveWorkflowAction = new SaveWorkflowAction();

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(SaveAllWorkflowsAction.class);

	private static final String SAVE_ALL_WORKFLOWS = "Save all workflows";

	private FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	public SaveAllWorkflowsAction() {
		super(SAVE_ALL_WORKFLOWS, WorkbenchIcons.saveIcon);
		fileManager.addObserver(fileManagerObserver);
		updateEnabled();
	}

	public void updateEnabled() {
		setEnabled(!(fileManager.getOpenDataflows().isEmpty()));
	}

	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component) {
			parentComponent = (Component) ev.getSource();
		}
		saveAllDataflows(parentComponent);
	}

	public void saveAllDataflows(Component parentComponent) {
		// Save in reverse so we save nested workflows first
		List<Dataflow> dataflows = fileManager.getOpenDataflows();
		Collections.reverse(dataflows);

		for (Dataflow dataflow : dataflows) {
			boolean success = saveWorkflowAction.saveDataflow(parentComponent,
					dataflow);
			if (!success) {
				break;
			}
		}
	}

}
