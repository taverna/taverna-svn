/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.TalismanProcessor;

import org.embl.ebi.escience.scuflui.workbench.ProcessorFactory;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * TalismanProcessor nodes
 * @author Tom Oinn
 */
public class TalismanProcessorFactory implements ProcessorFactory {

    private String scriptURL;

    /**
     * Create a new factory configured with the specified
     * script URL
     */
    public TalismanProcessorFactory(String scriptURL) {
	this.scriptURL = scriptURL;
    }
    
    /**
     * Return the leaf of the path as the factory name
     */
    public String toString() {
	String[] parts = scriptURL.split("/");
	return parts[parts.length - 1];
    }
    
    /**
     * Create a new SoaplabProcessor and add it to the model
     */
    public void createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new TalismanProcessor(model, name, this.scriptURL);
	model.addProcessor(theProcessor);
    }
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor based on Talisman, using the tscript at "+this.scriptURL;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scufl.TalismanProcessor.class;
    }
    
}
