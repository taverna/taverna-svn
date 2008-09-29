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
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import java.net.URI;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.3 $
 */
public class SaveWorkflowAction extends ScuflModelAction
{
    final JFileChooser fc = new JFileChooser();
	
	/**
	 * @param model
	 */
	public SaveWorkflowAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.saveIcon);
		putValue(NAME, "Save");
		putValue(SHORT_DESCRIPTION, "Save this workflow...");		
	}

	/*
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
	    // Save to XScufl
	    try {
		Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Save Workflow");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    prefs.put("currentDir", fc.getCurrentDirectory().toString());
		    File file = fc.getSelectedFile();
		    if (file.getName().endsWith(".xml") == false) {
			file = new File(file.toURI().resolve(file.getName()+".xml"));
		    }
		    XScuflView xsv = new XScuflView(model);
		    PrintWriter out = new PrintWriter(new FileWriter(file));
		    out.println(xsv.getXMLText());
		    model.removeListener(xsv);
		    out.flush();
		    out.close();
		}
	    }
	    catch (Exception ex) {
		JOptionPane.showMessageDialog(null,
					      "Problem saving workflow : \n"+ex.getMessage(),
					      "Error!",
					      JOptionPane.ERROR_MESSAGE);
	    }
	}
}
