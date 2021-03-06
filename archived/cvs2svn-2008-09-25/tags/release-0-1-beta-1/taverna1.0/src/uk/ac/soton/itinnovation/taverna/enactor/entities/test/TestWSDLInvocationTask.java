
package uk.ac.soton.itinnovation.taverna.enactor.entities.test;

import junit.framework.TestCase;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scufl.WSDLBasedProcessor;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Flow;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.DiGraph;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTaskFactory;

// JDOM Imports
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;



public class TestWSDLInvocationTask extends TestCase {

  public TestWSDLInvocationTask(String s) {
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
		//configure portTask for jobID
		
		// Attempt to create a new WSDLBasedProcessor
		Processor describe = new WSDLBasedProcessor(model,
							  "describe",
							  "http://industry.ebi.ac.uk/soaplab/wsdl/edit__seqret__derived.wsdl",
							  "edit__seqret",
							  "describe");
		String id = "testTask";
		//get the output ports 
		DiGraph d = new DiGraph("testDigraph");
		ProcessorTask serviceTask = TavernaTaskFactory.getConcreteTavernaTask(id,describe,new LogLevel(LogLevel.HIGH));
		GraphNode[] nodes = new GraphNode[1];
		nodes[0] = serviceTask;
		d.setNodeList(nodes);
		Flow flow = new Flow("testFlow",d);
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
