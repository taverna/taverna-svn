package net.sf.taverna.dalec.io;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>SequenceWorkflowInput</code> is an implementation of the WorkflowInput interface which sets the input type as
 * raw Sequence information.  Whenever a workflow is specified which takes an unannotated sequence as its input, this
 * type of input should be used.  This class takes the raw sequence and constructs a <code>DataThing</code> object from
 * it, also wrapping the processor name and the job ID.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class SequenceWorkflowInput implements WorkflowInput
{
    private String procName;
    private String jobID;
    private String sequence;

    public void setProcessorName(String processorName)
    {
        this.procName = processorName;
    }

    public void setJobID(String jobID)
    {
        this.jobID = jobID;
    }

    /**
     * This method is used to set the raw sequence information which is to be used in the workflow.  Any calling class
     * should always set this before calling the <code>getInput()</code> method, otherwise a NullPointerException will
     * be thrown.
     *
     * @param sequence The string representing the raw sequence to be annotated.  Whitespace, tabs and newline
     *                 characters are not allowed.
     */
    public void setSequenceData(String sequence)
    {
        this.sequence = sequence;
    }

    public String getProcessorName()
    {
        return procName;
    }

    public String getJobID()
    {
        return jobID;
    }

    public Map getInput()
    {
        Map input = new HashMap();
        if (procName == null || sequence == null)
        {
            throw new NullPointerException();
        }
        else
        {
            input.put(procName, DataThingFactory.bake(sequence));
            return input;
        }
    }
}
