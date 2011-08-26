/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Store the entire Map of DataThing objects to disk
 * as a single XML data document.
 * @author Tom Oinn
 */
public class SaveAsDataThingDocument implements ResultMapSaveSPI {

    /**
     * Return the standard looking save to disk icon
     */
    public Icon getIcon() {
	return TavernaIcons.xmlNodeIcon;
    }

    /**
     * Get the description for this plugin
     */
    public String getDescription() {
	return ( "Saves the set of results in native Baclava XML." );
    }

    /**
     * Get the short name
     */
    public String getName() {
	return "Save as XML";
    }

    /**
     * Show a standard save dialog and dump the entire result
     * set to the specified XML file
     */
    public ActionListener getListener(Map results, JComponent parent) {
	final Map resultMap = results;
	final JComponent parentComponent = parent;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			JFileChooser jfc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			jfc.resetChoosableFileFilters();
			jfc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
			jfc.setCurrentDirectory(new File(curDir));
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = jfc.showSaveDialog(parentComponent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    // Build the string containing the XML
			    // document from the datathing map
			    Document doc = DataThingXMLFactory.getDataDocument(resultMap);
			    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			    String xmlString = xo.outputString(doc);
			    File f = jfc.getSelectedFile();
			    PrintWriter out = new PrintWriter(new FileWriter(f));
			    out.print(xmlString);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(parentComponent,
						      "Problem saving results : \n"+ex.getMessage(),
						      "Error!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    };
    }

}
