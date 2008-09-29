/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Create a new APIConsumerScavenger from a file dialog
 * @author Tom Oinn
 */
public class APIConsumerScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(APIConsumerScavengerHelper.class);
	
    public String getScavengerDescription() {
	return "Add new API Consumer...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(TavernaIcons.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
			fc.setCurrentDirectory(new File(curDir));
			int returnVal = fc.showOpenDialog(s.getContainingFrame());
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

	public Set<Scavenger> getDefaults() {
		// Find all apiconsumer.xml files in the classpath root and
		// load them as API Consumer scavengers
		Set <Scavenger> result=new HashSet<Scavenger>();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Enumeration en = loader.getResources("apiconsumer.xml");
			while (en.hasMoreElements()) {
				URL resourceURL = (URL) en.nextElement();
				result.add(new APIConsumerScavenger(resourceURL));
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		return result;
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		return new HashSet<Scavenger>();
	}
	
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new APIConsumerProcessorInfoBean().icon();
	}
    
    


}
