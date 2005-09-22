package net.sf.taverna.dalec.io;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>SequenceWorkflowInput</code> is an implementation of the <code>WorkflowInput</code> interface which sets the
 * input type as raw sequence information.  Whenever a workflow is specified which takes an unannotated sequence as its
 * input, this type of input should be used.  This class takes the raw sequence and constructs a <code>DataThing</code>
 * object from it, also wrapping the processor name and the job ID.
 * <p/>
 * This should be used fro workflows which have two input processors, named "seqID" for the sequenceID (which should be
 * the same as the jobID) and "sequence" for the raw sequence string.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class SequenceWorkflowInput implements WorkflowInput
{
    private static final String SEQUENCE = "sequence";
    private static final String SEQ_ID = "seqID";

    private HashMap inputItems = new HashMap();
    private String jobID;

    public void setJobID(String jobID)
    {
        this.jobID = jobID;
        inputItems.put(SEQ_ID, jobID);
    }

    /**
     * This method is used to set the raw sequence information which is to be used in the workflow.  Any calling class
     * should always set this before calling the <code>getInputs()</code> method, otherwise a NullPointerException will
     * be thrown.
     *
     * @param sequence The string representing the raw sequence to be annotated.  Whitespace, tabs and newline
     *                 characters are not allowed.
     */
    public void setSequence(String sequence)
    {
        inputItems.put(SEQUENCE, sequence);
    }

    public Map getInputMappings()
    {
        return inputItems;
    }

    public String getJobID()
    {
        return jobID;
    }

    public Map getInputs()
    {
        Map input = new HashMap();

        String sequence = (String) inputItems.get(SEQUENCE);
        String seqID = (String) inputItems.get(SEQ_ID);
        if (sequence != null && seqID != null)
        {
            input.put(SEQUENCE, DataThingFactory.bake(sequence));
            input.put(SEQ_ID, DataThingFactory.bake(seqID));
            return input;
        }
        else
        {
            throw new NullPointerException("No sequence has been set");
        }
    }
}
