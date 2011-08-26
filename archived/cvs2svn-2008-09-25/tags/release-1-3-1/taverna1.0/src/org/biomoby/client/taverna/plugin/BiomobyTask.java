/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby
 * Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BiomobyTask.class);

    //private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;

    Namespace mobyNS = MobyObjectClassNSImpl.MOBYNS;

    public BiomobyTask(Processor p) {
        this.proc = p;
    }

    public Map execute(Map inputMap, ProcessorTask parentTask)
            throws TaskExecutionException {
        if (inputMap.containsKey("input")) {
            // input port takes precedence over other ports
            try {
                DataThing inputThing = (DataThing) inputMap.get("input");

                InputPort myInput = null;
                InputPort[] myInputs = proc.getInputPorts();
                for (int i = 0; i < myInputs.length; i++) {
                    if (myInputs[i].getName().equalsIgnoreCase("input")) {
                        myInput = myInputs[i];
                        break;
                    }
                }
                if (myInput == null)
                    throw new TaskExecutionException(
                            "The port 'input' was not specified correctly.");
                String inputType = myInput.getSyntacticType();
                // If this is 'text/xml' then the biomoby service consumes
                // a simple and there is no processing to do as that's what we
                // have
                // If this is l('text/xml') biomoby expects a collection but
                // we have a java List of simples - need to convert this into
                // a biomoby collection document
                String inputXML = null;
                
                if (inputType.equals("'text/xml'")) {
                    inputXML = (String) inputThing.getDataObject();
                } else {
                    // List of strings containing simple biomoby objects
                    List simpleInputs = (List) inputThing.getDataObject();
                    // Create the empty collection document
                    Element root = new Element("MOBY", mobyNS);
                    Element content = new Element("mobyContent", mobyNS);
                    root.addContent(content);
                    Element data = new Element("mobyData", mobyNS);
                    data.setAttribute("queryID", "a1", mobyNS);
                    content.addContent(data);
                    Element collectionElement = new Element("Collection",
                            mobyNS);
                    collectionElement.setAttribute("articleName", "", mobyNS);
                    // It is this collection element that's going to acquire the
                    // simples
                    for (Iterator i = simpleInputs.iterator(); i.hasNext();) {
                        String s = (String)i.next();
                    	Element el = XMLUtilities.getDOMDocument(s).getRootElement();
                        Element mobyDataElement = el.getChild("mobyContent",
                                mobyNS).getChild("mobyData", mobyNS);
                        // Remove the single 'Simple' child from this...
                        Element simpleElement = (Element) mobyDataElement
                                .getChildren().get(0);
                        // Tag the simple element onto the collection.
                        collectionElement.addContent(simpleElement.detach());
                    }
                    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
                    inputXML = xo.outputString(new Document(root));
                    // Iterate and create the collection,
                    // ....inputXML = collectionThing
                }

                // do the task and populate outputXML
                String methodName = ((BiomobyProcessor) proc).getServiceName();
                String serviceEndpoint = ((BiomobyProcessor) proc)
                        .getEndpoint().toExternalForm();
                String outputXML = new CentralImpl(serviceEndpoint,
                        "http://biomoby.org/").call(methodName, inputXML);
                Map outputMap = new HashMap();
                // goes through and creates the port 'output'
                processOutputPort(outputXML, outputMap);
                // create the other ports
                processOutputPorts(outputXML, outputMap);
                return outputMap;

            } catch (MobyException ex) {
                // a MobyException should be already reasonably formatted
                logger
                        .error(
                                "Error invoking biomoby service for biomoby. A MobyException caught",
                                ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem invoking biomoby service.\n"
                                + ex.getMessage());
                tee.initCause(ex);
                throw tee;

            } catch (Exception ex) {
                // details of other exceptions will appear only in a log
                ex.printStackTrace();
                logger.error("Error invoking biomoby service for biomoby", ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem invoking biomoby service (see details in log)");
                tee.initCause(ex);
                throw tee;
            }
        } else {
            // input port takes precedence over other ports
            try {
                String inputXML = null;
                InputPort[] inputPorts = proc.getBoundInputPorts();
                // create the main xml element that we will add
                // simples/collections too
                Document doc = XMLUtilities.createDomDocument();
                Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
                Element content = new Element("mobyContent",
                        MobyObjectClassNSImpl.MOBYNS);
                Element mobyData = new Element("mobyData",
                        MobyObjectClassNSImpl.MOBYNS);
                mobyData.setAttribute("queryID", "a1",
                        MobyObjectClassNSImpl.MOBYNS);
                root.addContent(content);
                doc.addContent(root);
                content.addContent(mobyData);
                // from now on, append data to mobyData!
                for (int x = 0; x < inputPorts.length; x++) {
                    // check for article name
                    String name = inputPorts[x].getName();
                    if (!name.equalsIgnoreCase("input")) {
                        DataThing inputThing = (DataThing) inputMap.get(name);
                        // TODO check for null?
                        if (!inputThing.getSyntacticType().toString()
                                .startsWith("l(")) {
                            // no list, we have a Simple!
                            inputXML = (String) inputThing.getDataObject();
                            Document d = XMLUtilities.getDOMDocument(inputXML);
                            
                            // check whether we have a collection of datatypes rather than a single simple
                            List simpleCollection = XMLUtilities.createMobySimpleListFromCollection(inputXML, ((BiomobyProcessor) proc).getMobyEndpoint());
                            if (simpleCollection.size() == 0) {
                            List list = d.getRootElement().getChild(
										"mobyContent",
										MobyObjectClassNSImpl.MOBYNS).getChild(
										"mobyData",
										MobyObjectClassNSImpl.MOBYNS)
										.getChildren("Simple",
												MobyObjectClassNSImpl.MOBYNS);
								for (Iterator i = list.iterator(); i.hasNext();) {
									Element nino = (Element) i.next();
									i.remove();
									mobyData.addContent(nino.detach());
								}
							} else {
								root = XMLUtilities.createMultipleInvocationMessageFromList(simpleCollection);
							}
                        } else {
                            // this is a Collection!
                            for (Iterator it = inputThing.childIterator(); it
                                    .hasNext();) {
                                DataThing dt = (DataThing) it.next();
                                inputXML = (String) dt.getDataObject();
                                Document d = XMLUtilities
                                        .getDOMDocument(inputXML);
                                // process collections now
                                List list = d.getRootElement().getChild(
                                        "mobyContent",
                                        MobyObjectClassNSImpl.MOBYNS).getChild(
                                        "mobyData",
                                        MobyObjectClassNSImpl.MOBYNS)
                                        .getChildren("Collection",
                                                MobyObjectClassNSImpl.MOBYNS);
                                for (Iterator i = list.iterator(); i.hasNext();) {
                                    Element colEl = (Element) i.next();
                                    i.remove();
                                    mobyData.addContent(colEl.detach());
                                }
                            }
                        }
                    }
                }

                // do the task and populate outputXML
                String methodName = ((BiomobyProcessor) proc).getServiceName();
                String serviceEndpoint = ((BiomobyProcessor) proc)
                        .getEndpoint().toExternalForm();
//                System.out.println("Mobycentral soap call\r\n"
//                 + new MobyObjectClassNSImpl(((BiomobyProcessor) proc).getMobyEndpoint()).toString(root));
                String outputXML = new CentralImpl(serviceEndpoint,
                        "http://biomoby.org/").call(methodName,
                        new MobyObjectClassNSImpl(((BiomobyProcessor) proc)
                                .getMobyEndpoint()).toString(root));
                Map outputMap = new HashMap();
                // goes through and creates the port 'output'
                processOutputPort(outputXML, outputMap);
                processOutputPorts(outputXML, outputMap);
                return outputMap;

            } catch (MobyException ex) {
                // a MobyException should be already reasonably formatted
                logger
                        .error(
                                "Error invoking biomoby service for biomoby. A MobyException caught",
                                ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem invoking biomoby service.\n"
                                + ex.getMessage());
                tee.initCause(ex);
                throw tee;

            } catch (Exception ex) {
                // details of other exceptions will appear only in a log
                ex.printStackTrace();
                logger.error("Error invoking biomoby service for biomoby", ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem invoking biomoby service (see details in log)");
                tee.initCause(ex);
                throw tee;
            }
        }
    }

	private void processOutputPort(String outputXML, Map outputMap) throws TaskExecutionException, JDOMException, IOException {
		OutputPort myOutput = null;
		OutputPort[] myOutputs = proc.getOutputPorts();
		for (int i = 0; i < myOutputs.length; i++) {
		    if (myOutputs[i].getName().equalsIgnoreCase("output"))
		        myOutput = myOutputs[i];
		}
		if (myOutput == null)
		    throw new TaskExecutionException("output port is invalid.");
		String outputType = myOutput.getSyntacticType();
		//System.out.println(outputXML);
		// Will be either 'text/xml' or l('text/xml')

		if (outputType.equals("'text/xml'")) {
		    outputMap.put("output", new DataThing(outputXML));
		} else {
		    List outputList = new ArrayList();
		    // Drill into the output xml document creating
		    // a list of strings containing simple types
		    // add them to the outputList

		    // This is in the 'outputXML'
		    // --------------------------
		    //        <?xml version="1.0" encoding="UTF-8"?>
		    //        <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
		    //          <moby:mobyContent>
		    //           <moby:mobyData queryID='b1'>
		    //               <Collection articleName="mySequenceCollection">
		    //                  <Simple>
		    //                   <Object namespace="Genbank/gi" id="163483"/>
		    //                  </Simple>
		    //                  <Simple>
		    //                   <Object namespace="Genbank/gi" id="244355"/>
		    //                  </Simple>
		    //                  <Simple>
		    //                   <Object namespace="Genbank/gi" id="533253"/>
		    //                  </Simple>
		    //                  <Simple>
		    //                   <Object namespace="Genbank/gi" id="745290"/>
		    //                  </Simple>
		    //                </Collection>
		    //           </moby:mobyData>
		    //          </moby:mobyContent>
		    //        </moby:MOBY>

		    // And this is what I want to create - several times:
		    // --------------------------------------------------
		    //        <?xml version="1.0" encoding="UTF-8"?>
		    //        <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
		    //           <moby:mobyContent>
		    //               <moby:mobyData queryID='a1'>
		    //                    <Simple articleName=''>
		    //                       <Object namespace="Genbank/gi" id="163483"/>
		    //                    </Simple>
		    //               </moby:mobyData>
		    //           </moby:mobyContent>
		    //        </moby:MOBY>

		    // Create a DOM document from the resulting XML
		    SAXBuilder saxBuilder = new SAXBuilder();
		    Document doc = saxBuilder.build(new InputSource(
		            new StringReader(outputXML)));
		    Element mobyElement = doc.getRootElement();
		    Element mobyDataElement = mobyElement.getChild(
		            "mobyContent", mobyNS).getChild("mobyData", mobyNS);

		    Element collectionElement = mobyDataElement.getChild(
		            "Collection", mobyNS);
		    if (collectionElement != null) {
		        List simpleElements = new ArrayList(collectionElement
		                .getChildren());
		        for (Iterator i = simpleElements.iterator(); i
		                .hasNext();) {
		            Element simpleElement = (Element) i.next();

		            Element newRoot = new Element("MOBY", mobyNS);
		            Element newMobyContent = new Element("mobyContent",
		                    mobyNS);
		            newRoot.addContent(newMobyContent);
		            Element newMobyData = new Element("mobyData",
		                    mobyNS);
		            newMobyData.setAttribute("queryID", "a1", mobyNS);
		            newMobyContent.addContent(newMobyData);
		            newMobyData.addContent(simpleElement.detach());
		            XMLOutputter xo = new XMLOutputter();
		            String outputItemString = xo
		                    .outputString(new Document(newRoot));
		            outputList.add(outputItemString);
		        }
		    }

		    // Return the list (may be empty)
		    outputMap.put("output", new DataThing(outputList));
		    // TODO think of how to output a list (collection)

		}
	}

    private void processOutputPorts(String outputXML, Map outputMap) throws MobyException {
        // fill in the supplementary moby object ports
        OutputPort[] outputPorts = proc.getBoundOutputPorts();
        Document doc = XMLUtilities.getDOMDocument(outputXML);
        for (int x = 0; x < outputPorts.length; x++) {
            String name = outputPorts[x].getName();
            if (!name.equalsIgnoreCase("output")) {
                // join the data to the output port by parsing outputXML
                // 'empty' ports will have empty skeleton
                Element documentElement = doc.getRootElement();
                if (name.indexOf("(Collection - '") > 0) {
                    // TODO process a collection
                    if (name.indexOf("MobyCollection") > 0) {
                        // parse out the MyCollection because this object doesnt
                        // have
                        // a name
                        String objectType = name
                                .substring(0, name.indexOf("("));
                        String mobyCollection = XMLUtilities.getMobyCollection(
                                documentElement, objectType, "",
                                ((BiomobyProcessor) proc).getMobyEndpoint());
                        if (mobyCollection != null)
                            outputMap.put(name, new DataThing(mobyCollection));
                    } else {
                        // we have an article name, so extract it and do the
                        // same as above
                        String objectType = name
                                .substring(0, name.indexOf("("));
                        String artName = name.substring(name.indexOf("'") + 1,
                                name.lastIndexOf("'")); // modified
                        String mobyCollection = XMLUtilities.getMobyCollection(
                                documentElement, objectType, artName,
                                ((BiomobyProcessor) proc).getMobyEndpoint());
                        if (mobyCollection != null)
                            outputMap.put(name, new DataThing(mobyCollection));
                    }
                } else {
                    // process simples
                    if (name.indexOf("(_ANON_)") > 0) {
                        // parse out the _ANON_ because this object doesnt have
                        // a name
                        String objectType = name
                                .substring(0, name.indexOf("("));
                        //System.out.println(""+new
                        // MobyObjectClassNSImpl().toString(documentElement));
                        String mobySimple = XMLUtilities.getMobyElement(
                                documentElement, objectType, "", null,
                                ((BiomobyProcessor) proc).getMobyEndpoint());
                        if (mobySimple != null)
                            outputMap.put(name, new DataThing(mobySimple));
                    } else {
                        // we have an article name, so extract it and do the
                        // same as above
                        String objectType = name
                                .substring(0, name.indexOf("("));
                        String artName = name.substring(name.indexOf("(") + 1,
                                name.indexOf(")"));
                        String mobySimple = XMLUtilities.getMobyElement(
                                documentElement, objectType, artName, null,
                                ((BiomobyProcessor) proc).getMobyEndpoint());
                        if (mobySimple != null)
                            outputMap.put(name, new DataThing(mobySimple));
                    }
                }
            }
        }
    }

}