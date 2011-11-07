package uk.ac.manchester.cs.wireit.taverna.workflow;

import java.util.List;
import java.util.Map;

/**
 * Interface for extracting the required information from a taverna workflow.
 * 
 * Abstracts away from the actual implementation so that different kinds of workflows t2Flow, scuff2 .. could be handled.
 * Allows the implementation to be an XMLBased hack or proper Taverna code.
 * 
 * @author Christian
 */
public interface TavernaWorkflow {
 
    /**
     * Obtains the workflow name.
     * 
     * @return workflow name.
     */
    public String getWorkflowName();
    
    /**
     * Obtains the expected inputs by name and the corrospondoing depth for each input.
     * 
     * Taverna workflows consider a single value as depth zero, a list as depth 1 ect.
     * Regrettably at the time of writing there was no way to identify the expected inout types from a Taverna workflow.
     * @return Map of input names to their port depth.
     */
    public Map<String,Integer> getInputs();

    /**
     * Obtains the expected outputs by name.
     * 
     * Regrettably at the time of writing there was no way to identify the expected outout types from a Taverna workflow.
     * @return List of output names.
     */
    public List<String> getOutputs();

}
