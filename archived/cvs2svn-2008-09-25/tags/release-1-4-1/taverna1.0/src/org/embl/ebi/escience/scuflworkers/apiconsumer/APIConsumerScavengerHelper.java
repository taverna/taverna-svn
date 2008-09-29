/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.prefs.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import java.net.*;

/**
 * Create a new APIConsumerScavenger from a file dialog
 * @author Tom Oinn
 */
public class APIConsumerScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new API Consumer...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(ScuflIcons.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
			fc.setCurrentDirectory(new File(curDir));
			int returnVal = fc.showOpenDialog(s);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    
			    URL url = file.toURL();
			    s.addScavenger(new APIConsumerScavenger(url));
			    
			}
		    }
		    catch (Exception sce) {
			JOptionPane.showMessageDialog
			    (null,
			     "Unable to create scavenger!\n"+sce.getMessage(),
			     "Exception!",
			     JOptionPane.ERROR_MESSAGE);
		    }
		}
	    };
    }


}
