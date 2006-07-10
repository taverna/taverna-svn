/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;



/**
 * An editor for the string constant processor, this handles
 * requests to change the string the processor emits.
 * @author Tom Oinn
 */
public class StringConstantEditor implements ProcessorEditor {
    
    public ActionListener getListener(Processor theProcessor) {
	final StringConstantProcessor scp = (StringConstantProcessor)theProcessor;
	return new ActionListener() {
		public void actionPerformed(ActionEvent at) {
		    String newValue = (String)JOptionPane.showInputDialog(null,
									  "String constant",
									  "New value?",
									  JOptionPane.QUESTION_MESSAGE,
									  null,
									  null,
									  scp.getStringValue());
		    if (newValue!=null && newValue.equals(scp.getStringValue())==false) {
			scp.setStringValue(newValue);
		    }
		}
	    };
    }
    
    public String getEditorDescription() {
	return "Edit string value...";
    }

} 
