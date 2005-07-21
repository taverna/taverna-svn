package net.sf.taverna.dalec.io;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Javadocs go here.
 *
 * @version 1.0
 * @author Tony Burdett
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
