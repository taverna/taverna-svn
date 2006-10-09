/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision$
 */
public class SaveWorkflowAction extends ScuflModelAction {
	final JFileChooser fc = new JFileChooser();
	
	/**
	 * @param model
	 */
	public SaveWorkflowAction(ScuflModel model) {
		super(model);
		boolean jdbcStore = false;
		if (jdbcStore) {
			putValue(SMALL_ICON, TavernaIcons.saveMenuIcon);
		} else {
			putValue(SMALL_ICON, TavernaIcons.saveIcon);
		}
		putValue(NAME, "Save");
		putValue(SHORT_DESCRIPTION, "Save this workflow...");
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
		
		Preferences prefs = Preferences
		.userNodeForPackage(SaveWorkflowAction.class);
		String curDir = prefs
		.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Save Workflow");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			File file = fc.getSelectedFile();
			if (file.getName().endsWith(".xml") == false) {
				file = new File(file.toURI().resolve(file.getName() + ".xml"));
			}	
			PrintWriter out = new PrintWriter(new FileWriter(file));
			out.print(XScuflView.getXMLText(model));			
			out.flush();
			out.close();
		}
		
	}
}
