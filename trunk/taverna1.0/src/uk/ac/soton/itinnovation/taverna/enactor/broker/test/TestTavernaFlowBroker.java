
package uk.ac.soton.itinnovation.taverna.enactor.broker.test;

import junit.framework.TestCase;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaStringifiedWorkflowSubmission;

// IO Imports
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



public class TestTavernaFlowBroker extends TestCase {

  public TestTavernaFlowBroker(String s) {
    super(s);
  }

  protected void setUp() {
		/** TODO put in proxy settings here **/
		/*
		System.setProperty("http.proxyHost","www.cache.soton.ac.uk");
		System.setProperty("http.proxyPort","3128");
		*/
	}

  protected void tearDown() {
  }

  public void testSubmitWorkflow() {
	try{
	   //BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/XScufl_example.xml"));
	   //BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input.xml"));
		 //BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("hybridworkflow/workflow.xml"));
		 //BufferedInputStream inData = new BufferedInputStream(new FileInputStream("hybridworkflow/input.xml"));
	   BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/nested_example.xml"));
	   BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input.xml"));
		 StringWriter sWriter = new StringWriter();
       while(workflowspec.available()>0) {
         sWriter.write(workflowspec.read());
       }
       String scuflDefn = sWriter.toString();
       sWriter = new StringWriter();
       while(inData.available()>0) {
         sWriter.write(inData.read());
       }
       String input = sWriter.toString();
       TavernaStringifiedWorkflowSubmission submit = new TavernaStringifiedWorkflowSubmission(scuflDefn,input,"TestTavernaFlowBroker","http://www.it-innovation.soton.ac.uk/users");
       FlowBroker broker = FlowBrokerFactory.createFlowBroker("uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowBroker");
       TavernaFlowReceipt receipt = (TavernaFlowReceipt) broker.submitFlow(submit);
       //poll for status every 500ms
       boolean stop = false;
       String status ="UNKNOWN";
       while(!stop) {
         //retrieve the status
         status = receipt.getStatusString();
         if(status.equals("COMPLETE") || status.equals("FAILED"))
           stop = true;
         try {
           Thread.sleep(500);
         }
         catch(InterruptedException ex) {

         }
       }
	   
       if(status.equals("FAILED"))
	   System.out.println("Error message: " + receipt.getErrorMessage());
       System.out.println("Emboss Workflow has finished with status: " + status); 
	   System.out.println("Output:\n\n" + receipt.getOutputString());
       

	}
	catch(Exception ex) {
		ex.printStackTrace();
		fail();
	}
  }  
}
