
package uk.ac.soton.itinnovation.taverna.enactor.broker.test;

import java.io.*;
import junit.framework.*;
import org.embl.ebi.escience.scufl.*; 
import uk.ac.soton.itinnovation.taverna.enactor.broker.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.*;

public class TestTavernaFlowBroker extends TestCase {

  public TestTavernaFlowBroker(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testSubmitWorkflow() {
	try{
	   BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/XScufl_example.xml"));
	   BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input.xml"));

	   StringWriter sWriter = new StringWriter();
       while(workflowspec.available()>0) {
         sWriter.write(workflowspec.read());
       }
       String wsflDefn = sWriter.toString();
       sWriter = new StringWriter();
       while(inData.available()>0) {
         sWriter.write(inData.read());
       }
       String input = sWriter.toString();
       TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(wsflDefn,input,"TestTavernaFlowBroker","http://www.it-innovation.soton.ac.uk/users");
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
       System.out.println("Emboss Workflow has finished with status: " + status); 
       /*	
       //now submit workflow that calls single wsdl service
       BufferedInputStream workflowspec = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/wsdl_example.xml"));
	   BufferedInputStream inData = new BufferedInputStream(new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/broker/test/input2.xml"));

	   StringWriter sWriter = new StringWriter();
       while(workflowspec.available()>0) {
         sWriter.write(workflowspec.read());
       }
       String wsflDefn = sWriter.toString();
       sWriter = new StringWriter();
       while(inData.available()>0) {
         sWriter.write(inData.read());
       }
       String input = sWriter.toString();
       TavernaWorkflowSubmission submit = new TavernaWorkflowSubmission(wsflDefn,input,"TestTavernaFlowBroker","http://www.it-innovation.soton.ac.uk/users");
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
       */

	}
	catch(Exception ex) {
		ex.printStackTrace();
		fail();
	}
  }  
}
