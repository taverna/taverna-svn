/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

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
//import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BiomobyTask.class);

    private static final int INVOCATION_TIMEOUT = 0;

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
                String inputXML = null;
                inputXML = (String) inputThing.getDataObject();
                // TODO if passed in a collection and only simples are needed,
                // parse the collection

                // do the task and populate outputXML
                String methodName = ((BiomobyProcessor) proc).getName();
                String serviceEndpoint = ((BiomobyProcessor) proc)
                        .getEndpoint().toExternalForm();
                String outputXML = new CentralImpl(serviceEndpoint,
                        "http://biomoby.org/").call(methodName, inputXML);

                // create a map of all the outputs:
                // output & all ports created by this service
                // this means retrieving the ports and looking for the data to
                // put in there
                // if service returns empty, this may be empty
                Map outputMap = new HashMap();
                // failed (use MobyObjectClassNSImpl for skeleton)
                outputMap.put("output", new DataThing(outputXML));

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
                        inputXML = (String) inputThing.getDataObject();
                        Document d = XMLUtilities.getDOMDocument(inputXML);
                        List list = d.getRootElement().getChild("mobyContent",
                                MobyObjectClassNSImpl.MOBYNS).getChild(
                                "mobyData", MobyObjectClassNSImpl.MOBYNS)
                                .getChildren("Simple",
                                        MobyObjectClassNSImpl.MOBYNS);
                        for (Iterator i = list.iterator(); i.hasNext();) {
                            Element nino = (Element) i.next();
                            i.remove();
                            mobyData.addContent(nino.detach());
                        }
                        // process collections now
                        list = d.getRootElement().getChild("mobyContent",
                                MobyObjectClassNSImpl.MOBYNS).getChild(
                                "mobyData", MobyObjectClassNSImpl.MOBYNS)
                                .getChildren("Collection",
                                        MobyObjectClassNSImpl.MOBYNS);

                        for (Iterator i = list.iterator(); i.hasNext();) {
                            mobyData.addContent(((Element) i.next()).detach());
                        }
                    }
                }

                // do the task and populate outputXML
                String methodName = ((BiomobyProcessor) proc).getName();
                String serviceEndpoint = ((BiomobyProcessor) proc)
                        .getEndpoint().toExternalForm();
                //System.out.println("Mobycentral soap call\r\n"
                // + new MobyObjectClassNSImpl().toString(root));
                String outputXML = new CentralImpl(serviceEndpoint,
                        "http://biomoby.org/").call(methodName,
                        new MobyObjectClassNSImpl().toString(root));
                //System.out.println("Mobycentral sent me\r\n" + outputXML);
                Map outputMap = new HashMap();
                outputMap.put("output", new DataThing(outputXML));
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

    private void processOutputPorts(String outputXML, Map outputMap) {
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
                                documentElement, objectType, "", null);
                        if (mobyCollection != null)
                            outputMap.put(name, new DataThing(mobyCollection));
                    } else {
                        // we have an article name, so extract it and do the
                        // same as above
                        String objectType = name
                                .substring(0, name.indexOf("'"));
                        String artName = name.substring(name.indexOf("'") + 1,
                                name.indexOf("'"));
                        String mobyCollection = XMLUtilities.getMobyCollection(
                                documentElement, objectType, artName, null);
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
                        //System.out.println(""+new MobyObjectClassNSImpl().toString(documentElement));
                        String mobySimple = XMLUtilities.getMobyElement(
                                documentElement, objectType, "", null);
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
                                documentElement, objectType, artName, null);
                        if (mobySimple != null)
                            outputMap.put(name, new DataThing(mobySimple));
                    }
                }

            }
        }
    }

}
