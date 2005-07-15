package net.sf.taverna.dalec.workflow.io;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 14-Jul-2005
 */
public class SequenceIDWorkflowInput implements WorkflowInput
{
    private String procName;
    private String jobID;

    public void setProcessorName(String processorName)
    {
        this.procName = processorName;
    }

    public void setJobID(String jobID)
    {
        this.jobID = jobID;
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
        if (procName == null || jobID == null)
        {
            throw new NullPointerException();
        }
        else
        {
            input.put(procName, DataThingFactory.bake(jobID));
            return input;
        }
    }
}
