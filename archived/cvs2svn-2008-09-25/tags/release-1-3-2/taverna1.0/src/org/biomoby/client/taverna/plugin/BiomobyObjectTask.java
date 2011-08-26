/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Element;
import org.jdom.Namespace;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyObjectTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BiomobyTask.class);

    //private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;

    Namespace mobyNS = CreateMobyData.mobyNS;

    public BiomobyObjectTask(Processor p) {
        this.proc = p;
    }

    public Map execute(Map inputMap, ProcessorTask parentTask)
            throws TaskExecutionException {
        // the possible inputs to create 'mobyData' from
        InputPort[] inputPorts = parentTask.getProcessor().getBoundInputPorts();
        // initialize the namespace and id fields
        String namespace = "";
        String id = "";
        String article = "";
        boolean isPrimitiveType = inputMap.containsKey("value");
        String objectName = ((BiomobyObjectProcessor) parentTask.getProcessor())
                .getServiceName();
        HashMap outputMap = new HashMap();
        // would like to use the MobyObjectClass to generate a skeleton
        MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(((BiomobyObjectProcessor) parentTask.getProcessor()).getCentral().getRegistryEndpoint());
        if (isPrimitiveType) {
            try {
                DataThing inputThing;
                try {
                    inputThing = (DataThing) inputMap.get("namespace");
                    namespace = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                try {
                    inputThing = (DataThing) inputMap.get("id");
                    id = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                try {
                    inputThing = (DataThing) inputMap.get("article name");
                    article = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                Element mobyObjectElement = mo.createObject(objectName,
                        namespace, id, article);

                inputThing = (DataThing) inputMap.get("value");
                String value = (String) inputThing.getDataObject();
                mobyObjectElement.setText(value);

                String mobyDataString = mo.toString(XMLUtilities
                        .createMobyDataElementWrapper(mo.toSimple(
                                mobyObjectElement, "")));
                outputMap.put("mobyData", new DataThing(mobyDataString));

            } catch (Exception ex) {
                // details of other exceptions will appear only in a log
                ex.printStackTrace();
                logger.error("Error creating biomoby object for biomoby", ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem creating biomoby object (see details in log)");
                tee.initCause(ex);
                throw tee;
            }
        } else {
            // Situation where simples are feeding into this non primitive type
            try {
                DataThing inputThing;
                try {
                    inputThing = (DataThing) inputMap.get("namespace");
                    namespace = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                try {
                    inputThing = (DataThing) inputMap.get("id");
                    id = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                try {
                    inputThing = (DataThing) inputMap.get("article name");
                    article = (String) inputThing.getDataObject();
                } catch (Exception e) {
                }

                Element mobyObjectElement = mo.createObject(objectName,
                        namespace, id, article);
                // using the inputs, iterate through and fill in data
                for (int x = 0; x < inputPorts.length; x++) {
                    String portName = inputPorts[x].getName();
                    if (!(portName.equalsIgnoreCase("namespace")
                            || portName.equalsIgnoreCase("id") || portName
                            .equalsIgnoreCase("article name"))) {
                        String type = portName.substring(0, portName
                                .indexOf("("));
                        String articleName = portName.substring(
                                type.length() + 1, portName.length() - 1);
                        inputThing = null;
                        try {
                            inputThing = (DataThing) inputMap.get(portName);
                        } catch (Exception e) {

                        }
                        if (inputThing != null) {
                            String value = (String) inputThing.getDataObject();
                            // TODO make sure that we have the right element -
                            // use the articleName
                            Element valueElement = (Element) (XMLUtilities
                                    .getDOMDocument(value)
                                    .getRootElement()
                                    .getChild("mobyContent",
                                            MobyObjectClassNSImpl.MOBYNS).getChild(
                                                    "mobyData", MobyObjectClassNSImpl.MOBYNS)
                                                    .getChild("Simple",
                                                            MobyObjectClassNSImpl.MOBYNS).getChild(type, mobyNS));

                            Element _child = mo.getObject(mobyObjectElement,
                                    type, articleName);
                            valueElement.setAttribute("articleName", articleName, mobyNS);
//                            Element _newChild = mo.createObject(type, _ns, _id,
//                                    articleName);
//                            _newChild.setText(valueElement
//                                    .getText());
//                            _newChild.addContent(_child.cloneContent());
                            // import and replace the node
                            mobyObjectElement.removeContent(
                                    _child);
                            mobyObjectElement.addContent(valueElement.detach());
                        }
                    }
                }
                String mobyDataString = mo.toString(XMLUtilities
                        .createMobyDataElementWrapper(mo.toSimple(
                                mobyObjectElement, article)));
                outputMap.put("mobyData", new DataThing(mobyDataString));

            } catch (Exception ex) {
                // details of other exceptions will appear only in a log
                ex.printStackTrace();
                logger.error("Error creating biomoby object for biomoby", ex);
                TaskExecutionException tee = new TaskExecutionException(
                        "Task failed due to problem creating biomoby object (see details in log)");
                tee.initCause(ex);
                throw tee;
            }
        }
        return outputMap;
    }
}
