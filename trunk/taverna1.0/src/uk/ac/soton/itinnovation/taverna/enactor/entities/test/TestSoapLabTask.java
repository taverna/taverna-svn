
package uk.ac.soton.itinnovation.taverna.enactor.entities.test;

import junit.framework.TestCase;
import org.embl.ebi.escience.scufl.*;
import uk.ac.soton.itinnovation.taverna.enactor.broker.*;
import uk.ac.soton.itinnovation.taverna.enactor.entities.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.*;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.*;

// IO Imports
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;

import org.jdom.*;
import org.jdom.output.*;

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
