/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;




/**
 * Implementing classes are capable of creating
 * a new processor and attaching it to a model
 * when supplied with the new processor name and
 * a reference to the model. The intention is that
 * service scavengers should create an implementation
 * of this for each service they find and that these
 * should then be used as the user objects inside
 * a default tree model to allow simple service selection
 * and addition to a ScuflModel
 * @author Tom Oinn
 */
public interface ProcessorFactory {

    /**
     * Instantiate a new processor with the given
     * name and add it to the specified ScuflModel
     */
    public void createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException;

    /**
     * Describe the processor that would be created
     * by this factory were the createProcessor method
     * invoked
     */
    public String getProcessorDescription();

    /**
     * Return the Class object describing the processor
     * this factory would build
     */
    public Class getProcessorClass();

}
