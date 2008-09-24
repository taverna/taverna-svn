package net.sf.taverna.dalec.io;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * An implementation of <code>WorkflowInput</code> which uses the sequence ID (with respect to the DAS Reference Server)
 * as an input.  This means that the client does not need to pass any sequence information, but can set the sequence ID
 * as the input using the <code>setJobID</code> method.  The job ID field will be used as both the required jobID
 * <i>and</i> the input data.
 * <p/>
 * This should only be used for workflows which have a single input processor named "seqID"
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class SequenceIDWorkflowInput implements WorkflowInput
{
    private String procName;
    private String jobID;

    public void setJobID(String jobID)
    {
        this.procName = "seqID";
        this.jobID = jobID;
    }

    public Map getInputMappings()
    {
        Map inputItems = new HashMap();
        inputItems.put(procName, jobID);
        return inputItems;
    }

    public String getJobID()
    {
        return jobID;
    }

    public Map getInputs()
    {
        Map input = new HashMap();
        if (procName != null && jobID != null)
        {
            input.put(procName, DataThingFactory.bake(jobID));
            return input;
        }
        else
        {
            throw new NullPointerException();
        }
    }
}
