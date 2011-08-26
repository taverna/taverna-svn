/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.util.*;

/**
 * A processor which fails if the test input matches the string 'false'
 * @author Tom Oinn
 */
public class FailIfFalse extends FailIfTrue implements LocalWorker {

    public FailIfFalse() {
	super("false");
    }

}
