/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import java.lang.Class;
import java.lang.ClassNotFoundException;



/**
 * A container for the various icons used by the Scufl ui components
 * @author Tom Oinn
 */
public class ScuflIcons {

    public static ImageIcon wsdlIcon, soaplabIcon, talismanIcon, inputIcon, outputIcon, inputPortIcon, outputPortIcon, dataLinkIcon, constraintIcon, deleteIcon, zoomIcon, wsdlFactoryIcon, soaplabFactoryIcon, talismanFactoryIcon, webIcon, openIcon, runIcon, refreshIcon, workflowIcon, workflowFactoryIcon;

    static {
	// Load the image files found in this package into the class.
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflIcons");
	    wsdlIcon = new ImageIcon(c.getResource("wsdl.gif"));
	    talismanIcon = new ImageIcon(c.getResource("talisman.gif"));
	    soaplabIcon = new ImageIcon(c.getResource("soaplab.gif"));
	    workflowIcon = new ImageIcon(c.getResource("workflow.gif"));
	    wsdlFactoryIcon = wsdlIcon;
	    talismanFactoryIcon = talismanIcon;
	    soaplabFactoryIcon = soaplabIcon;
	    workflowFactoryIcon = workflowIcon;
	    inputPortIcon = new ImageIcon(c.getResource("inputport.gif"));
	    outputPortIcon = new ImageIcon(c.getResource("outputport.gif"));
	    dataLinkIcon = new ImageIcon(c.getResource("datalink.gif"));
	    inputIcon = new ImageIcon(c.getResource("input.gif"));
	    outputIcon = new ImageIcon(c.getResource("output.gif"));
	    constraintIcon = new ImageIcon(c.getResource("constraint.gif"));
	    deleteIcon = new ImageIcon(c.getResource("delete.gif"));
	    zoomIcon = new ImageIcon(c.getResource("zoom.gif"));
	    webIcon = new ImageIcon(c.getResource("web.gif"));
	    openIcon = new ImageIcon(c.getResource("open.gif"));
	    runIcon = new ImageIcon(c.getResource("run.gif"));
	    refreshIcon = new ImageIcon(c.getResource("refresh.gif"));

	}
	catch (ClassNotFoundException cnfe) {
	    //
	}	
    }

}
