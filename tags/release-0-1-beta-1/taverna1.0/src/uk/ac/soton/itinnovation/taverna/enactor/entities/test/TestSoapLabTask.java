
package uk.ac.soton.itinnovation.taverna.enactor.entities.test;

import junit.framework.TestCase;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scufl.SoaplabProcessor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTaskFactory;

// JDOM Imports
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.lang.Exception;
import java.lang.Integer;
import java.lang.String;
import java.lang.System;



public class TestSoapLabTask extends TestCase {

  public TestSoapLabTask(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testDoTask() {
	  try {
	  	// Create a new ScuflModel
		ScuflModel model = new ScuflModel();
		// Register a listener to print to stdout
		model.addListener(new ScuflModelEventPrinter(null));
		
		// Attempt to create a new SoaplabProcessor
		SoaplabProcessor proc = new SoaplabProcessor(model, 
						"my_processor", 
						"http://industry.ebi.ac.uk/soap/soaplab/edit::seqret");
		model.addProcessor(proc);
		String id = "testTask";
		//get the output ports 
		DiGraph d = new DiGraph("testDigraph");
		ProcessorTask serviceTask = TavernaTaskFactory.getConcreteTavernaTask(id,proc,new LogLevel(LogLevel.HIGH));
		GraphNode[] nodes = new GraphNode[1];
		nodes[0] = serviceTask;
		d.setNodeList(nodes);
		Flow flow = new Flow("testFlow",d);
		//want to create suitable input part
		Part part = new Part(1,"sequence_usa","string","embl:xlrhodop");
		Port[] inputPorts = proc.getInputPorts();
		for(int i=0;i<inputPorts.length;i++) {
			if(inputPorts[i].getName().equals(part.getName())) {
				PortTask pT = new PortTask(Integer.toString(i),inputPorts[i]);
				serviceTask.addParent(pT);
				pT.addChild(serviceTask);
				pT.setData(part);
			}
		}
		
		serviceTask.doTask();
		//test the provenance output
		Element e = serviceTask.getProvenance();
		System.out.println("*** describe Provenance follows ****");
		XMLOutputter xmlout = new XMLOutputter();
		xmlout.setIndent(" ");
		xmlout.setNewlines(true);
		xmlout.setTextNormalize(false);
		System.out.println(xmlout.outputString(e));
	  }
	  catch (Exception ex) {
		  ex.printStackTrace();
		  fail();
	  }
  }

  
}
