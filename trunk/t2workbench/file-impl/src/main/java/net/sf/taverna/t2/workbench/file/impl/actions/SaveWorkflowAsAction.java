package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.SaveException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class SaveWorkflowAsAction extends AbstractAction {

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow dataflow = (Dataflow) message.getNewModel();
				updateEnabledStatus(dataflow);
			}
		}
	}

	private static final String[] EXTENSIONS = new String[] { "t2flow" };
	private static final String SAVE_WORKFLOW_AS = "Save workflow as...";

	private static Logger logger = Logger.getLogger(SaveWorkflowAsAction.class);

	private FileManager fileManager = FileManager.getInstance();

	private ModelMap modelMap = ModelMap.getInstance();

	public SaveWorkflowAsAction() {
		super(SAVE_WORKFLOW_AS, WorkbenchIcons.saveIcon);
		modelMap.addObserver(new ModelMapObserver());
		updateEnabledStatus((Dataflow) modelMap
				.getModel(ModelMapConstants.CURRENT_DATAFLOW));
	}

	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		}
		if (fileManager.getCurrentDataflow() == null) {
			JOptionPane.showMessageDialog(parentComponent,
					"No workflow open yet", "No workflow to save",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();

		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(SAVE_WORKFLOW_AS);
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new ExtensionFileFilter(EXTENSIONS));
		fileChooser.setCurrentDirectory(new File(curDir));

		int returnVal = fileChooser.showSaveDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File file = fileChooser.getSelectedFile();
			// TODO: Open in separate thread to avoid hanging UI
			try {
				fileManager.saveCurrentDataflow(file, true);
			} catch (SaveException ex) {
				logger.warn("Could not save workflow to " + file, ex);
				JOptionPane.showMessageDialog(parentComponent,
						"Could not save workflow to " + file + ": \n\n"
								+ ex.getMessage(), "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
			logger.info("Saved current workflow to " + file);
		}
	}

	protected void updateEnabledStatus(Dataflow dataflow) {
		if (dataflow == null) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}

}
