/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.WorkflowChanges;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @author David Withers
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class SaveWorkflowAction extends AbstractAction {
	private JFileChooser fc = new JFileChooser();
	private WorkflowChanges workflowChanges = WorkflowChanges.getInstance();
	private Component parentComponent;
	public final String EXTENSION = "xml";

	
	public SaveWorkflowAction(Component parentComponent) {
		putValue(SMALL_ICON, TavernaIcons.saveIcon);
		putValue(NAME, "Save workflow ...");
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
			JOptionPane.showMessageDialog(parentComponent, "Problem saving workflow : \n"
					+ ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		}

	}

	/*
	 * Prompts for a file, and then saves the workflow to that file. 
	 * Return true if file was saved, or false if the action was cancelled.
	 */
	public boolean saveToFile() throws Exception {
		ScuflModel currentModel = (ScuflModel) ModelMap.getInstance()
				.getNamedModel(ModelMap.CURRENT_WORKFLOW);
		if (currentModel == null) {
			return false;
		}
		return saveToFile(currentModel);
	}
	
	/*
	 * Prompts for a file, and then saves the workflow to that file. 
	 * Return true if file was saved, or false if the action was cancelled.
	 */
	public boolean saveToFile(ScuflModel model) throws Exception {
		// Make sure it is visible first so the user knows what he will save
		ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, model);
		
		Preferences prefs = Preferences.userNodeForPackage(SaveWorkflowAction.class);

		String curDir = prefs.get("currentDir", System
				.getProperty("user.home"));
		fc.setDialogTitle("Save Workflow - "
				+ model.getDescription().getTitle());
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { EXTENSION }));
		File lastSavedAs = workflowChanges.lastFilename(model);
		String title = model.getDescription().getTitle();
		if (lastSavedAs != null) {
			fc.setSelectedFile(lastSavedAs);
		} else {
			if (! title.startsWith(WorkflowDescription.DEFAULT_TITLE)) {
				// Suggest a filename from the title
				fc.setSelectedFile(new File(curDir, 
						model.getDescription().getTitle()));
			} else {
				// No selection, just the directory
				fc.setCurrentDirectory(new File(curDir));
			}
		}
		int returnVal = fc.showSaveDialog(parentComponent);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		prefs.put("currentDir", fc.getCurrentDirectory().toString());
		File file = fc.getSelectedFile();
		if (! file.getName().endsWith("." + EXTENSION)) {
			file = new File(file.toURI().resolve(
					file.getName() + "." + EXTENSION));
		}
		if (title.startsWith(WorkflowDescription.DEFAULT_TITLE)) {
			// Set the title to the chosen filename, should be more
			// reasonable than "Untitled workflow #34"
			title = file.getName().replace("." + EXTENSION, "");
			model.getDescription().setTitle(title);
		}
		
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(file), Charset.forName("UTF-8"));
		PrintWriter out = new PrintWriter(writer);
		out.print(XScuflView.getXMLText(model));
		out.flush();
		out.close();
		workflowChanges.syncedWithFile(model, file);
		return true;
	}
}
