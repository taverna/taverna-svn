/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.jdom.Element;
import org.jdom.Namespace;
import org.biomoby.registry.meta.Registry;
import org.biomoby.shared.data.MobyDataInt;
import org.biomoby.shared.data.MobyDataString;
import org.biomoby.shared.data.MobyDataBoolean;
import org.biomoby.shared.data.MobyDataFloat;
import org.biomoby.shared.data.MobyDataDateTime;
import org.biomoby.shared.data.MobyDataInstance;
import org.biomoby.shared.data.MobyDataComposite;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.registry.meta.Registry;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class BiomobyObjectTask implements ProcessorTaskWorker {

    private static Logger logger  = Logger.getLogger(BiomobyTask.class);

    //private static final int INVOCATION_TIMEOUT = 0;

    private Processor proc;

    Namespace mobyNS = XMLUtilities.MOBY_NS;

    public BiomobyObjectTask(Processor p) {
        this.proc = p;
        // doing this for no reason other than to read the variable proc
        proc.isBoring();
    }

    @SuppressWarnings("unchecked")
	public Map execute(Map inputMap, IProcessorTask parentTask)
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
        
        String registryEndpoint = ((BiomobyObjectProcessor) parentTask.getProcessor()).getCentral().getRegistryEndpoint();
        Registry mRegistry = new Registry(registryEndpoint,registryEndpoint,"http://domain.com/MOBY/Central");
        
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
                
                inputThing = (DataThing) inputMap.get("value");
                String value = (String) inputThing.getDataObject();
                
                if (objectName.equals("String")) {
                    if (value == null) {
                	value = "";
                    }
                    MobyDataString d = new MobyDataString(value,mRegistry);
                    d.setId(id);
                    MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                    if (mNamespace != null)
                	d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                    d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                    outputMap.put("mobyData", 
                	    new DataThing(XMLUtilities.createMobyDataElementWrapper(
                		    "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                } else if (objectName.equals("Float")) {
                    if (value == null || value.trim().equals("")) {
                	MobyDataComposite d = new MobyDataComposite(
                		MobyDataType.getDataType("Float", mRegistry));
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    } else {
                	MobyDataFloat d = new MobyDataFloat(value,mRegistry);
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    }
                } else if (objectName.equals("Integer")) {
                    
                    try {
                	int val = 0;
                	val = Integer.parseInt(value);
                	MobyDataInt d = new MobyDataInt(val,mRegistry);
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                        d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                        outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    } catch (Exception e) {
                	MobyDataComposite d = new MobyDataComposite(
                		MobyDataType.getDataType("Integer", mRegistry));
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    }
                }else if (objectName.equals("Boolean")) {
                    if (value == null || value.trim().equals("")) {
                	MobyDataComposite d = new MobyDataComposite(MobyDataType.getDataType("Boolean", mRegistry));
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    } else {
                        MobyDataBoolean d = new MobyDataBoolean(value,mRegistry);
                        d.setId(id);
                        MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                        d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                        outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    }
                } else if (objectName.equals("DateTime")) {
                    if (value == null || value.trim().equals("")) {
                	MobyDataComposite d = new MobyDataComposite(MobyDataType.getDataType("DateTime", mRegistry));
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    } else {
                	MobyDataDateTime d = new MobyDataDateTime("", value,mRegistry);
                	d.setId(id);
                	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                        if (mNamespace != null)
                    		d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                    	d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                    	outputMap.put("mobyData", new DataThing(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" )));
                    }
                }
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

                //Element mobyObjectElement = mo.createObject(objectName,namespace, id, article);
                
                MobyDataComposite composite = new MobyDataComposite(MobyDataType.getDataType(objectName, mRegistry));
                composite.setId(id);
        	MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
                if (mNamespace != null)
                    composite.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
                composite.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
                
               // Element mobyObjectElement = XMLUtilities.getDOMDocument(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + composite.toXML() +"</Simple>" )).detachRootElement();
                Element mobyObjectElement = XMLUtilities.getDOMDocument(( composite.toXML() )).detachRootElement();
                
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
                            Element valueElement = (XMLUtilities.getDOMDocument(value)).getRootElement();
                            if (valueElement.getChild("mobyContent",XMLUtilities.MOBY_NS) != null) {
                        	valueElement = valueElement.getChild("mobyContent",XMLUtilities.MOBY_NS);
                            } else {
                        	valueElement = valueElement.getChild("mobyContent");
                            }
                            if (valueElement.getChild("mobyData",XMLUtilities.MOBY_NS) != null ) {
                        	valueElement = valueElement.getChild("mobyData",XMLUtilities.MOBY_NS);
                            } else {
                        	valueElement = valueElement.getChild("mobyData");
                            }
                            if (valueElement.getChild("Simple",XMLUtilities.MOBY_NS) != null ) {
                        	valueElement = valueElement.getChild("Simple",XMLUtilities.MOBY_NS);
                            } else {
                        	valueElement = valueElement.getChild("Simple");
                            }
                            if (valueElement.getChild(type,XMLUtilities.MOBY_NS) != null ) {
                        	valueElement = valueElement.getChild(type,XMLUtilities.MOBY_NS);
                            } else {
                        	valueElement = valueElement.getChild(type);
                            }
                           // Element _child = mo.getObject(mobyObjectElement,type, articleName);
                            
                            valueElement.removeAttribute("articleName");
                            valueElement.removeAttribute("articleName", XMLUtilities.MOBY_NS);
                            valueElement.setAttribute("articleName", articleName, XMLUtilities.MOBY_NS);
//                            Element _newChild = mo.createObject(type, _ns, _id,
//                                    articleName);
//                            _newChild.setText(valueElement
//                                    .getText());
//                            _newChild.addContent(_child.cloneContent());
                            // import and replace the node
                           // mobyObjectElement.removeContent(_child);
                            mobyObjectElement.addContent(valueElement.detach());
                        }
                    }
                }
                Element simple = new Element("Simple", XMLUtilities.MOBY_NS);
		simple.setAttribute("articleName", article, XMLUtilities.MOBY_NS);
		simple.addContent(mobyObjectElement);
		
		org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                String mobyDataString = outputter.outputString(XMLUtilities.createMobyDataElementWrapper(simple));
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
