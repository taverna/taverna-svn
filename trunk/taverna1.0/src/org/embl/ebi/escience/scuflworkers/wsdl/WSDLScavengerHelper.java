/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Helper for handling WSDL scavengers.
 * @author Tom Oinn
 */
public class WSDLScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new WSDL scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		 public void actionPerformed(ActionEvent ae) {
		     String wsdlLocation = (String)JOptionPane.showInputDialog(null,
									       "Address of the WSDL document?",
									       "WSDL location",
									       JOptionPane.QUESTION_MESSAGE,
									       null,
									       null,
									       "http://");
		     if (wsdlLocation!=null) {
			 try {
			     s.addScavenger(new WSDLBasedScavenger(wsdlLocation));					
			 }
			 catch (ScavengerCreationException sce) {
			     JOptionPane.showMessageDialog(null,
							   "Unable to create scavenger!\n"+sce.getMessage(),
							   "Exception!",
							   JOptionPane.ERROR_MESSAGE);
			 }
		     }	
		 }
	    };
    }
    
}
