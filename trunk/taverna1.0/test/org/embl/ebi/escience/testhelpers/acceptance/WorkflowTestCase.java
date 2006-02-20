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


public class WorkflowTestCase extends AcceptanceTestCase 
{			

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
        
        return workflowInstance.getOutput();               
        							
	}	
	
	
	
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
			if (result.length()>0) result+="\n";
			result+=line;
		}
		
		return result;
		
	}
	
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
			if (result.length()>0) result+="\n";
			result+=line;
		}
		
		return result;
		
	}
}
