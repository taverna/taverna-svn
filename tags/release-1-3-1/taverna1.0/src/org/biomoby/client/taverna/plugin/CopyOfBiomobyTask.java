package org.biomoby.client.taverna.plugin;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.MobyException;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class CopyOfBiomobyTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(CopyOfBiomobyTask.class);

    private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;

    Namespace mobyNS = CreateMobyData.mobyNS;

    public CopyOfBiomobyTask(Processor p) {
        this.proc = p;
    }

    public Map execute(Map inputMap, ProcessorTask parentTask)
            throws TaskExecutionException {

        try {
            DataThing inputThing = (DataThing) inputMap.get("input");

            InputPort myInput = proc.getInputPorts()[0];
            String inputType = myInput.getSyntacticType();
            // If this is 'text/xml' then the biomoby service consumes
            // a simple and there is no processing to do as that's what we have
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
                Element collectionElement = new Element("Collection", mobyNS);
                collectionElement.setAttribute("articleName", "", mobyNS);
                // It is this collection element that's going to acquire the simples
                for (Iterator i = simpleInputs.iterator(); i.hasNext();) {
                    Element el = (Element) i.next();
                    Element mobyDataElement = el
                            .getChild("mobyContent", mobyNS).getChild(
                                    "mobyData", mobyNS);
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
            String methodName = ((BiomobyProcessor) proc).getName();
            String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint()
                    .toExternalForm();
            String outputXML = new CentralImpl(serviceEndpoint,
                    "http://biomoby.org/").call(methodName, inputXML);
            Map outputMap = new HashMap();

            OutputPort myOutput = proc.getOutputPorts()[0];
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
                //           <moby:mobyData  queryID='b1'>
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
                Element mobyDataElement = mobyElement.getChild("mobyContent",
                        mobyNS).getChild("mobyData", mobyNS);

                Element collectionElement = mobyDataElement.getChild(
                        "Collection", mobyNS);
                if (collectionElement != null) {
                    List simpleElements = new ArrayList(collectionElement
                            .getChildren());
                    for (Iterator i = simpleElements.iterator(); i.hasNext();) {
                        Element simpleElement = (Element) i.next();

                        Element newRoot = new Element("MOBY", mobyNS);
                        Element newMobyContent = new Element("mobyContent",
                                mobyNS);
                        newRoot.addContent(newMobyContent);
                        Element newMobyData = new Element("mobyData", mobyNS);
                        newMobyData.setAttribute("queryID", "a1", mobyNS);
                        newMobyContent.addContent(newMobyData);
                        newMobyData.addContent(simpleElement.detach());
                        XMLOutputter xo = new XMLOutputter();
                        String outputItemString = xo.outputString(new Document(
                                newRoot));
                        outputList.add(outputItemString);
                    }
                }

                // Return the list (may be empty)
                outputMap.put("output", new DataThing(outputList));
            }
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
