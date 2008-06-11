package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.SaveException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class SaveWorkflowAction extends AbstractAction {

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow dataflow = (Dataflow) message.getNewModel();
				updateEnabledStatus(dataflow);
			}
		}
	}

	private static final String SAVE_WORKFLOW = "Save workflow";

	private static Logger logger = Logger.getLogger(SaveWorkflowAction.class);

	private FileManager fileManager = FileManager.getInstance();

	private ModelMap modelMap = ModelMap.getInstance();

	public SaveWorkflowAction() {
		super(SAVE_WORKFLOW, WorkbenchIcons.saveIcon);
		modelMap.addObserver(new ModelMapObserver());
		updateEnabledStatus((Dataflow) modelMap
				.getModel(ModelMapConstants.CURRENT_DATAFLOW));
	}

	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component) {
			parentComponent = (Component) ev.getSource();
		}
		if (!fileManager.canSaveCurrentWithoutFilename()) {
			new SaveWorkflowAsAction().actionPerformed(ev);
		}
		try {
			fileManager.saveCurrentDataflow(true);
		} catch (SaveException ex) {
			logger.warn("Could not save current workflow", ex);
			JOptionPane.showMessageDialog(parentComponent,
					"Could not save dataflow: \n\n"
							+ ex.getMessage(), "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	protected void updateEnabledStatus(Dataflow dataflow) {
		if (dataflow == null) {
			setEnabled(false);
		} else {
			setEnabled(fileManager.isDataflowChanged(dataflow));
			// TODO: Also update setEnabled by listening to the FileManager /
			// EditManager
		}
	}

}
