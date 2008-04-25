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
import java.util.List;

import net.sf.taverna.t2.service.util.Var;

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
	
	static     	int eventCnt = 0;

	ProvenanceWriter pw = null;
	
	/**
	 * @param pw 
	 * 
	 */
	public EventProcessor(ProvenanceWriter pw) {
		this.pw = pw;
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
			String wfId = getWorkflowID(root);

			pw.addWFId(wfId);

//			add processors		
			String xpathExpr = "processors/processor";
			XPath xpath;
			try {
				xpath = XPath.newInstance(xpathExpr);
				List<Element> nodelist = (List<Element>) xpath.selectNodes(root);  // all processor nodes

				for (Element pElement: nodelist) {
					String pName = pElement.getAttributeValue("name");
					System.out.println("processor name: "+pName);

					pw.addProcessor(pName, wfId);
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
					pw.addArc(sourceVar, sinkVar, wfId);

				}
				
				pw.addVariables(vars, wfId);
				
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
			pw.addProcessorBinding(processorId, activityId, iterationId, dataflowId);
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
			List<Element> valueElements = inputport.getChildren();
			if (valueElements != null && valueElements.size() > 0) {
				
				Element valueEl = valueElements.get(0); // only really 1 child
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
	private void processVarBinding(Element valueEl, String processorId,
			String portName, String iterationId, String dataflowId) {
		
		// uses the defaults:
		// collIdRef = null 
		// parentcollectionRef = null
		// positionInCollection = 1
		processVarBinding(valueEl, processorId, portName, null, 1, null, iterationId, dataflowId);
	}


	private void processVarBinding(Element valueEl, String processorId,
			String portName, String collIdRef, int positionInCollection, String parentCollectionRef, String iterationId, String dataflowId) {
			
		String valueType  = valueEl.getName();
		System.out.println("value element for "+processorId+": "+valueType);
		
		if (valueType.equals("literal")) {
			
			String literalValue = null;
			
			System.out.println("processing literal value");
			literalValue = valueEl.getAttributeValue("id");
			try {
				pw.addVarBinding(processorId, literalValue, "NULL", valueType, portName, collIdRef, positionInCollection, iterationId, dataflowId);
			} catch (SQLException e) {
				System.out.println(" ==>> "+e.getMessage());
	//			e.printStackTrace();
			}
			
		} else if (valueType.equals("dataDocument")) {

			String refValue = null;
			String ref   = null;   // used for non-literal values that need de-referencing

			System.out.println("processing dataDocument value");
			refValue = valueEl.getAttributeValue("id");
			ref   = valueEl.getChildText("reference");
			try {
				pw.addVarBinding(processorId, refValue, ref, valueType, portName, collIdRef, positionInCollection, iterationId, dataflowId);
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
				
				parentCollectionRef = pw.addCollection(processorId, collId, parentCollectionRef, portName, dataflowId);
				
				// iterate over each list element
				List<Element> listElements = valueEl.getChildren();
				
				positionInCollection = 1;
				// children can be any base type, including list itself -- so use recursion
				for (Element el:listElements) {
					processVarBinding(el, processorId, portName, collId, positionInCollection, parentCollectionRef, iterationId, dataflowId);
					positionInCollection++;
				}
				
			} catch (SQLException e) {
				System.out.println(" ==>> "+e.getMessage());
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

	
	
	public void saveEvent(Document d, String eventType) {

		// save event for later inspection
		String fname = "src/main/resources/TEST-EVENTS/event_"+eventCnt++ +"_" + eventType+".xml";
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

	
	
}
