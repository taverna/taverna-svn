package org.biomoby.client.taverna.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomoby.client.CentralImpl;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.ScuflModel;

import junit.framework.TestCase;

/**
 * The tests for parsing moby datatypes.
 * @author Edward Kawas
 *
 */
public class BiomobyParserTaskTest extends TestCase {
	// TODO - really should check for all the individual values in the xml rather than print them out
	private ScuflModel model;
	public BiomobyParserTaskTest() {
		super();
		createNewModel();
	}
	
    private void createNewModel() {
		model = new ScuflModel();
		
	}

	public void testObject(){
		System.out.println("Test Object:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(objectXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("7422"));
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectCollection(){
		System.out.println("Test Object Collection:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(objectCollectionXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectList(){
		System.out.println("Test Object List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(objectXML);
		holder.add(objectXML.replaceAll("7422", "2").replaceAll("EntrezGene_ID", "EG1"));
		holder.add(objectXML.replaceAll("7422", "3").replaceAll("EntrezGene_ID", "EG2"));
		holder.add(objectXML.replaceAll("7422", "4").replaceAll("EntrezGene_ID", "EG3"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("7422"));
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectCollectionList(){
		System.out.println("Test Object Collection List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(objectCollectionXML.replaceAll("EG", "EntrezGene_ID_a_").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(objectCollectionXML.replaceAll("EG", "EntrezGene_ID_b_").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(objectCollectionXML.replaceAll("EG", "EntrezGene_ID_c_").replaceAll("222", "b").replaceAll("333", "c"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));
		

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	// MIM messages
	public void testObjectMIM(){
		System.out.println("Test Object MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(objectXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("7422"));
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectCollectionMIM(){
		System.out.println("Test Object Collection MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(objectCollectionXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectListMIM(){
		System.out.println("Test Object List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(objectXMLMIM);
		holder.add(objectXMLMIM.replaceAll("7422", "2").replaceAll("EntrezGene_ID", "EG1"));
		holder.add(objectXMLMIM.replaceAll("7422", "3").replaceAll("EntrezGene_ID", "EG2"));
		holder.add(objectXMLMIM.replaceAll("7422", "4").replaceAll("EntrezGene_ID", "EG3"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("7422"));
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testObjectCollectionListMIM(){
		System.out.println("Test Object Collection List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Object";
		String articlename = "myObject";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(objectCollectionXMLMIM.replaceAll("EG", "EntrezGene_ID_a_").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(objectCollectionXMLMIM.replaceAll("EG", "EntrezGene_ID_b_").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(objectCollectionXMLMIM.replaceAll("EG", "EntrezGene_ID_c_").replaceAll("222", "b").replaceAll("333", "c"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));
		

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" + key + ":-> " + list);
		}
	}
	
	public void testPrimitiveString(){
		System.out.println("Test primitive String:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(stringXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("this is the string")
					|| list.contains("7422"));
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPrimitiveStringCollection(){
		System.out.println("Test primitive String Collection:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(stringCollectionXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPrimitiveStringCollectionList(){
		System.out.println("Test primitive String Collection List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(stringCollectionXML.replaceAll("EntrezGene_ID", "EntrezGene_ID_a_").replaceAll("111", "a").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(stringCollectionXML.replaceAll("EntrezGene_ID", "EntrezGene_ID_b_").replaceAll("111", "i").replaceAll("222", "ii").replaceAll("333", "iii"));
		holder.add(stringCollectionXML.replaceAll("EntrezGene_ID", "EntrezGene_ID_c_").replaceAll("111", "one").replaceAll("222", "two").replaceAll("333", "three"));
		holder.add(stringCollectionXML.replaceAll("EntrezGene_ID", "EntrezGene_ID_d_").replaceAll("111", "uno").replaceAll("222", "dos").replaceAll("333", "tres"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	public void testPrimitiveStringList(){
		System.out.println("Test primitive String List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(stringXML.replaceAll("7422", "1").replaceAll("EntrezGene_ID", "EG1").replaceAll("this is the string", "this is the first string"));
		holder.add(stringXML.replaceAll("7422", "2").replaceAll("EntrezGene_ID", "EG2").replaceAll("this is the string", "this is the second string"));
		holder.add(stringXML.replaceAll("7422", "3").replaceAll("EntrezGene_ID", "EG3").replaceAll("this is the string", "this is the third string"));
		holder.add(stringXML.replaceAll("7422", "4").replaceAll("EntrezGene_ID", "EG4").replaceAll("this is the string", "this is the fourth string"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	// test mim
	public void testPrimitiveStringMIM(){
		System.out.println("Test primitive String MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(stringXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			assertTrue(list.contains("EntrezGene_ID")
					|| list.contains("this is the string")
					|| list.contains("7422"));
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPrimitiveStringCollectionMIM(){
		System.out.println("Test primitive String Collection MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(stringCollectionXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPrimitiveStringCollectionListMIM(){
		System.out.println("Test primitive String Collection List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(stringCollectionXMLMIM.replaceAll("EntrezGene_ID", "EntrezGene_ID_a_").replaceAll("111", "a").replaceAll("222", "b").replaceAll("333", "c"));
		holder.add(stringCollectionXMLMIM.replaceAll("EntrezGene_ID", "EntrezGene_ID_b_").replaceAll("111", "i").replaceAll("222", "ii").replaceAll("333", "iii"));
		holder.add(stringCollectionXMLMIM.replaceAll("EntrezGene_ID", "EntrezGene_ID_c_").replaceAll("111", "one").replaceAll("222", "two").replaceAll("333", "three"));
		holder.add(stringCollectionXMLMIM.replaceAll("EntrezGene_ID", "EntrezGene_ID_d_").replaceAll("111", "uno").replaceAll("222", "dos").replaceAll("333", "tres"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	public void testPrimitiveStringListMIM(){
		System.out.println("Test primitive String List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "String";
		String articlename = "myString";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(stringXMLMIM.replaceAll("7422", "1").replaceAll("EntrezGene_ID", "EG1").replaceAll("this is the string", "this is the first string"));
		holder.add(stringXMLMIM.replaceAll("7422", "2").replaceAll("EntrezGene_ID", "EG2").replaceAll("this is the string", "this is the second string"));
		holder.add(stringXMLMIM.replaceAll("7422", "3").replaceAll("EntrezGene_ID", "EG3").replaceAll("this is the string", "this is the third string"));
		holder.add(stringXMLMIM.replaceAll("7422", "4").replaceAll("EntrezGene_ID", "EG4").replaceAll("this is the string", "this is the fourth string"));
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}

	public void testCompositeGoTerm(){
		System.out.println("Test Composite GO_Term:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(goTermXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testCompositeGoTermList(){
		System.out.println("Test Composite GO_Term List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(goTermXML);
		holder.add(goTermXML);
		holder.add(goTermXML);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testCompositeGoTermCollection(){
		System.out.println("Test Composite GO_Term Collection:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(goTermCollectionXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}

	public void testCompositeGoTermCollectionList(){
		System.out.println("Test Composite GO_Term Collection List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(goTermCollectionXML);
		holder.add(goTermCollectionXML);
		holder.add(goTermCollectionXML);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));
		

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	// test mim
	public void testCompositeGoTermMIM(){
		System.out.println("Test Composite GO_Term MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(goTermXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testCompositeGoTermListMIM(){
		System.out.println("Test Composite GO_Term List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(goTermXMLMIM);
		holder.add(goTermXMLMIM);
		holder.add(goTermXMLMIM);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testCompositeGoTermCollectionMIM(){
		System.out.println("Test Composite GO_Term Collection MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(goTermCollectionXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}

	public void testCompositeGoTermCollectionListMIM(){
		System.out.println("Test Composite GO_Term Collection List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "GO_Term";
		String articlename = "terms";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(goTermCollectionXMLMIM);
		holder.add(goTermCollectionXMLMIM);
		holder.add(goTermCollectionXMLMIM);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));
		

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	

	
	public void testPubmed(){
		System.out.println("Test Pubmed:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(pubmedXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPubmedCollection(){
		System.out.println("Test Pubmed Collection:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(pubmedCollectionXML));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPubmedCollectionList(){
		System.out.println("Test Pubmed Collection List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(pubmedCollectionXML);
		holder.add(pubmedCollectionXML);
		holder.add(pubmedCollectionXML);
		holder.add(pubmedCollectionXML);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	public void testPubmedList(){
		System.out.println("Test Pubmed List:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(pubmedXML);
		holder.add(pubmedXML);
		holder.add(pubmedXML);
		holder.add(pubmedXML);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	// test mim
	public void testPubmedMIM(){
		System.out.println("Test Pubmed MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(pubmedXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPubmedCollectionMIM(){
		System.out.println("Test Pubmed Collection MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(pubmedXMLMIM));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	
	public void testPubmedCollectionListMIM(){
		System.out.println("Test Pubmed Collection List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(pubmedCollectionXMLMIM);
		holder.add(pubmedCollectionXMLMIM);
		holder.add(pubmedCollectionXMLMIM);
		holder.add(pubmedCollectionXMLMIM);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	public void testPubmedListMIM(){
		System.out.println("Test Pubmed List MIM:");
		createNewModel();
		MobyParseDatatypeProcessor p = null;
		HashMap<String, DataThing> inputs = new HashMap<String, DataThing>();
		MobyParseDatatypeTask theTask = null;
		Map<String, DataThing> outputs = null;
		String datatypeName = "Publication";
		String articlename = "publications";
		
		// create the parse datatype processor
		try {
			p = new MobyParseDatatypeProcessor(model, "foo", datatypeName,
					articlename, CentralImpl.DEFAULT_ENDPOINT);
		} catch (Exception e) {
			fail("problem creating a datatype: " + e.getLocalizedMessage());
		}

		// bind the output ports to workflow outputs
		try {
			OutputPort[] outPorts = p.getOutputPorts();
			for (int i = 0; i < outPorts.length; i++) {
				String portName = outPorts[i].getName();
				InputPort port = new InputPort(
						model.getWorkflowSinkProcessor(), portName);
				model.getWorkflowSinkProcessor().addPort(port);
				model.addDataConstraint(new DataConstraint(model, outPorts[i],
						port));
			}

		} catch (Exception e) {
			fail("Problem linking output ports: " + e.getLocalizedMessage());
		}
		// create the task
		try {
			theTask = new MobyParseDatatypeTask(p);
		} catch (Exception e) {
			fail("Problem creating the task: " + e.getLocalizedMessage());
		}

		// populate the inptus
		ArrayList<String> holder = new ArrayList<String>();
		holder.add(pubmedXMLMIM);
		holder.add(pubmedXMLMIM);
		holder.add(pubmedXMLMIM);
		holder.add(pubmedXMLMIM);
		inputs.put(p.getInputPorts()[0].getName(), new DataThing(holder));

		// run the task
		try {
			outputs = theTask.execute(inputs, null);
		} catch (Exception e) {
			fail("problem running the task: " + e.getLocalizedMessage());
		}

		// check what was returned
		for (String key : outputs.keySet()) {
			DataThing thingy = outputs.get(key);
			assertTrue(thingy.getDataObject() instanceof List);
			List<String> list = (List) thingy.getDataObject();
			assertTrue(!list.isEmpty());
			assertTrue(list.get(0).trim().length() != 0);
			System.out.println("\t" +key + ":-> " + list);
		}
	}
	private String stringXML = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
			"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
			"  <moby:mobyContent>" + 
			"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
			"      <moby:Simple moby:articleName=\"myString\">" + 
			"        <moby:String moby:id=\"7422\" moby:namespace=\"EntrezGene_ID\" >this is the string</moby:String>" + 
			"      </moby:Simple>" + 
			"    </moby:mobyData>" + 
			"  </moby:mobyContent>" + 
			"</moby:MOBY>";
	
	private String stringCollectionXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Collection moby:articleName=\"myString\">" + 
		"        <moby:Simple>" + 
		"        <moby:String moby:id=\"111\" moby:namespace=\"EntrezGene_ID1\" >this is the string 1</moby:String>" +
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"        <moby:String moby:id=\"222\" moby:namespace=\"EntrezGene_ID2\" >this is the string 2</moby:String>" +
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"        <moby:String moby:id=\"333\" moby:namespace=\"EntrezGene_ID3\" >this is the string 3</moby:String>" +
		"        </moby:Simple>" +  
		"      </moby:Collection>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>" + 
		"";
	
	private String objectXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Simple moby:articleName=\"myObject\">" + 
		"        <moby:Object moby:id=\"7422\" moby:namespace=\"EntrezGene_ID\" />" + 
		"      </moby:Simple>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>";
	
	private String objectCollectionXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Collection moby:articleName=\"myObject\">" + 
		"        <moby:Simple>" + 
		"        <moby:Object moby:id=\"1\" moby:namespace=\"EG1\" />" + 
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"        <moby:Object moby:id=\"222\" moby:namespace=\"EG2\" />" +
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"        <moby:Object moby:id=\"333\" moby:namespace=\"EG3\" />" +
		"        </moby:Simple>" +  
		"      </moby:Collection>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>" + 
		"";
	
	private String goTermCollectionXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Collection moby:articleName=\"terms\">" + 
		"        <moby:Simple>" + 
		"          <moby:GO_Term moby:id=\"0050840\" moby:namespace=\"GO1\">" + 
		"            <moby:String moby:id=\"1a\" moby:namespace=\"n1a\" moby:articleName=\"Term\">extracellular matrix binding</moby:String>" + 
		"            <moby:String moby:id=\"1b\" moby:namespace=\"n1b\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix.</moby:String>" + 
		"          </moby:GO_Term>" + 
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"          <moby:GO_Term moby:id=\"0008083\" moby:namespace=\"GO2\">" + 
		"            <moby:String moby:id=\"2a\" moby:namespace=\"n2a\" moby:articleName=\"Term\">growth factor activity</moby:String>" + 
		"            <moby:String moby:id=\"2b\" moby:namespace=\"n2b\" moby:articleName=\"Definition\">The function that stimulates a cell to grow or proliferate. Most growth factors have other actions besides the induction of cell growth or proliferation.</moby:String>" + 
		"          </moby:GO_Term>" + 
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"          <moby:GO_Term moby:id=\"0008201\" moby:namespace=\"GO3\">" + 
		"            <moby:String moby:id=\"3a\" moby:namespace=\"n3a\" moby:articleName=\"Term\">heparin binding</moby:String>" + 
		"            <moby:String moby:id=\"3b\" moby:namespace=\"n3b\" moby:articleName=\"Definition\">Interacting selectively with heparin, any member of a group of glycosaminoglycans found mainly as an intracellular component of mast cells and which consist predominantly of alternating alpha1-4-linked D-galactose and N-acetyl-D-glucosamine-6-sulfate residues.</moby:String>" + 
		"          </moby:GO_Term>" + 
		"        </moby:Simple>" +  
		"      </moby:Collection>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>" + 
		"";
	
	private String goTermXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Simple moby:articleName=\"terms\">" + 
		"          <moby:GO_Term moby:id=\"0050840\" moby:namespace=\"GO1\">" + 
		"            <moby:String moby:id=\"1a\" moby:namespace=\"n1a\" moby:articleName=\"Term\">extracellular matrix binding</moby:String>" + 
		"            <moby:String moby:id=\"1b\" moby:namespace=\"n1b\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix.</moby:String>" + 
		"          </moby:GO_Term>" + 
		"        </moby:Simple>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>" + 
		"";
	
	private String stringXMLMIM = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Simple moby:articleName=\"myString\">" + 
		"        <moby:String moby:id=\"7422\" moby:namespace=\"EntrezGene_ID\" >this is the string</moby:String>" + 
		"      </moby:Simple>" + 
		"    </moby:mobyData>" + 
		"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
		"      <moby:Simple moby:articleName=\"myString\">" + 
		"        <moby:String moby:id=\"7422_2\" moby:namespace=\"EntrezGene_ID_2\" >this is the string_2</moby:String>" + 
		"      </moby:Simple>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>";

private String stringCollectionXMLMIM = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
	"  <moby:mobyContent>" + 
	"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
	"      <moby:Collection moby:articleName=\"myString\">" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"111\" moby:namespace=\"EntrezGene_ID1\" >this is the string 1</moby:String>" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"222\" moby:namespace=\"EntrezGene_ID2\" >this is the string 2</moby:String>" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"333\" moby:namespace=\"EntrezGene_ID3\" >this is the string 3</moby:String>" +
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
	"      <moby:Collection moby:articleName=\"myString\">" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"111_2\" moby:namespace=\"EntrezGene_ID1_2\" >this is the string 1_2</moby:String>" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"222_2\" moby:namespace=\"EntrezGene_ID2_2\" >this is the string 2_2</moby:String>" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:String moby:id=\"333_2\" moby:namespace=\"EntrezGene_ID3_2\" >this is the string 3_2</moby:String>" +
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"  </moby:mobyContent>" + 
	"</moby:MOBY>" + 
	"";

private String objectXMLMIM = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
	"  <moby:mobyContent>" + 
	"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
	"      <moby:Simple moby:articleName=\"myObject\">" + 
	"        <moby:Object moby:id=\"7422\" moby:namespace=\"EntrezGene_ID\" />" + 
	"      </moby:Simple>" + 
	"    </moby:mobyData>" + 
	"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
	"      <moby:Simple moby:articleName=\"myObject\">" + 
	"        <moby:Object moby:id=\"7422_2\" moby:namespace=\"EntrezGene_ID_2\" />" + 
	"      </moby:Simple>" + 
	"    </moby:mobyData>" + 
	"  </moby:mobyContent>" + 
	"</moby:MOBY>";

private String objectCollectionXMLMIM = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
	"  <moby:mobyContent>" + 
	"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
	"      <moby:Collection moby:articleName=\"myObject\">" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"1\" moby:namespace=\"EG1\" />" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"222\" moby:namespace=\"EG2\" />" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"333\" moby:namespace=\"EG3\" />" +
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
	"      <moby:Collection moby:articleName=\"myObject\">" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"1_2\" moby:namespace=\"EG1_2\" />" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"222_2\" moby:namespace=\"EG2_2\" />" +
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"        <moby:Object moby:id=\"333_2\" moby:namespace=\"EG3_2\" />" +
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"  </moby:mobyContent>" + 
	"</moby:MOBY>" + 
	"";

private String goTermCollectionXMLMIM = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
	"  <moby:mobyContent>" + 
	"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
	"      <moby:Collection moby:articleName=\"terms\">" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0050840\" moby:namespace=\"GO1\">" + 
	"            <moby:String moby:id=\"1a\" moby:namespace=\"n1a\" moby:articleName=\"Term\">extracellular matrix binding</moby:String>" + 
	"            <moby:String moby:id=\"1b\" moby:namespace=\"n1b\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0008083\" moby:namespace=\"GO2\">" + 
	"            <moby:String moby:id=\"2a\" moby:namespace=\"n2a\" moby:articleName=\"Term\">growth factor activity</moby:String>" + 
	"            <moby:String moby:id=\"2b\" moby:namespace=\"n2b\" moby:articleName=\"Definition\">The function that stimulates a cell to grow or proliferate. Most growth factors have other actions besides the induction of cell growth or proliferation.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0008201\" moby:namespace=\"GO3\">" + 
	"            <moby:String moby:id=\"3a\" moby:namespace=\"n3a\" moby:articleName=\"Term\">heparin binding</moby:String>" + 
	"            <moby:String moby:id=\"3b\" moby:namespace=\"n3b\" moby:articleName=\"Definition\">Interacting selectively with heparin, any member of a group of glycosaminoglycans found mainly as an intracellular component of mast cells and which consist predominantly of alternating alpha1-4-linked D-galactose and N-acetyl-D-glucosamine-6-sulfate residues.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
	"      <moby:Collection moby:articleName=\"terms\">" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0050840_2\" moby:namespace=\"GO1_2\">" + 
	"            <moby:String moby:id=\"1a_2\" moby:namespace=\"n1a_2\" moby:articleName=\"Term\">extracellular matrix binding_2</moby:String>" + 
	"            <moby:String moby:id=\"1b_2\" moby:namespace=\"n1b_2\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix._2</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0008083_2\" moby:namespace=\"GO2_2\">" + 
	"            <moby:String moby:id=\"2a_2\" moby:namespace=\"n2a_2\" moby:articleName=\"Term\">growth factor activity_2</moby:String>" + 
	"            <moby:String moby:id=\"2b_2\" moby:namespace=\"n2b_2\" moby:articleName=\"Definition\">The function that stimulates a cell to grow or proliferate. Most growth factors have other actions besides the induction of cell growth or proliferation_2.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"        <moby:Simple>" + 
	"          <moby:GO_Term moby:id=\"0008201_2\" moby:namespace=\"GO3_2\">" + 
	"            <moby:String moby:id=\"3a_2\" moby:namespace=\"n3a_2\" moby:articleName=\"Term\">heparin binding_2</moby:String>" + 
	"            <moby:String moby:id=\"3b_2\" moby:namespace=\"n3b_2\" moby:articleName=\"Definition\">Interacting selectively with heparin, any member of a group of glycosaminoglycans found mainly as an intracellular component of mast cells and which consist predominantly of alternating alpha1-4-linked D-galactose and N-acetyl-D-glucosamine-6-sulfate residues_2.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" +  
	"      </moby:Collection>" + 
	"    </moby:mobyData>" + 
	"  </moby:mobyContent>" + 
	"</moby:MOBY>" + 
	"";

private String goTermXMLMIM = 
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
	"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
	"  <moby:mobyContent>" + 
	"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
	"      <moby:Simple moby:articleName=\"terms\">" + 
	"          <moby:GO_Term moby:id=\"0050840\" moby:namespace=\"GO1\">" + 
	"            <moby:String moby:id=\"1a\" moby:namespace=\"n1a\" moby:articleName=\"Term\">extracellular matrix binding</moby:String>" + 
	"            <moby:String moby:id=\"1b\" moby:namespace=\"n1b\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix.</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"    </moby:mobyData>" + 
	"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
	"      <moby:Simple moby:articleName=\"terms\">" + 
	"          <moby:GO_Term moby:id=\"0050840_2\" moby:namespace=\"GO1_2\">" + 
	"            <moby:String moby:id=\"1a_2\" moby:namespace=\"n1a_2\" moby:articleName=\"Term\">extracellular matrix binding_2</moby:String>" + 
	"            <moby:String moby:id=\"1b_2\" moby:namespace=\"n1b_2\" moby:articleName=\"Definition\">Interacting selectively with a component of the extracellular matrix._2</moby:String>" + 
	"          </moby:GO_Term>" + 
	"        </moby:Simple>" + 
	"    </moby:mobyData>" + 
	"  </moby:mobyContent>" + 
	"</moby:MOBY>" + 
	"";

private String pubmedCollectionXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
		"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
		"  <moby:mobyContent>" + 
		"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
		"      <moby:Collection moby:articleName=\"publications\">" + 
		"        <moby:Simple>" + 
		"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
		"            <moby:String moby:id=\"id_1\" moby:namespace=\"ns1\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
		"            <moby:String moby:id=\"id_2\" moby:namespace=\"ns2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
		"            <moby:String moby:id=\"id_3\" moby:namespace=\"ns3\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
		"            <moby:String moby:id=\"id_4\" moby:namespace=\"ns4\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
		"            <moby:String moby:id=\"id_5\" moby:namespace=\"ns5\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
		"            <moby:String moby:id=\"id_6\" moby:namespace=\"ns6\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
		"            <moby:String moby:id=\"id_7\" moby:namespace=\"ns7\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
		"            <moby:String moby:id=\"id_8\" moby:namespace=\"ns8\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
		"          </moby:Publication>" + 
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"          <moby:Publication moby:id=\"11563986\" moby:namespace=\"PMID\">" + 
		"            <moby:String moby:id=\"id_a1\" moby:namespace=\"nsa1\" moby:articleName=\"Author\">M K Tee</moby:String>" + 
		"            <moby:String moby:id=\"id_a2\" moby:namespace=\"nsa2\" moby:articleName=\"Author\">R B Jaffe</moby:String>" + 
		"            <moby:String moby:id=\"id_a3\" moby:namespace=\"nsa3\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) is a mitogen in physiological and pathological angiogenesis. Understanding the expression of different VEGF isoforms might be important for distinguishing angiogenesis in tissue development, vascular remodelling and tumour formation. We examined its expression and noted the presence of the isoforms VEGF(121) and VEGF(165) (121 and 165 residues long respectively) in fetal heart, lung, ovary, spleen, placenta and ovarian tumours. Unexpectedly, a 47 kDa species predominated in fetal intestine and muscle. The presumed initiation site in VEGF is an AUG codon (AUG(1039)), 1039 nt from its main transcriptional start site. AUG(1039) is preceded in the 5\' untranslated region by an in-frame CUG at nt 499 (CUG(499)), which could produce the 47 kDa form with a 180-residue N-terminal extension. We therefore assessed whether CUG(499) functions as an initiator. CUG(499) initiation produced the 47 kDa VEGF(165) precursor, which was processed at two sites to yield VEGF and three N-terminal fragments. When CTG(499) was mutated to CGC, the precursor and N-terminal fragments were barely detectable. Although the precursor form was predominant in VEGF(165), both CUG(499) and AUG(1039) forms were found in VEGF(121). VEGF precursor induced neither the proliferation of human umbilical vein endothelial cells nor the expression of angiopoietin 2, which can be induced by, and act with, VEGF to induce tumour angiogenesis. The precursor also adheres to the extracellular matrix (ECM), suggesting that it might be a storage form for generating active VEGF in the cell or ECM. Alternate CUG(499) and AUG(1039) initiation and processing of the inactive precursor and its products might be important in regulating angiogenesis.</moby:String>" + 
		"            <moby:String moby:id=\"id_a4\" moby:namespace=\"nsa4\" moby:articleName=\"Title\">A precursor form of vascular endothelial growth factor arises by initiation from an upstream in-frame CUG codon.</moby:String>" + 
		"            <moby:String moby:id=\"id_a5\" moby:namespace=\"nsa5\" moby:articleName=\"Journal\">The Biochemical journal</moby:String>" + 
		"          </moby:Publication>" + 
		"        </moby:Simple>" + 
		"        <moby:Simple>" + 
		"          <moby:Publication moby:id=\"11642726\" moby:namespace=\"PMID\">" + 
		"            <moby:String moby:id=\"id_one\" moby:namespace=\"nsone\" moby:articleName=\"Author\">S A Vinores</moby:String>" + 
		"            <moby:String moby:id=\"id_two\" moby:namespace=\"nstwo\" moby:articleName=\"Author\">N L Derevjanik</moby:String>" + 
		"            <moby:String moby:id=\"id_three\" moby:namespace=\"nsthree\" moby:articleName=\"Author\">A Shi</moby:String>" + 
		"            <moby:String moby:id=\"id_four\" moby:namespace=\"nsfour\" moby:articleName=\"Author\">M A Vinores</moby:String>" + 
		"            <moby:String moby:id=\"id_five\" moby:namespace=\"nsfive\" moby:articleName=\"Author\">D A Klein</moby:String>" + 
		"            <moby:String moby:id=\"id_six\" moby:namespace=\"nssix\" moby:articleName=\"Author\">J A Whittum-Hudson</moby:String>" + 
		"            <moby:String moby:id=\"id_seven\" moby:namespace=\"nsseven\" moby:articleName=\"Abstract\">Experimental herpesvirus retinopathy presents a unique model of a transient inflammatory response in the virus-injected eye and subsequent acute retinal necrosis and chronic inflammation in the contralateral eye. For 6 days after infection, VEGF, TGFbeta1, and TGFbeta2 were associated only with inflammatory cells in the injected eye. By 6 days (after viral antigens were no longer detected), VEGF and TGFbeta2 were upregulated in retinas of injected eyes until 8-10 days. In contralateral eyes, VEGF was first demonstrated in the retina at 6-7 days (prior to the appearance of viral antigens) and TGFbeta2 at 7-8 days. Staining for these factors was also evident around areas of necrosis. The VEGF receptor, flt-1, was associated with ganglion cells and the inner nuclear layer of normal and experimental mice and it was also demonstrated around areas of necrosis. Another VEGF receptor, flk-1, was localized to Muller cell processes and the outer plexiform layer in normal and experimental mice. Coincident with VEGF upregulation in the retinas of herpesvirus-1 injected mice, there was increased flk-1 in ganglion cells and the inner and outer nuclear layers. IL-6 was associated with Muller cell endfeet in normal mice. Following unilateral intraocular inoculation, IL-6 spread along the MUller cell processes and some astrocytes demonstrated IL-6 in both eyes at 6-8 days. The present study demonstrates that intraocular inoculation of herpesvirus is sufficient to induce VEGF, flk-1, TGFbeta2, and IL-6 in the retinas of injected and contralateral eyes. Further investigation of common signaling pathways for these factors during responses to viral infection and the development of acute retinal necrosis could provide information useful for therapeutic intervention in human herpesvirus retinopathy.</moby:String>" + 
		"            <moby:String moby:id=\"id_eight\" moby:namespace=\"nseight\" moby:articleName=\"Title\">Vascular endothelial growth factor (VEGF), transforming growth factor-beta (TGFbeta), and interleukin-6 (IL-6) in experimental herpesvirus retinopathy: association with inflammation and viral infection.</moby:String>" + 
		"            <moby:String moby:id=\"id_nine\" moby:namespace=\"nsnine\" moby:articleName=\"Journal\">Histology and histopathology</moby:String>" + 
		"          </moby:Publication>" + 
		"        </moby:Simple>" + 
		"      </moby:Collection>" + 
		"    </moby:mobyData>" + 
		"  </moby:mobyContent>" + 
		"</moby:MOBY>";

private String pubmedCollectionXMLMIM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
"  <moby:mobyContent>" + 
"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
"      <moby:Collection moby:articleName=\"publications\">" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_1\" moby:namespace=\"ns1\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
"            <moby:String moby:id=\"id_2\" moby:namespace=\"ns2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
"            <moby:String moby:id=\"id_3\" moby:namespace=\"ns3\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
"            <moby:String moby:id=\"id_4\" moby:namespace=\"ns4\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
"            <moby:String moby:id=\"id_5\" moby:namespace=\"ns5\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
"            <moby:String moby:id=\"id_6\" moby:namespace=\"ns6\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
"            <moby:String moby:id=\"id_7\" moby:namespace=\"ns7\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
"            <moby:String moby:id=\"id_8\" moby:namespace=\"ns8\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11563986\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_a1\" moby:namespace=\"nsa1\" moby:articleName=\"Author\">M K Tee</moby:String>" + 
"            <moby:String moby:id=\"id_a2\" moby:namespace=\"nsa2\" moby:articleName=\"Author\">R B Jaffe</moby:String>" + 
"            <moby:String moby:id=\"id_a3\" moby:namespace=\"nsa3\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) is a mitogen in physiological and pathological angiogenesis. Understanding the expression of different VEGF isoforms might be important for distinguishing angiogenesis in tissue development, vascular remodelling and tumour formation. We examined its expression and noted the presence of the isoforms VEGF(121) and VEGF(165) (121 and 165 residues long respectively) in fetal heart, lung, ovary, spleen, placenta and ovarian tumours. Unexpectedly, a 47 kDa species predominated in fetal intestine and muscle. The presumed initiation site in VEGF is an AUG codon (AUG(1039)), 1039 nt from its main transcriptional start site. AUG(1039) is preceded in the 5\' untranslated region by an in-frame CUG at nt 499 (CUG(499)), which could produce the 47 kDa form with a 180-residue N-terminal extension. We therefore assessed whether CUG(499) functions as an initiator. CUG(499) initiation produced the 47 kDa VEGF(165) precursor, which was processed at two sites to yield VEGF and three N-terminal fragments. When CTG(499) was mutated to CGC, the precursor and N-terminal fragments were barely detectable. Although the precursor form was predominant in VEGF(165), both CUG(499) and AUG(1039) forms were found in VEGF(121). VEGF precursor induced neither the proliferation of human umbilical vein endothelial cells nor the expression of angiopoietin 2, which can be induced by, and act with, VEGF to induce tumour angiogenesis. The precursor also adheres to the extracellular matrix (ECM), suggesting that it might be a storage form for generating active VEGF in the cell or ECM. Alternate CUG(499) and AUG(1039) initiation and processing of the inactive precursor and its products might be important in regulating angiogenesis.</moby:String>" + 
"            <moby:String moby:id=\"id_a4\" moby:namespace=\"nsa4\" moby:articleName=\"Title\">A precursor form of vascular endothelial growth factor arises by initiation from an upstream in-frame CUG codon.</moby:String>" + 
"            <moby:String moby:id=\"id_a5\" moby:namespace=\"nsa5\" moby:articleName=\"Journal\">The Biochemical journal</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11642726\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_one\" moby:namespace=\"nsone\" moby:articleName=\"Author\">S A Vinores</moby:String>" + 
"            <moby:String moby:id=\"id_two\" moby:namespace=\"nstwo\" moby:articleName=\"Author\">N L Derevjanik</moby:String>" + 
"            <moby:String moby:id=\"id_three\" moby:namespace=\"nsthree\" moby:articleName=\"Author\">A Shi</moby:String>" + 
"            <moby:String moby:id=\"id_four\" moby:namespace=\"nsfour\" moby:articleName=\"Author\">M A Vinores</moby:String>" + 
"            <moby:String moby:id=\"id_five\" moby:namespace=\"nsfive\" moby:articleName=\"Author\">D A Klein</moby:String>" + 
"            <moby:String moby:id=\"id_six\" moby:namespace=\"nssix\" moby:articleName=\"Author\">J A Whittum-Hudson</moby:String>" + 
"            <moby:String moby:id=\"id_seven\" moby:namespace=\"nsseven\" moby:articleName=\"Abstract\">Experimental herpesvirus retinopathy presents a unique model of a transient inflammatory response in the virus-injected eye and subsequent acute retinal necrosis and chronic inflammation in the contralateral eye. For 6 days after infection, VEGF, TGFbeta1, and TGFbeta2 were associated only with inflammatory cells in the injected eye. By 6 days (after viral antigens were no longer detected), VEGF and TGFbeta2 were upregulated in retinas of injected eyes until 8-10 days. In contralateral eyes, VEGF was first demonstrated in the retina at 6-7 days (prior to the appearance of viral antigens) and TGFbeta2 at 7-8 days. Staining for these factors was also evident around areas of necrosis. The VEGF receptor, flt-1, was associated with ganglion cells and the inner nuclear layer of normal and experimental mice and it was also demonstrated around areas of necrosis. Another VEGF receptor, flk-1, was localized to Muller cell processes and the outer plexiform layer in normal and experimental mice. Coincident with VEGF upregulation in the retinas of herpesvirus-1 injected mice, there was increased flk-1 in ganglion cells and the inner and outer nuclear layers. IL-6 was associated with Muller cell endfeet in normal mice. Following unilateral intraocular inoculation, IL-6 spread along the MUller cell processes and some astrocytes demonstrated IL-6 in both eyes at 6-8 days. The present study demonstrates that intraocular inoculation of herpesvirus is sufficient to induce VEGF, flk-1, TGFbeta2, and IL-6 in the retinas of injected and contralateral eyes. Further investigation of common signaling pathways for these factors during responses to viral infection and the development of acute retinal necrosis could provide information useful for therapeutic intervention in human herpesvirus retinopathy.</moby:String>" + 
"            <moby:String moby:id=\"id_eight\" moby:namespace=\"nseight\" moby:articleName=\"Title\">Vascular endothelial growth factor (VEGF), transforming growth factor-beta (TGFbeta), and interleukin-6 (IL-6) in experimental herpesvirus retinopathy: association with inflammation and viral infection.</moby:String>" + 
"            <moby:String moby:id=\"id_nine\" moby:namespace=\"nsnine\" moby:articleName=\"Journal\">Histology and histopathology</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"      </moby:Collection>" + 
"    </moby:mobyData>" + 
"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
"      <moby:Collection moby:articleName=\"publications\">" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_1_2\" moby:namespace=\"ns1_2\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
"            <moby:String moby:id=\"id_2_2\" moby:namespace=\"ns2_2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
"            <moby:String moby:id=\"id_3_2\" moby:namespace=\"ns3_2\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
"            <moby:String moby:id=\"id_4_2\" moby:namespace=\"ns4_2\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
"            <moby:String moby:id=\"id_5_2\" moby:namespace=\"ns5_2\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
"            <moby:String moby:id=\"id_6_2\" moby:namespace=\"ns6_2\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
"            <moby:String moby:id=\"id_7_2\" moby:namespace=\"ns7_2\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
"            <moby:String moby:id=\"id_8_2\" moby:namespace=\"ns8_2\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11563986\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_a1_2\" moby:namespace=\"nsa1_2\" moby:articleName=\"Author\">M K Tee</moby:String>" + 
"            <moby:String moby:id=\"id_a2_2\" moby:namespace=\"nsa2_2\" moby:articleName=\"Author\">R B Jaffe</moby:String>" + 
"            <moby:String moby:id=\"id_a3_2\" moby:namespace=\"nsa3_2\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) is a mitogen in physiological and pathological angiogenesis. Understanding the expression of different VEGF isoforms might be important for distinguishing angiogenesis in tissue development, vascular remodelling and tumour formation. We examined its expression and noted the presence of the isoforms VEGF(121) and VEGF(165) (121 and 165 residues long respectively) in fetal heart, lung, ovary, spleen, placenta and ovarian tumours. Unexpectedly, a 47 kDa species predominated in fetal intestine and muscle. The presumed initiation site in VEGF is an AUG codon (AUG(1039)), 1039 nt from its main transcriptional start site. AUG(1039) is preceded in the 5\' untranslated region by an in-frame CUG at nt 499 (CUG(499)), which could produce the 47 kDa form with a 180-residue N-terminal extension. We therefore assessed whether CUG(499) functions as an initiator. CUG(499) initiation produced the 47 kDa VEGF(165) precursor, which was processed at two sites to yield VEGF and three N-terminal fragments. When CTG(499) was mutated to CGC, the precursor and N-terminal fragments were barely detectable. Although the precursor form was predominant in VEGF(165), both CUG(499) and AUG(1039) forms were found in VEGF(121). VEGF precursor induced neither the proliferation of human umbilical vein endothelial cells nor the expression of angiopoietin 2, which can be induced by, and act with, VEGF to induce tumour angiogenesis. The precursor also adheres to the extracellular matrix (ECM), suggesting that it might be a storage form for generating active VEGF in the cell or ECM. Alternate CUG(499) and AUG(1039) initiation and processing of the inactive precursor and its products might be important in regulating angiogenesis.</moby:String>" + 
"            <moby:String moby:id=\"id_a4_2\" moby:namespace=\"nsa4_2\" moby:articleName=\"Title\">A precursor form of vascular endothelial growth factor arises by initiation from an upstream in-frame CUG codon.</moby:String>" + 
"            <moby:String moby:id=\"id_a5_2\" moby:namespace=\"nsa5_2\" moby:articleName=\"Journal\">The Biochemical journal</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"        <moby:Simple>" + 
"          <moby:Publication moby:id=\"11642726\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"id_one_2\" moby:namespace=\"nsone_2\" moby:articleName=\"Author\">S A Vinores</moby:String>" + 
"            <moby:String moby:id=\"id_two_2\" moby:namespace=\"nstwo_2\" moby:articleName=\"Author\">N L Derevjanik</moby:String>" + 
"            <moby:String moby:id=\"id_three_2\" moby:namespace=\"nsthree_2\" moby:articleName=\"Author\">A Shi</moby:String>" + 
"            <moby:String moby:id=\"id_four_2\" moby:namespace=\"nsfour_2\" moby:articleName=\"Author\">M A Vinores</moby:String>" + 
"            <moby:String moby:id=\"id_five_2\" moby:namespace=\"nsfive_2\" moby:articleName=\"Author\">D A Klein</moby:String>" + 
"            <moby:String moby:id=\"id_six_2\" moby:namespace=\"nssix_2\" moby:articleName=\"Author\">J A Whittum-Hudson</moby:String>" + 
"            <moby:String moby:id=\"id_seven_2\" moby:namespace=\"nsseven_2\" moby:articleName=\"Abstract\">Experimental herpesvirus retinopathy presents a unique model of a transient inflammatory response in the virus-injected eye and subsequent acute retinal necrosis and chronic inflammation in the contralateral eye. For 6 days after infection, VEGF, TGFbeta1, and TGFbeta2 were associated only with inflammatory cells in the injected eye. By 6 days (after viral antigens were no longer detected), VEGF and TGFbeta2 were upregulated in retinas of injected eyes until 8-10 days. In contralateral eyes, VEGF was first demonstrated in the retina at 6-7 days (prior to the appearance of viral antigens) and TGFbeta2 at 7-8 days. Staining for these factors was also evident around areas of necrosis. The VEGF receptor, flt-1, was associated with ganglion cells and the inner nuclear layer of normal and experimental mice and it was also demonstrated around areas of necrosis. Another VEGF receptor, flk-1, was localized to Muller cell processes and the outer plexiform layer in normal and experimental mice. Coincident with VEGF upregulation in the retinas of herpesvirus-1 injected mice, there was increased flk-1 in ganglion cells and the inner and outer nuclear layers. IL-6 was associated with Muller cell endfeet in normal mice. Following unilateral intraocular inoculation, IL-6 spread along the MUller cell processes and some astrocytes demonstrated IL-6 in both eyes at 6-8 days. The present study demonstrates that intraocular inoculation of herpesvirus is sufficient to induce VEGF, flk-1, TGFbeta2, and IL-6 in the retinas of injected and contralateral eyes. Further investigation of common signaling pathways for these factors during responses to viral infection and the development of acute retinal necrosis could provide information useful for therapeutic intervention in human herpesvirus retinopathy.</moby:String>" + 
"            <moby:String moby:id=\"id_eight_2\" moby:namespace=\"nseight_2\" moby:articleName=\"Title\">Vascular endothelial growth factor (VEGF), transforming growth factor-beta (TGFbeta), and interleukin-6 (IL-6) in experimental herpesvirus retinopathy: association with inflammation and viral infection.</moby:String>" + 
"            <moby:String moby:id=\"id_nine_2\" moby:namespace=\"nsnine_2\" moby:articleName=\"Journal\">Histology and histopathology</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" + 
"      </moby:Collection>" + 
"    </moby:mobyData>" + 
"  </moby:mobyContent>" + 
"</moby:MOBY>";

private String pubmedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
"  <moby:mobyContent>" + 
"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
"      <moby:Simple moby:articleName=\"publications\">" + 
"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"aa1\" moby:namespace=\"nn1\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
"            <moby:String moby:id=\"aa2\" moby:namespace=\"nn2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
"            <moby:String moby:id=\"aa3\" moby:namespace=\"nn3\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
"            <moby:String moby:id=\"aa4\" moby:namespace=\"nn4\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
"            <moby:String moby:id=\"aa5\" moby:namespace=\"nn5\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
"            <moby:String moby:id=\"aa6\" moby:namespace=\"nn6\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
"            <moby:String moby:id=\"aa7\" moby:namespace=\"nn7\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
"            <moby:String moby:id=\"aa8\" moby:namespace=\"nn8\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
"          </moby:Publication>" +  
"        </moby:Simple>" +
"    </moby:mobyData>" + 
"  </moby:mobyContent>" + 
"</moby:MOBY>";

private String pubmedXMLMIM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
"<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">" + 
"  <moby:mobyContent>" + 
"    <moby:mobyData moby:queryID=\"sip_1_\">" + 
"      <moby:Simple moby:articleName=\"publications\">" + 
"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"aa1\" moby:namespace=\"nn1\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
"            <moby:String moby:id=\"aa2\" moby:namespace=\"nn2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
"            <moby:String moby:id=\"aa3\" moby:namespace=\"nn3\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
"            <moby:String moby:id=\"aa4\" moby:namespace=\"nn4\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
"            <moby:String moby:id=\"aa5\" moby:namespace=\"nn5\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
"            <moby:String moby:id=\"aa6\" moby:namespace=\"nn6\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
"            <moby:String moby:id=\"aa7\" moby:namespace=\"nn7\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
"            <moby:String moby:id=\"aa8\" moby:namespace=\"nn8\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" +
"    </moby:mobyData>" + 
"    <moby:mobyData moby:queryID=\"sip_2_\">" + 
"      <moby:Simple moby:articleName=\"publications\">" + 
"          <moby:Publication moby:id=\"11352659\" moby:namespace=\"PMID\">" + 
"            <moby:String moby:id=\"a1\" moby:namespace=\"n1\" moby:articleName=\"Author\">M Meiron</moby:String>" + 
"            <moby:String moby:id=\"a2\" moby:namespace=\"n2\" moby:articleName=\"Author\">R Anunu</moby:String>" + 
"            <moby:String moby:id=\"a3\" moby:namespace=\"n3\" moby:articleName=\"Author\">E J Scheinman</moby:String>" + 
"            <moby:String moby:id=\"a4\" moby:namespace=\"n4\" moby:articleName=\"Author\">S Hashmueli</moby:String>" + 
"            <moby:String moby:id=\"a5\" moby:namespace=\"n5\" moby:articleName=\"Author\">B Z Levi</moby:String>" + 
"            <moby:String moby:id=\"a6\" moby:namespace=\"n6\" moby:articleName=\"Abstract\">Vascular endothelial growth factor (VEGF) has a central role in normal as well as in tumor angiogenesis. As such, VEGF is subjected to multi-level regulation at the transcriptional, post-transcriptional, translational, and post-translational levels to ensure proper expression during embryogenesis and adulthood. Its mRNA contains an exceptionally long (1038 bp) 5\' untranslated region (5\'UTR), which has a role in transcriptional as well as translational regulation of VEGF expression. In this communication, we provide new evidence showing that an open reading frame (ORF) present in the 5\'UTR encodes for new putative isoforms of VEGF due to alternative translational initiation from CUG codons. Like VEGF, the translation of the new isoforms is not sensitive to stress signals such as anoxia. Most likely, these isoforms either possess new capabilities, which are different from the activity of the classical VEGF isoforms, or affect the efficiency and capacity of translational initiation from the canonical AUG codon.</moby:String>" + 
"            <moby:String moby:id=\"a7\" moby:namespace=\"n7\" moby:articleName=\"Title\">New isoforms of VEGF are translated from alternative initiation CUG codons located in its 5\'UTR.</moby:String>" + 
"            <moby:String moby:id=\"a8\" moby:namespace=\"n8\" moby:articleName=\"Journal\">Biochemical and biophysical research communications</moby:String>" + 
"          </moby:Publication>" + 
"        </moby:Simple>" +
"    </moby:mobyData>" + 
"  </moby:mobyContent>" + 
"</moby:MOBY>";

}
