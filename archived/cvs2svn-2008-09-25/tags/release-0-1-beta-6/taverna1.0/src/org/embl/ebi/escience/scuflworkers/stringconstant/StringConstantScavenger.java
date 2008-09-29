/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;




/**
 * A Scavenger that knows how to create string constant nodes
 * @author Tom Oinn
 */
public class StringConstantScavenger extends Scavenger {

    /**
     * Create a new String Constant scavenger, the single parameter
     * should is the constant value that any thus created will carry.
     * tscript could be fetched.
     */
    public StringConstantScavenger()
	throws ScavengerCreationException {
	super(new StringConstantProcessorFactory("edit me!"));
    }
}
	
