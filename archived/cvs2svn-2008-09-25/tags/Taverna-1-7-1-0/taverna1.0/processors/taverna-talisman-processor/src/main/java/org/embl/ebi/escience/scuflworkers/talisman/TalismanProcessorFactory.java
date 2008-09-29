/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;



/**
 * Implementation of ProcessorFactory that creates
 * TalismanProcessor nodes
 * @author Tom Oinn
 */
public class TalismanProcessorFactory extends ProcessorFactory {

    private String scriptURL;

    /**
     * Create a new factory configured with the specified
     * script URL
     */
    public TalismanProcessorFactory(String scriptURL) {
	this.scriptURL = scriptURL;
	String[] parts = scriptURL.split("/");
	setName(parts[parts.length - 1]);
    }
    
    /**
     * Get the script URL
     */
    public String getTScriptURL() {
	return this.scriptURL;
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
    	return TalismanProcessor.class;
    }
    
}
