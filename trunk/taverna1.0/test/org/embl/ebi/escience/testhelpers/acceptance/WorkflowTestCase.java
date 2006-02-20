package org.embl.ebi.escience.testhelpers.acceptance;

import java.io.*;
import java.io.InputStream;
import java.util.*;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.*;
import org.embl.ebi.escience.scufl.enactor.implementation.*;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

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
		ScuflModel model = openModel(workflow);	
		if (listener!=null) 
			WorkflowEventDispatcher.DISPATCHER.addListener(listener);        
               
        EnactorProxy enactor = FreefluoEnactorProxy.getInstance();                      
        WorkflowInstance workflowInstance = enactor.compileWorkflow(model, inputs, null);
        workflowInstance.run();
        String status=workflowInstance.getStatus();
        
        while(!status.equals("COMPLETE") && !status.equals("FAILED"))
        {
        	status=workflowInstance.getStatus();        	
        	Thread.sleep(2000);
        } 
        
        if (status.equals("FAILED")) fail("Workflow: "+workflow+" failed to run.");
        
        progressReportXMLString=workflowInstance.getProgressReportXMLString();
        
        return workflowInstance.getOutput();               
        							
	}	
	
	
	/**
	 * Opens and parses xml representing a ScuflModel. This xml is stored in a file that must be in the form and location of
	 * /test/data/workflows/<workflow>/<workflow>.xml
	 * Relies on the property $taverna.home being set in able to find the location of the files.
	 * @param workflowName
	 * @return
	 * @throws Exception
	 */
	protected ScuflModel openModel(String workflowName) throws Exception
	{
		ScuflModel model = new ScuflModel();	
		String home = System.getProperty("taverna.home");
		if (home==null) 
		{
			throw new Exception("Unable to open workflow - taverna.home is not set.");
		}
		InputStream stream=new FileInputStream(new File(home,"/test/data/workflows/"+workflowName+"/"+workflowName+".xml"));
		XScuflParser.populate(stream,model,null);		
		return model;
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
