package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class SaveWorkflowAction extends AbstractAction {

	private final SaveWorkflowAsAction saveWorkflowAsAction = new SaveWorkflowAsAction();

	private static Logger logger = Logger.getLogger(SaveWorkflowAction.class);

	private static final String SAVE_WORKFLOW = "Save workflow";

	private EditManager editManager = EditManager.getInstance();

	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	private FileManager fileManager = FileManager.getInstance();

	private ModelMap modelMap = ModelMap.getInstance();

	private ModelMapObserver modelMapObserver = new ModelMapObserver();

	public SaveWorkflowAction() {
		super(SAVE_WORKFLOW, WorkbenchIcons.saveIcon);
		modelMap.addObserver(modelMapObserver);
		editManager.addObserver(editManagerObserver);
		updateEnabledStatus(fileManager.getCurrentDataflow());
	}

	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component) {
			parentComponent = (Component) ev.getSource();
		}

		Dataflow dataflow = fileManager.getCurrentDataflow();

		saveDataflow(parentComponent, dataflow);

	}

	public boolean saveDataflow(Component parentComponent, Dataflow dataflow) {
		if (!fileManager.canSaveWithoutFilename(dataflow)) {
			return saveWorkflowAsAction.saveDataflow(parentComponent, dataflow);
		}
		try {
			try {
				fileManager.saveDataflow(dataflow, true);
				logger.info("Saved dataflow " + dataflow + " to "
						+ fileManager.getDataflowFile(dataflow));
				return true;
			} catch (OverwriteException ex) {
				File file = fileManager.getDataflowFile(dataflow);
				logger.warn("File was changed on disk: " + file, ex);
				fileManager.setCurrentDataflow(dataflow);
				String msg = "File " + file + " has changed on disk, "
						+ "are you sure you want to overwrite?";
				int ret = JOptionPane.showConfirmDialog(parentComponent, msg,
						"File changed on disk",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					fileManager.saveDataflow(dataflow, false);
					logger.info("Saved dataflow " + dataflow
							+ " by overwriting "
							+ fileManager.getCurrentDataflowFile());
					return true;
				} else if (ret == JOptionPane.NO_OPTION) {
					// Pop up Save As instead to choose another name
					return saveWorkflowAsAction.saveDataflow(parentComponent,
							dataflow);
				} else {
					logger.info("Aborted overwrite of " + file);
					return false;
				}
			}
		} catch (SaveException ex) {
			logger.warn("Could not save dataflow " + dataflow, ex);
			JOptionPane.showMessageDialog(parentComponent,
					"Could not save dataflow: \n\n" + ex.getMessage(),
					"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	protected void updateEnabledStatus(Dataflow dataflow) {
		if (dataflow == null) {
			setEnabled(false);
		} else {
			setEnabled(fileManager.isDataflowChanged(dataflow));
		}
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				Dataflow dataflow = ((AbstractDataflowEditEvent) message)
						.getDataFlow();
				if (dataflow == fileManager.getCurrentDataflow()) {
					updateEnabledStatus(dataflow);
				}
			}
		}
	}

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow dataflow = (Dataflow) message.getNewModel();
				updateEnabledStatus(dataflow);
			}
		}
	}

}
