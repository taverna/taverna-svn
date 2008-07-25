/**
 * 
 */
package net.sf.taverna.t2.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.service.util.Arc;
import net.sf.taverna.t2.service.util.ProcBinding;
import net.sf.taverna.t2.service.util.Var;
import net.sf.taverna.t2.service.util.VarBinding;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
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
	 * populate static portion of the DB
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

		} else if (valueType.equals("dataDocument")) {

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



	public void saveEvent(Document d, String eventType) {

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



}
