/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.WorkflowChanges;

/**
 * Action and static methods for saving a workflow or workflow components.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @author David Withers
 * @author Stian Soiland
 */
@SuppressWarnings("serial")
public class SaveWorkflowAction extends AbstractAction {
	private static WorkflowChanges workflowChanges = WorkflowChanges
			.getInstance();

	private static Logger logger = Logger.getLogger(SaveWorkflowAction.class);

	private Component parentComponent;

	public static final String EXTENSION = "xml";

	public SaveWorkflowAction(Component parentComponent) {
		putValue(SMALL_ICON, TavernaIcons.saveIcon);
		putValue(NAME, "Save workflow...");
		putValue(SHORT_DESCRIPTION, "Saves the current workflow to a file");
		this.parentComponent = parentComponent;
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// Save to XScufl
		try {
			saveToFile();
		} catch (Exception ex) {
			logger.warn("Could not save the workflow", ex);
			JOptionPane.showMessageDialog(parentComponent,
					"Problem saving workflow : \n" + ex.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Prompt for a file name, and then save the current workflow to the file.
	 * 
	 * @return true if file was saved, or false if the action was cancelled
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkWrite</code>
	 *             method denies write access to the file.
	 */
	public boolean saveToFile() throws FileNotFoundException, SecurityException {
		ScuflModel currentModel = (ScuflModel) ModelMap.getInstance()
				.getNamedModel(ModelMap.CURRENT_WORKFLOW);
		if (currentModel == null) {
			logger.warn("Can't save null model");
			return false;
		}
		return saveToFile(parentComponent, currentModel);
	}

	/**
	 * Prompt for a file name, and then save the given workflow to that file.
	 * This static version of saveToFile()is intended for non-action invocation
	 * of file saving, such as through a "Do you want to save?" dialogue when
	 * closing Taverna.
	 * 
	 * @see saveToFile()
	 * @param parentComponent
	 *            Parent component for dialogue window
	 * @param model
	 *            Workflow to save
	 * @return true if file was saved, or false if the action was cancelled
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkWrite</code>
	 *             method denies write access to the file.
	 */
	public static boolean saveToFile(Component parentComponent, ScuflModel model) {
		// Make sure it is visible first so the user knows what he will save
		ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, model);

		File file = saveDialogue(parentComponent, model, EXTENSION,
				"Save workflow");
		if (file == null) {
			return false;
		}
		try {
			saveToFile(model, file);
			return true;
		} catch (SecurityException ex) {
			logger.warn("Not allowed to save workflow to " + file, ex);
			JOptionPane.showMessageDialog(parentComponent,
					"Not allowed to save workflow to :" + file + "\n"
							+ ex.getMessage(), "Permission error!",
					JOptionPane.ERROR_MESSAGE);
		} catch (FileNotFoundException ex) {
			logger.warn("Could not save the workflow to " + file, ex);
			JOptionPane.showMessageDialog(parentComponent,
					"Problem saving workflow to :" + file + "\n"
							+ ex.getMessage(), "Error!",
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	/**
	 * Save the given workflow to the given file. The filename will be marked as
	 * saved to the given file, and that file will be suggested in future save
	 * dialogues.
	 * 
	 * @param model
	 *            Workflow to save
	 * @param file
	 *            Abstract file of where to save the workflow
	 * 
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkWrite</code>
	 *             method denies write access to the file.
	 */
	public static void saveToFile(ScuflModel model, File file)
			throws FileNotFoundException, SecurityException {
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(file), Charset.forName("UTF-8"));
		PrintWriter out = new PrintWriter(writer);
		out.print(XScuflView.getXMLText(model));
		out.flush();
		out.close();
		logger.info("Saved " + model + " to " + file);
		// FIXME: Safe to syncedWithFile() here? 
		workflowChanges.syncedWithFile(model, file);
	}

	/**
	 * Pop up a save dialogue relating to the given workflow. This general
	 * static method can be used for example for saving the workflow diagram as
	 * .png, and will use the existing workflow title as a base for suggesting a
	 * filename.
	 * 
	 * @param parentComponent
	 *            Parent component for dialogue window
	 * @param model
	 *            Workflow to save
	 * @param extension
	 *            Extension for filename, such as "jpg"
	 * @param windowTitle
	 *            Title for dialogue box, such as "Save workflow diagram"
	 * @return File instance for the selected abstract filename, or null if the
	 *         dialogue was cancelled.
	 */
	public static File saveDialogue(Component parentComponent,
			ScuflModel model, String extension, String windowTitle) {
		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences
				.userNodeForPackage(SaveWorkflowAction.class);

		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		String title = model.getDescription().getTitle();
		fc.setDialogTitle(windowTitle + ": " + title);
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { extension }));
		File lastSavedAs = workflowChanges.lastFilename(model);
		if (lastSavedAs != null) {
			// Suggest last saved/load filename (but with desired extension)
			fc.setSelectedFile(fixExtension(lastSavedAs, extension));
		} else if (title.startsWith(WorkflowDescription.DEFAULT_TITLE)) {
			// No file suggestion (useless title), just the directory
			fc.setCurrentDirectory(new File(curDir));
		} else {
			// Suggest a filename from the title
			fc.setSelectedFile(new File(curDir, title + "." + extension));
		}
		int returnVal = fc.showSaveDialog(parentComponent);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			logger.info("Aborting save of " + title);
			return null;
		}
		File file = fixExtension(fc.getSelectedFile(), extension);
		logger.debug("Selected " + file + " for save");
		prefs.put("currentDir", fc.getCurrentDirectory().toString());
		// FIXME: Should do "Do you want to overwrite?" if user selected an
		// existing file that was not already set from lastSavedAs
		
		if (title.startsWith(WorkflowDescription.DEFAULT_TITLE)) {
			// Set the title to the chosen filename (without extension),
			// should be more reasonable than "Untitled workflow #34"
			String name = file.getName().replace("." + extension, "");
			model.getDescription().setTitle(name);
		}
		return file;
	}

	/**
	 * Make sure given File has given extension. If it has the wrong extension
	 * or no extension, a new File instance will be returned. Otherwise, the
	 * passed instance is returned unchanged.
	 * 
	 * @param file
	 *            File which extension is to be checked
	 * @param extension
	 *            Extension desired, example: "xml"
	 * @return file parameter if the extension was OK, or a new File instance
	 *         with the correct extension
	 */
	private static File fixExtension(File file, String extension) {
		if (file.getName().endsWith("." + extension)) {
			return file;
		}
		// Remove any existing extension
		String name = file.getName().split("\\.", 2)[0];
		return new File(file.getParent(), name + "." + extension);
	}
}
