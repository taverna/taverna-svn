/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.baclava.DataThing;
import java.util.Map;
import java.util.Iterator;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import java.io.File;
import java.io.IOException;


/**
 * Store the Map of DataThing objects to disk, using the
 * collection structure to drive the directory structure
 * and storing each leaf DataThing item as a single file
 * @author Tom Oinn
 */
public class SaveToFileSystem implements ResultMapSaveSPI {

    /**
     * Return the standard looking save to disk icon
     */
    public Icon getIcon() {
	return ScuflIcons.saveIcon;
    }

    /**
     * Get the description for this plugin
     */
    public String getDescription() {
	return ( "Saves the complete set of results to the file system,\n"+
		 "writing each result into its own file or set of directories\n"+
		 "in the case of collections, the directory structure\n"+
		 "mirroring that of the collection and leaf nodes being\n"+
		 "allocated numbers as names starting at zero and incrementing.");
    }

    /**
     * Return the name for this plugin
     */
    public String getName() {
	return "Save to disk";
    }
    
    /**
     * Show a standard save dialog and dump the results to disk
     */
    public ActionListener getListener(Map results, JComponent parent) {
	final Map resultMap = results;
	final JComponent parentComponent = parent;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser jfc = new JFileChooser();
		    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    int returnVal = jfc.showSaveDialog(parentComponent);
		    for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultValue = (DataThing) resultMap.get(resultName);
			try {
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = jfc.getSelectedFile();
				String name = resultName;
				resultValue.writeToFileSystem(f, name);
			    }
			} catch (IOException ioe) {
			    JOptionPane.showMessageDialog(parentComponent,
							  "Problem saving results : \n"+ioe.getMessage(),
							  "Error!",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    };
    }

}
