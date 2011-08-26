/**
 * 
 */
package net.sf.taverna.t2.lineageService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lineageService.util.Arc;
import net.sf.taverna.t2.lineageService.util.ProcBinding;
import net.sf.taverna.t2.lineageService.util.ProvenanceProcessor;
import net.sf.taverna.t2.lineageService.util.Var;
import net.sf.taverna.t2.lineageService.util.VarBinding;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorOutputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.DataflowXMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * @author paolo
 *
 */
public class EventProcessor {

	private static final String OUTPUT_CONTAINER_PROCESSOR = "_OUTPUT_";
	private static final String INPUT_CONTAINER_PROCESSOR = "_INPUT_";
	private static final String TEST_EVENTS_FOLDER  = "webservice/log/TEST-EVENTS/event";


	static     	int eventCnt = 0;

	String wfInstanceID = null;

	ProvenanceWriter pw = null;

	ProvenanceQuery pq = null;

	/**
	 * @param pw 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
	public EventProcessor(ProvenanceWriter pw) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this.pw = pw;

		pq = new ProvenanceQuery();
	}



	/**
	 * this is the new version that makes use of the T2 deserializer
	 * @param content is a serialized dataflow (XML) -- this is parsed using the T2 Deserializer
	 * @return the wfInstanceRef for this workflow structure 
	 */
	public String processWorkflowStructure(String content)  {

//		System.out.println("incoming workflow structure:\n"+content);

		XMLDeserializer deserializer = new XMLDeserializerImpl();

		SAXBuilder builder = new SAXBuilder();
		Element el;
		try {

			el = builder.build(new StringReader(content)).detachRootElement();

			Dataflow df = deserializer.deserializeDataflow(el); // ,new HashMap<String, Element>());

			List<Var> vars = new ArrayList<Var>();

			////////
			// add workflow instance ID  -- this will come from the dataflow header
			///////

			wfInstanceID = df.getInternalIdentier();

			pw.addWFId(wfInstanceID);
			pw.addWFInstanceId(wfInstanceID);

			// CHECK whether there is now a distinction b/w instanceID and dataflow name

			////////
			//  add processors along with their variables
			///////
			List<? extends Processor> processors = df.getProcessors();

			for (Processor p: processors) {

				String pName = p.getLocalName();
				pw.addProcessor(pName, wfInstanceID);

				/////
				// add all input ports for this processor as input variables
				/////
				List<? extends ProcessorInputPort> inputs = p.getInputPorts();

				for (ProcessorInputPort ip: inputs) {

					Var inputVar = new Var();					

					inputVar.setPName(pName);
					inputVar.setWfInstanceRef(wfInstanceID);
					inputVar.setVName(ip.getName());
					inputVar.setTypeNestingLevel(ip.getDepth());
					inputVar.setInput(true);
					vars.add(inputVar);
				}

				/////
				// add all output ports for this processor as output variables
				/////
				List<? extends ProcessorOutputPort> outputs = p.getOutputPorts();

				for (ProcessorOutputPort op: outputs) {

					Var outputVar = new Var();					

					outputVar.setPName(pName);
					outputVar.setWfInstanceRef(wfInstanceID);
					outputVar.setVName(op.getName());
					outputVar.setTypeNestingLevel(op.getDepth());
					outputVar.setInput(false);
					vars.add(outputVar);
				}

			} // end for each processor

			//////
			// add inputs to entire dataflow
			//////

			String pName = INPUT_CONTAINER_PROCESSOR;

			List<? extends DataflowInputPort> inputPorts = df.getInputPorts();

			for (DataflowInputPort ip: inputPorts) {

				Var inputVar = new Var();					

				inputVar.setPName(pName);
				inputVar.setWfInstanceRef(wfInstanceID);
				inputVar.setVName(ip.getName());
				inputVar.setTypeNestingLevel(ip.getDepth());
				inputVar.setInput(true);
				vars.add(inputVar);
			}


			//////
			// add outputs of entire dataflow
			//////
			pName = OUTPUT_CONTAINER_PROCESSOR;

			List<? extends DataflowOutputPort> outputPorts = df.getOutputPorts();

			for (DataflowOutputPort op: outputPorts) {

				Var outputVar = new Var();					

				outputVar.setPName(pName);
				outputVar.setWfInstanceRef(wfInstanceID);
				outputVar.setVName(op.getName());
				outputVar.setTypeNestingLevel(op.getDepth());
				outputVar.setInput(false);
				vars.add(outputVar);
			}

			pw.addVariables(vars, wfInstanceID);

			//////
			// add arc records using the dataflow links
			// retrieving the processor names requires navigating from links to source/sink and from there to the processors
			//////
			List<? extends Datalink> links = df.getLinks();

			for (Datalink l: links)  {

				// TODO cover the case of arcs from an input and to an output to the entire dataflow

				String sourcePname = null;
				String sinkPname = null;

				if (l.getSource() instanceof ProcessorOutputPort) {
					sourcePname = ((ProcessorOutputPort) l.getSource()).getProcessor().getLocalName();
				} else {
					System.out.println("found link from dataflow input");
				}

				if (l.getSink() instanceof ProcessorInputPort) {
					sinkPname = ((ProcessorInputPort) l.getSink()).getProcessor().getLocalName(); 
				} else {
					System.out.println("found link to dataflow output");
				}

				if (sourcePname != null && sinkPname != null) {
					System.out.println("adding regular internal arc");

					pw.addArc(l.getSource().getName(), sourcePname, 
							l.getSink().getName(), sinkPname, 
							wfInstanceID);

				} else if ( sourcePname == null) {
					// link is from dataflow input 

					System.out.println("adding arc from dataflow input");

					pw.addArc(l.getSource().getName(), INPUT_CONTAINER_PROCESSOR, 
							l.getSink().getName(), sinkPname, 
							wfInstanceID);

				} else if  (sinkPname == null) {
					// link is to dataflow output

					System.out.println("adding arc to dataflow output");

					pw.addArc(l.getSource().getName(), sourcePname, 
							l.getSink().getName(), OUTPUT_CONTAINER_PROCESSOR, 
							wfInstanceID);
				}
			}			

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return wfInstanceID;
	}


	/**
	 * populate static portion of the DB -- OBSOLETE
	 * @param d
	 * @throws SQLException 
	 */
	public void processWorkflowStructure(Document d)  {

		System.out.println("processWorkflowStructure: ");

		Element root = d.getRootElement();

		try {

			// add workflow instance ID  -- this will come from the dataflow header
			wfInstanceID = getWorkflowID(root);

			pw.addWFId(wfInstanceID);
			pw.addWFInstanceId(wfInstanceID);

			// in the initial impl. the instance ID coincides with the dataflow id --
			// so we write both as soon as we see a new dataflow desription


//			add processors		
			String xpathExpr = "processors/processor";
			XPath xpath;
			try {
				xpath = XPath.newInstance(xpathExpr);
				List<Element> nodelist = (List<Element>) xpath.selectNodes(root);  // all processor nodes

				for (Element pElement: nodelist) {
					String pName = pElement.getAttributeValue("name");
					System.out.println("processor name: "+pName);

					pw.addProcessor(pName, wfInstanceID);
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			}		


//			add variables -- use the datalink elements
			List<Var> vars = new ArrayList<Var>();

			xpathExpr = "datalinks/datalink";

			try {
				xpath = XPath.newInstance(xpathExpr);
				List<Element> nodelist = (List<Element>) xpath.selectNodes(root);  // all datalinks nodes

				String pName, vName;
				Element processorEl, portEl = null;				

				for (Element el:nodelist) { // element is a datalink

					Element sinkEl = el.getChild("sink");

					Var sinkVar = new Var();					
					processorEl  = sinkEl.getChild("processor");

					if (processorEl == null) { 
						// sink + no processor = workflow output
						pName = OUTPUT_CONTAINER_PROCESSOR;
					} else {
						pName = processorEl.getText();
					}

					portEl  = sinkEl.getChild("inputport");
					vName = portEl.getText();
					sinkVar.setInput(true);  // a source is an input var
					sinkVar.setPName(pName);
					sinkVar.setVName(vName);						
					vars.add(sinkVar);

					Element sourceEl = el.getChild("source");

					Var sourceVar = new Var();					
					processorEl  = sourceEl.getChild("processor");

					if (processorEl == null) { 
						// sink + no processor = workflow output
						pName = INPUT_CONTAINER_PROCESSOR;
					} else {
						pName = processorEl.getText();
					}

					portEl  = sourceEl.getChild("outputport");
					vName = portEl.getText();
					sourceVar.setInput(false);  
					sourceVar.setPName(pName);
					sourceVar.setVName(vName);						
					vars.add(sourceVar);

					// add this pair as an arc in the workflow
					pw.addArc(sourceVar, sinkVar, wfInstanceID);

				}

				pw.addVariables(vars, wfInstanceID);

			} catch(Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}



	/**
	 * processes an elementary process execution event from T2
	 * @param d
	 */
	public void processProcessEvent(Document d) {

		Element root = d.getRootElement();

		String dataflowId = root.getAttributeValue("dataflowID");
		Element processorEl = root.getChild("processor");

		if (processorEl == null) {
			System.out.println("NULL processor found (dataflowID: "+dataflowId+")");
			return;
		}

		String processorId = processorEl.getAttributeValue("id");
		Element activityEl = processorEl.getChild("activity");

		if (activityEl == null) {
			System.out.println("NULL activity found (dataflowID: "+dataflowId+")");
			return;
		}

		String activityId = activityEl.getAttributeValue("id");	

		Element iterationEl = activityEl.getChild("iteration");			
		String iterationId = iterationEl.getAttributeValue("id");

		// record the processor /. activity binding
		try {

			ProcBinding pb = new ProcBinding();

			pb.setPNameRef(processorId);
			pb.setActName(activityId);
			pb.setIterationVector(extractIterationVector(iterationId));
			pb.setExecIDRef(dataflowId);

			pw.addProcessorBinding(pb);

		} catch (SQLException e) {
			System.out.println(" ==>> "+e.getMessage());
			// case of multiple key insertion. Ignore and continue.
			//	e.printStackTrace();
		}

		Element inputDataEl = iterationEl.getChild("inputdata");
		Element outputDataEl = iterationEl.getChild("outputdata");

		// process inputdata
		// 0 or more port elements
		List<Element> inputPorts = inputDataEl.getChildren("port");

		for (Element inputport: inputPorts) {

			String portName = inputport.getAttributeValue("name");
			System.out.println("input port for "+processorId+": "+portName);

			// value type may vary
			List<Element> valueElements = inputport.getChildren(); // hopefully in the right order...
			if (valueElements != null && valueElements.size() > 0) {

				Element valueEl = valueElements.get(0); // expect only 1 child
				processVarBinding(valueEl, processorId, portName, iterationId, dataflowId);
			}				
		}

		// process outputdata
		// 0 or more port elements
		List<Element> outputPorts = outputDataEl.getChildren("port");

		for (Element outputport: outputPorts) {

			String portName = outputport.getAttributeValue("name");
			System.out.println("output port for "+processorId+": "+portName);

			// value type may vary
			List<Element> valueElements = outputport.getChildren();
			if (valueElements != null && valueElements.size()>0) {

				Element valueEl = valueElements.get(0); // only really 1 child
				processVarBinding(valueEl, processorId, portName, iterationId, dataflowId);			}
		}
	}



	/**
	 * capture the default case where the value is not a list
	 * @param valueEl
	 * @param processorId
	 * @param portName
	 * @param iterationId
	 * @param dataflowId
	 */
	private void processVarBinding(
			Element valueEl, String processorId, String portName, 
			String iterationId, String dataflowId) {

		// uses the defaults:
		// collIdRef = null 
		// parentcollectionRef = null
		// positionInCollection = 1
		processVarBinding(valueEl, processorId, portName, null, 1, null, iterationId, dataflowId);
	}


	private void processVarBinding(Element valueEl, String processorId, String portName, 
			String collIdRef, int positionInCollection, String parentCollectionRef, 
			String iterationId, String dataflowId) {

		String valueType  = valueEl.getName();
		System.out.println("value element for "+processorId+": "+valueType);

		String iterationVector = extractIterationVector(iterationId);

		VarBinding vb = new VarBinding();

		vb.setWfInstanceRef(dataflowId);
		vb.setPNameRef(processorId);
		vb.setValueType(valueType);
		vb.setVarNameRef(portName);
		vb.setCollIDRef(collIdRef);
		vb.setPositionInColl(positionInCollection);
		vb.setIterationVector(iterationVector);		

		if (valueType.equals("literal")) {

			System.out.println("processing literal value");
			try {

				vb.setValue(valueEl.getAttributeValue("id"));

				pw.addVarBinding(vb);

			} catch (SQLException e) {
				System.out.println(" ==>> "+e.getMessage());
			}

		} else if (valueType.equals("referenceSet")) {

			String refValue = null;
			String ref   = null;   // used for non-literal values that need de-referencing

			System.out.println("processing dataDocument value");
			vb.setValue(valueEl.getAttributeValue("id"));
			vb.setRef(valueEl.getChildText("reference"));

			try {
				pw.addVarBinding(vb);
			} catch (SQLException e) {
				System.out.println(" ==>> "+e.getMessage());
//				e.printStackTrace();
			}

		} else if (valueType.equals("list")) {

			// add entries to the Collection and to the VarBinding tables
			// list id --> Collection.collId

			System.out.println("processing list value");
			String collId = valueEl.getAttributeValue("id");
			try {

				parentCollectionRef = pw.addCollection(processorId, collId, parentCollectionRef, iterationVector, portName, dataflowId);

				// iterate over each list element
				List<Element> listElements = valueEl.getChildren();

				positionInCollection = 1;
				// children can be any base type, including list itself -- so use recursion
				for (Element el:listElements) {
					processVarBinding(el, processorId, portName, collId, positionInCollection, parentCollectionRef, iterationId, dataflowId);
					positionInCollection++;
				}

			} catch (SQLException e) {
				System.out.println("SQL exception ==>> "+e.getMessage());
//				e.printStackTrace();
			}
		} else {
			System.out.println("unrecognized value type element for "+processorId+": "+valueType);
		}

	}


	/**
	 * dummy impl -- waiting for T2 to provide the real ID as part of the event message
	 * @param root
	 * @return
	 */
	public String getWorkflowID(Element root) {

		Element nameEl = root.getChild("name");

		if (nameEl != null)  return nameEl.getText();

		else return "N/A";

	}


	/**
	 * OBSOLETE: returns the iteration vector x,y,z,...  from [x,y,z,...]<p/>
	 * now returns the vector itself -- this is still experimental
	 * @param iteration
	 * @return
	 */
	String extractIterationVector(String iteration) {

//		return iteration;
		return iteration.substring(1, iteration.length()-1);
		// iteration is of the form "[n]" so we extract n
//		String iterationN = iteration.substring(1, iteration.length()-1);

//		if (iterationN.length() == 0) return 0;

//		return Integer.parseInt(iterationN);

	}


	/**
	 * assume content is XML but this is really immaterial
	 * @param content
	 * @param eventType
	 */
	public void saveXMLEvent(Document d, String eventType) {

		// save event for later inspection
		String fname = TEST_EVENTS_FOLDER+"_"+eventCnt++ +"_" + eventType+".xml";
		File f = new File(fname);

		XMLOutputter outputter = new XMLOutputter();

		try {
			outputter.output(d, new FileOutputStream(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("saved as file "+fname);
	}



	/**
	 * assume content is XML but this is really immaterial
	 * @param content
	 * @param eventType
	 * @throws IOException 
	 */
	public void saveEvent(String content, String eventType) throws IOException {

		// save event for later inspection
		String fname = TEST_EVENTS_FOLDER+"_"+eventCnt++ +"_" + eventType+".xml";
		File f = new File(fname);

		FileWriter fw = new FileWriter(fname);
		fw.write(content);
		fw.flush();
		fw.close();

		System.out.println("saved as file "+fname);
	}


	/**
	 * for each arc of the form (_INPUT_/I, P/V): propagate VarBinding for P/V to var _INPUT_/I <br/>
	 * @throws SQLException 
	 */
	public void fillInputVarBindings() throws SQLException {

		System.out.println("*** fillInputVarBindings: ***");

		// retrieve appropriate arcs
		Map<String,String> constraints = new HashMap<String,String>();
		constraints.put("sourcePnameRef", "_INPUT_");
		constraints.put("wfInstanceRef", wfInstanceID);
		List<Arc> arcs = pq.getArcs(constraints);

		// backpropagate VarBinding from the target var of the arc to the source
		for (Arc aArc:arcs) {

			System.out.println("propagating VarBinding from ["+aArc.getSinkPnameRef()+
					"/"+aArc.getSinkVarNameRef()+
					"] to input ["+
					aArc.getSourcePnameRef()+"/"+
					aArc.getSourceVarNameRef()+"]");

			// get the varBinding for the arc sinks
			Map<String,String> vbConstraints = new HashMap<String,String>();
			vbConstraints.put("VB.PNameRef", aArc.getSinkPnameRef());
			vbConstraints.put("VB.varNameRef", aArc.getSinkVarNameRef());
			vbConstraints.put("VB.wfInstanceRef", wfInstanceID);

			List<VarBinding> vbList = pq.getVarBindings(vbConstraints); // DB QUERY

			for (VarBinding vb:vbList) {
				//add a new VarBinding for the input

				vb.setPNameRef(aArc.getSourcePnameRef());
				vb.setVarNameRef(aArc.getSourceVarNameRef());
				// all other attributes are the same --> CHECK!!

				pw.addVarBinding(vb);				
			}
		}		
	}


	/**
	 * for each arc of the form (P/V, _OUTPUT_/O): propagate VarBinding for P/V to var _OUTPUT_/O <br/>
	 * @throws SQLException 
	 */
	public void fillOutputVarBindings() throws SQLException {

		System.out.println("*** fillOutputVarBindings: ***");

		// retrieve appropriate arcs
		Map<String,String> constraints = new HashMap<String,String>();
		constraints.put("sinkPnameRef", "_OUTPUT_");
		constraints.put("wfInstanceRef", wfInstanceID);
		List<Arc> arcs = pq.getArcs(constraints);

		// fowd propagate VarBinding from the source var of the arc to the output
		for (Arc aArc:arcs) {

			System.out.println("fwd propagating VarBinding from ["+aArc.getSourcePnameRef()+
					"/"+aArc.getSourceVarNameRef()+
					"] to input ["+
					aArc.getSinkPnameRef()+"/"+
					aArc.getSinkVarNameRef()+"]");

			// get the varBinding for the arc sinks
			Map<String,String> vbConstraints = new HashMap<String,String>();
			vbConstraints.put("VB.PNameRef", aArc.getSourcePnameRef());
			vbConstraints.put("VB.varNameRef", aArc.getSourceVarNameRef());
			vbConstraints.put("VB.wfInstanceRef", wfInstanceID);

			List<VarBinding> vbList = pq.getVarBindings(vbConstraints); // DB QUERY

			for (VarBinding vb:vbList) {
				//add a new VarBinding for the input

				System.out.println("found binding to propagate:");
				System.out.println(vb.getPNameRef()+"/"+vb.getVarNameRef()+"/"+vb.getWfInstanceRef()+"/"+vb.getIteration());

				vb.setPNameRef(aArc.getSinkPnameRef());
				vb.setVarNameRef(aArc.getSinkVarNameRef());
				// all other attributes are the same --> CHECK!!

				System.out.println(vb.toString());

				pw.addVarBinding(vb);	 // DB UPDATE			
			}

		}
	}



	/**
	 * propagates anl() through the graph, using a toposort alg
	 * @throws SQLException 
	 */
	public void propagateANL(String wfInstanceRef) throws SQLException {

		////////////////////////
		// PHASE I: toposort the processors in the whole graph
		////////////////////////

		// fetch processors along with the count of their predecessors		
		Map<String, Integer> processorsLinks = pq.getProcessorsIncomingLinks(wfInstanceRef);

		// holds sorted elements
		List<String> L = new ArrayList<String>();

		// temp queue
		List<String> Q = new ArrayList<String>();

//		System.out.println("propagateANL: processors in the graph");

		// init Q with root nodes
		for (Map.Entry<String, Integer> entry : processorsLinks.entrySet()) {

//			System.out.println(entry.getKey()+" has "+entry.getValue().intValue()+" predecessors");

			if (entry.getValue().intValue() == 0) { Q.add(entry.getKey()); }
		}		

		while (!Q.isEmpty())  {

			String current = Q.remove(0);
			L.add(current);

			List<String> successors = pq.getSuccProcessors(current, wfInstanceRef);

			for (String succ: successors) {
				// decrease edge count for each successor processor

				Integer cnt = processorsLinks.get(succ); 

				processorsLinks.put(succ, new Integer(cnt.intValue()-1));

				if (cnt.intValue() == 1) {
					Q.add(succ);
				}
			}
		} // end loop on Q

		System.out.println("toposort:");
		for (String p:L) { System.out.println(p); }

		// sorted processor names in L at this point		
		// process them in order
		for (String pname: L) {

			// process pname's inputs -- set ANL to be the DNL if not set in prior steps
			List<Var> inputs = pq.getInputVars(pname, wfInstanceRef);

			int totalANL = 0;
			for (Var iv: inputs) {
				if (iv.isANLset() == false) {
					iv.setActualNestingLevel(iv.getTypeNestingLevel());
					iv.setANLset(true);					
					pq.updateVar(iv);
				}
				totalANL += iv.getActualNestingLevel() - iv.getTypeNestingLevel();
			}

			// process pname's outputs -- set ANL based on the sum formula (see paper)
			List<Var> outputs = pq.getOutputVars(pname, wfInstanceRef);
			for (Var ov: outputs) {

				ov.setActualNestingLevel(ov.getTypeNestingLevel() + totalANL);
				ov.setANLset(true);		
				pq.updateVar(ov);

				// propagate this through all the links from this var
				List<Var> successors = pq.getSuccVars(pname, ov.getVName(), wfInstanceRef);
				
				for (Var v: successors) {
					v.setActualNestingLevel(ov.getActualNestingLevel());
					v.setANLset(true);
					pq.updateVar(v);
				}
			}			
		}
	}


}
