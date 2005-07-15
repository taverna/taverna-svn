package net.sf.taverna.dalec.workflow.io;

import java.util.Map;

/**
 * A generic workflow input interface. Contains some methods which are used to set and return required fields for use in
 * Dalec.  Any implementing class should describe a method to set the data to be used as an input, of the required
 * type.
 *
 * @author Tony Burdett date: 14-Jul-2005
 */
public interface WorkflowInput
{
    /**
     * Set the processor name for this workflow input
     *
     * @param processorName
     */
    public void setProcessorName(String processorName);

    /**
     * Set the jobID for this workflow input, ideally this should be the sequence ID, as this will be the name given to
     * the GFF file stored in the database
     *
     * @param jobID
     */
    public void setJobID(String jobID);

    /**
     * Returns the named input processor
     *
     * @return
     */
    public String getProcessorName ();

    /**
     * Returns the jobID set for this workflow.
     *
     * @return
     */
    public String getJobID();

    /**
     * Returns a Map object containing the required inputs for any workflow - namely, the processor ID and the data to
     * be submitted.
     *
     * @return
     */
    public Map getInput();
}
