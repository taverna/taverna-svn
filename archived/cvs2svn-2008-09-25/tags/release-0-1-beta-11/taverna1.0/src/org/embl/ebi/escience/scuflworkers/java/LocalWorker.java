/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.Map;

import java.lang.String;



/**
 * Implemented by classes acting as local services and which don't
 * require the full invocation infrastructure.
 * @author Tom Oinn
 */
public interface LocalWorker {
    
    /** Type string for a string object */
    public static final String STRING = "'text/plain'";
    /** Type string for an array of string objects */
    public static final String STRING_ARRAY = "l('text/plain')";
    /** Type string for a PNG image */
    public static final String PNG_IMAGE = "'application/octet-stream,image/png'";
    /** Type string for an array of PNG images */
    public static final String PNG_IMAGE_ARRAY = "l('application/octet-stream,image/png')";
    /** Type string for a document in HTML format */
    public static final String HTML = "'text/plain,text/html'";
    /** Type string for an array of documents in HTML format */
    public static final String HTML_ARRAY = "l('text/plain,text/html'";
    /** Type string for arbitrary binary data */
    public static final String BINARY = "'application/octet-stream'";
    /** Type string for an array of arbitrary binary data blocks */
    public static final String BINARY_ARRAY = "l('application/octet-stream')";
    /** Type string for a single item of unknown type */
    public static final String UNTYPED = "''";
    /** Type string for an array of items of unknown type */
    public static final String UNTYPED_ARRAY = "l('')";
    
    /**
     * Given a Map of DataThing objects as input, invoke the
     * underlying logic of the task and return a map of DataThing
     * objects as outputs, with the port names acting as keys
     * in the map in both cases.
     * @exception TaskExecutionException thrown if there is
     * an error during invocation of the task.
     */
    public Map execute(Map inputMap) throws TaskExecutionException;
    
    /**
     * Get an array of the names of input ports for this processor
     */

    public String[] inputNames();
    /**
     * Get an array of the string types for the inputs defined by
     * the inputNames() method, these should probably use the 
     * constants defined in this interface but may use any valid
     * Baclava data type specifier.
     */
    public String[] inputTypes();

    /**
     * Names of the output ports
     */
    public String[] outputNames();
    
    /**
     * Types of the output ports
     */
    public String[] outputTypes();

}
