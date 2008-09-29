/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.UnknownProcessorException;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

// Utility Imports
import java.util.Map;

import java.lang.String;

/**
 * This interface is implemented by any returned object that
 * represents the state of a running workflow instance within
 * an enactment system. It includes methods to pause and resume
 * the workflow, get the status and any result documents that
 * may be available, get intermediate results and other state
 * querying operations. It is intended to act as an abstraction
 * over classes such as the TavernaFlowReceipt.
 * @author Tom Oinn
 */
public interface WorkflowInstance {
    
   /**
    * Start this workflow istance running.
    * @throw InvalidInputException if the input is doesn't map to source in the dataflow.
    */
    public void run() throws InvalidInputException;
   
   /**
    * This method can be used to get a simple String that describes
    * the current state of this workflow instance object.
    * The possible values for the return value are enumerated in 
    * <code>FlowStateDefinition</code>.
    * @return simple string to that describes the workflow status 
    * @see uk.ac.soton.itinnovation.freefluo.main.FlowStateDefinition
    */
    public String getStatus();
    
    /**
     * Return an array of Maps containing intermediate results
     * for the named processor. The array is always exactly two 
     * items long, and each item is a Map containing scufl port
     * names as keys and DataThing objects as the values present
     * on those ports. The Map at position 0 in the array contains
     * the input values to the processor, that at position 1
     * the output values. If the processor has no output or input
     * values populated then these maps will be empty but will
     * still be returned.
     * @exception UnknownProcessorException thrown if the named
     * processor is not present in the ScuflModel that this workflow
     * instance is derived from.
     */
    public Map[] getIntermediateResultsForProcessor(String processorName)
	throws UnknownProcessorException;

    /**
     * Return the XML string of the status document
     * @return XML progress report document
     */
    public String getProgressReportXMLString();
    
    /**
     * Return the XML document containing all the set of results
     * this is in the form of the baclava data model defined by
     * the org.embl.ebi.escience.baclava package. Any outputs that
     * have not yet been populated with data will not appear in
     * the output document.
     * @return XML output document
     */
    public String getOutputXMLString();

    /**
     * Return the Map of DataThing objects for all known workflow
     * outputs, keys are the output names and values are the
     * DataThing objects.
     * @return Map of DataThing objects
     */
    public Map getOutput();
    
    /**
     * Returns a human readable string containing details of errors 
     * that occurred during execution of this <code>WorkflowInstance</code>
     */
    public String getErrorMessage();
    
    /**
     * Return the XML string containing the provenance report,
     * this document is currently poorly defined but will in
     * the future contain the set of RDF statements generated
     * by the knowledge collection code.
     * @return XML provenance doument
     */
    public String getProvenanceXMLString();
    
    /**
     * Pause the workflow enactment. This consists of setting the
     * paused boolean flag, then cancelling all running workflow
     * processes. Be aware therefore that calling this is not always
     * safe as it may interrupt processes that maintain some kind
     * of external state.
     * @return boolean value true if the workflow is still running,
     * false if it is now paused
     */
    public boolean pauseExecution();
    
    /**
     * Resume the workflow enactment.
     * @return boolean value true if the workflow is now running,
     * false if paused
     */
    public boolean resumeExecution();

    /**
     * Return whether the workflow is currently paused
     * @return boolean true if the workflow is paused
     */
    public boolean isPaused(); 
 
    /** 
     * Cancel the currently running workflow, freeing
     * any resources used
     */
    public void cancel();
    
    /** 
     * Set workflow inputs. Takes a Map of DataThing objects,
     * with the keys in the map being String objects corresponding
     * to the named workflow inputs within the workflow that
     * this instance represents the state of.
     */
    public void setInputs(Map inputMap);

    /**
     * Signal that any allocated resources should be cleaned up because 
     * the workflow instance is no longer required.
     */
    public void destroy();
}
