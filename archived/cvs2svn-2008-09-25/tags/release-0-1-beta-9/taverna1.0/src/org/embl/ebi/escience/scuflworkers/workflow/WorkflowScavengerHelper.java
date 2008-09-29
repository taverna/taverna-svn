/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

import org.embl.ebi.escience.scuflworkers.workflow.WorkflowScavenger;
import java.lang.String;



/**
 * Helper for handling Workflow scavengers.
 * @author Tom Oinn
 */
public class WorkflowScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Workflow scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		 public void actionPerformed(ActionEvent ae) {
		     String definitionURL = (String)JOptionPane.showInputDialog(null,
										"Address of the XScufl document?",
										"XScufl location",
										JOptionPane.QUESTION_MESSAGE,
										null,
										null,
										"http://");
		     if (definitionURL!=null) {
			 try {
			     s.addScavenger(new WorkflowScavenger(definitionURL));					
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
