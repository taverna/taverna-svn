package org.embl.ebi.escience.testhelpers.acceptance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;

/**
 * Test case that provides the ability to more easily test workflows. 
 */

public class WorkflowTestCase extends AcceptanceTestCase 
{			

	private String progressReportXMLString;
	
	
	
	public void setUp() {
		progressReportXMLString="";
		super.setUp();
	}	
	
	/**
	 * Provides access to the Progress Report, after executeWorkflow has been invoked.
	 * @return
	 */
	public String getProgressReportXMLString()
	{
		return progressReportXMLString;
	}

	/**
	 * Executes a given workflow and returns the output Map.
	 * Workflow is identified by workflow, which relates to a directory in /test/data/workflows, and within there an xml file with the same
	 * name (appended with .xml). i.e. /test/data/workflows/<workflow>/<workflow>.xml
	 * listener is the WorkflowEventListener that is applied to WorkflowEventDispatcher.DISPATCHER. A default listener is available from defaultWorkflowListener
	 * @param workflow
	 * @param inputs
	 * @param listener
	 * @return Map holding the outputs resulting from executing the workflow
	 * @throws Exception
	 */

	protected Map executeWorkflow(String workflow,Map inputs,WorkflowEventListener listener) throws Exception
	{		
		WorkflowLauncher launcher = new WorkflowLauncher(new URL("file:"+workflowFilename(workflow)));
		
		
		Map outputs=launcher.execute(inputs,defaultWorkflowEventListener());
		progressReportXMLString=launcher.getProgressReportXML();
		
		return outputs;        						
	}				
	
	protected String workflowFilename(String workflow) throws Exception
	{
		String home = System.getProperty("taverna.home");
		if (home==null) 
		{
			throw new Exception("Unable to open workflow - taverna.home is not set.");
		}
		return (home + "/test/data/workflows/"+workflow+"/"+workflow+".xml");
	}
	
	/**
	 * Provides a simple WorkflowEventListener. Should a WorkflowFailureEvent take place, this is recorded via the AcceptanceAssertions.fail method.
	 * @return
	 */
	protected WorkflowEventListener defaultWorkflowEventListener()
	{
		return new WorkflowEventAdapter() {
            public void workflowFailed(WorkflowFailureEvent e) 
            {                
                    fail("workflow failure event occurred: "+e.toString());                
            }
            
            public void dataChanged(UserChangedDataEvent e) {}
        };
	}
	
	/**
	 * Executes the workflow using the given inputs, and compares any outputs with the expectedOutputs.
	 * Any difference will be recorded as an error.
	 * @param workflow
	 * @param inputs
	 * @param expectedOutputs
	 * @throws Exception
	 */
	protected void performTest(String workflow,Map inputs,Map expectedOutputs) throws Exception
	{
			
		
		Map output = executeWorkflow(workflow,inputs,defaultWorkflowEventListener());
		
		assertEquals("wrong number of outputs",expectedOutputs.size(),output.size());
		for (Iterator iterator=expectedOutputs.keySet().iterator();iterator.hasNext();)
		{
			String outputName=(String)iterator.next();
			if (assertNotNull("output "+outputName+" does not exist",output.get(outputName)))
			{
				DataThing thing = (DataThing)output.get(outputName);
				assertEquals("output does not match",expectedOutputs.get(outputName),thing.getDataObject());
			}			
		}		
	}
	
	/**
	 * Returns a String input data stored in a file /test/data/workflows/<workflow>/inputs/<datafile>
	 * @param workflow
	 * @param dataFile
	 * @return
	 * @throws Exception
	 */
	protected String readInputDataFromFile(String workflow,String dataFile) throws Exception
	{		
		String home = System.getProperty("taverna.home");
		if (home==null) 
		{
			throw new Exception("Unable to open workflow data - taverna.home is not set.");
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(home,"test/data/workflows/"+workflow+"/inputs/"+dataFile)));
		
		String line;
		String result="";
		
		while((line=reader.readLine())!=null)
		{
			result+=line;
			result+="\n";
		}
		
		return result;
		
	}
	
	/**
	 * Returns a String output data stored in a file /test/data/workflows/<workflow>/outputs/<datafile>
	 * @param workflow
	 * @param dataFile
	 * @return
	 * @throws Exception
	 */
	protected String readOutputDataFromFile(String workflow,String dataFile) throws Exception
	{		
		String home = System.getProperty("taverna.home");
		if (home==null) 
		{
			throw new Exception("Unable to open workflow data - taverna.home is not set.");
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(home,"test/data/workflows/"+workflow+"/outputs/"+dataFile)));
		
		String line;
		String result="";
		
		while((line=reader.readLine())!=null)
		{			
			result+=line;
			result+="\n";
		}
		
		return result;
		
	}
}
