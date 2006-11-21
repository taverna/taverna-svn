/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

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
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @author David Withers
 * @version $Revision$
 */
public class SaveWorkflowAction extends AbstractAction {
	private JFileChooser fc = new JFileChooser();

	/**
	 * @param model
	 */
	public SaveWorkflowAction() {
		putValue(SMALL_ICON, TavernaIcons.saveIcon);
		putValue(NAME, "Save Workflow...");
		putValue(SHORT_DESCRIPTION, "Saves the current workflow to a file");
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// Save to XScufl
		try {
			saveToFile();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Problem saving workflow : \n"
					+ ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		}

	}

	/*
	 * Prompts for a file, and then saves the workflow to that file.
	 */
	protected void saveToFile() throws Exception {
		ScuflModel currentModel = (ScuflModel) ModelMap.getInstance()
				.getNamedModel(ModelMap.CURRENT_WORKFLOW);

		if (currentModel != null) {
			Preferences prefs = Preferences
					.userNodeForPackage(SaveWorkflowAction.class);
			String curDir = prefs.get("currentDir", System
					.getProperty("user.home"));
			fc.setDialogTitle("Save Workflow - "
					+ currentModel.getDescription().getTitle());
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
			fc.setCurrentDirectory(new File(curDir));
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				File file = fc.getSelectedFile();
				if (file.getName().endsWith(".xml") == false) {
					file = new File(file.toURI().resolve(
							file.getName() + ".xml"));
				}
				OutputStreamWriter writer = new OutputStreamWriter(
						new FileOutputStream(file), Charset.forName("UTF-8"));
				PrintWriter out = new PrintWriter(writer);
				out.print(XScuflView.getXMLText(currentModel));
				out.flush();
				out.close();
			}
		}
	}
}
