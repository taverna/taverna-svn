
package uk.ac.soton.itinnovation.taverna.enactor.entities.test;

import java.io.*;
import junit.framework.*;
import org.embl.ebi.escience.scufl.*; 
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.taverna.enactor.entities.*;


public class TestSeqretTask extends TestCase {

  public TestSeqretTask(String s) {
    super(s);
  }

  protected void setUp() {
	


}

  protected void tearDown() {
  }

  public void testdoTask() {
	try{
		
		ScuflModel model = new ScuflModel();
		SoaplabProcessor proc = new SoaplabProcessor(model,"seqret","http://industry.ebi.ac.uk/soap/soaplab/edit::seqret::derived");
		model.addProcessor(proc);
		
		//read in the input
		FileInputStream inputStream = new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/entities/test/sequence_usa.xml");
		Input input = new Input(inputStream);

		FileInputStream inputStream2 = new FileInputStream("src/uk/ac/soton/itinnovation/taverna/enactor/entities/test/os_format.xml");
		Input input2 = new Input(inputStream2);

		//create portTasks
		Port[] inPorts = proc.getInputPorts();
		Port[] outPorts = proc.getOutputPorts();
		Port p1 = null;
		Port p2 = null;
		Port p3 = null;
		Port p4 = null;
		for(int i=0;i<inPorts.length;i++) {
			System.out.println("In Port: " + inPorts[i].getName());
			if(inPorts[i].getName().equals("sequence_usa"))
				p1 = inPorts[i];
			if(inPorts[i].getName().equals("osformat"))
				p2 = inPorts[i];
		}
		for(int i=0;i<outPorts.length;i++) {
			System.out.println("Out Port: " + outPorts[i].getName());
			if(outPorts[i].getName().equals("outseq"))
				p3 = outPorts[i];
			if(outPorts[i].getName().equals("report"))
				p4 = outPorts[i];
		}
		//report port doesn't exist??
		if(p1==null||p2==null||p3==null) {
			System.out.println("Couldn't find all the ports");
			fail();
		}
		PortTask pT1 = new PortTask("p1",p1);
		PortTask pT2 = new PortTask("p2",p2);
		PortTask pT3 = new PortTask("p3",p3);
		PortTask pT4 = new PortTask("p4",p4);

		//set input data
		pT1.setData(input);
		pT2.setData(input2);

		SeqretTask task = new SeqretTask("seqretTask",proc);
		System.out.println("Created seqretTask");
		task.addParent(pT1);
		task.addParent(pT2);
		task.addChild(pT3);
		task.addChild(pT4);

		//call method
		TaskStateMessage message = task.doTask();
		
		assertTrue(message.getState()==TaskStateMessage.COMPLETE);

		//read output portTasks
		input = pT3.waitForData();
		Part p = input.getPartByName("outseq");
		System.out.println("Output Sequence :");
		System.out.println((String) p.getValue());
		System.out.println("");
		input = pT4.waitForData();
		p = input.getPartByName("report");
		System.out.println("");
		System.out.println("Job Report :");
		System.out.println((String) p.getValue()); 

		//assert correct
	}
	catch(Exception ex) {
		ex.printStackTrace();
		fail();
	}
  }  
}