/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

// Utility Imports
import java.util.Iterator;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import java.lang.String;



/**
 * Represents a ScuflModel instance as an XScufl
 * document.
 * @author Tom Oinn
 */
public class XScuflView implements ScuflModelEventListener, java.io.Serializable {
    
    private ScuflModel model = null;
    private boolean cacheValid = false;
    private String cachedRepresentation = null;
    private Document cachedDocument = null;

    /**
     * Construct the view and bind to the given model.
     */
    public XScuflView(ScuflModel model) {
	// Reference to the model that this is a view on
	this.model = model;
	// Cached copy doesn't exist so set validity to false
	this.cacheValid = false;
	// Be informed of events corresponding to changes in the model
	this.model.addListener(this);
    }

    /**
     * Get the XML Document from this view
     */
    public Document getDocument() {
	synchronized(this) {
	    if (!cacheValid) {
		updateCachedView();
	    }
	    return this.cachedDocument;
	}
    }

    /**
     * Get the XML String from this view
     */
    public String getXMLText() {
	synchronized (this) {
	    if (!cacheValid) {
		updateCachedView();
	    }
	    return this.cachedRepresentation;
	}
    }

    /**
     * Handle model events
     */
    public void receiveModelEvent(ScuflModelEvent event) {
	// Invalidate cache, this will
	// force a recalculation next time
	// the view is queried.
	synchronized(this) {
	    this.cacheValid = false;
	}
    }

    /**
     * Update or create the cached view, this
     * consists of building the XML document
     * from the model and creating a textual
     * version of it.
     */
    private void updateCachedView() {
	// Create the XML document
	Element root = new Element("scufl",scuflNS());
	root.setAttribute("version","0.1");
	root.setAttribute("log",""+model.getLogLevel());
	this.cachedDocument = new Document(root);
	
	// Create elements corresponding to processors
	Processor[] processors = model.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    Element processor = new Element("processor",scuflNS());
	    processor.setAttribute("name",processors[i].getName());
	    // Only set the log level if it is zero or higher, negative values
	    // implicitly mean 'inherit from model'
	    if (processors[i].getRealLogLevel() > -1) {
		processor.setAttribute("log",""+processors[i].getLogLevel());
	    }
	    // Set the description if it isn't the empty string
	    String description = processors[i].getDescription();
	    if (description.equals("")==false) {
		Element de = new Element("description",scuflNS());
		de.setText(description);
		processor.addContent(de);
	    }
	    Element spec = ProcessorHelper.elementForProcessor(processors[i]);
	    processor.addContent(spec);
	    // Do the templates
	    AnnotationTemplate[] templates = processors[i].getAnnotationTemplates();
	    for (int j = 0; j < templates.length; j++) {
		processor.addContent(templates[j].getElement());
	    }
	    // Do the alternates
	    AlternateProcessor[] ap = processors[i].getAlternatesArray();
	    for (int j = 0; j < ap.length; j++) {
		Element alternateElement = new Element("alternate",XScufl.XScuflNS);
		Processor alternateProcessor = ap[j].getProcessor();
		// Populate the processor spec part of the alternate
		alternateElement.addContent(ProcessorHelper.elementForProcessor(alternateProcessor));
		// Populate the output mapping part
		for (Iterator ii = ap[j].getOutputMapping().keySet().iterator(); ii.hasNext();) {
		    String key = (String)ii.next();
		    String value = (String)ap[j].getOutputMapping().get(key);
		    Element mappingElement = new Element("outputmap",XScufl.XScuflNS);
		    mappingElement.setAttribute("key",key);
		    mappingElement.setAttribute("value",value);
		    alternateElement.addContent(mappingElement);
		}
		// .. and the input mapping
		for (Iterator ii = ap[j].getInputMapping().keySet().iterator(); ii.hasNext();) {
		    String key = (String)ii.next();
		    String value = (String)ap[j].getInputMapping().get(key);
		    Element mappingElement = new Element("inputmap",XScufl.XScuflNS);
		    mappingElement.setAttribute("key",key);
		    mappingElement.setAttribute("value",value);
		    alternateElement.addContent(mappingElement);
		}
		processor.addContent(alternateElement);
	    }
	    root.addContent(processor);
	}
	

	// Create elements corresponding to data constraints
	DataConstraint[] dataconstraints = model.getDataConstraints();
	for (int i = 0; i < dataconstraints.length; i++) {

	    DataConstraint dc = dataconstraints[i];
	    String sourceProcessorName = dc.getSource().getProcessor().getName();
	    String sourcePortName = dc.getSource().getName();
	    String sinkProcessorName = dc.getSink().getProcessor().getName();
	    String sinkPortName = dc.getSink().getName();

	    Element link = new Element("link",scuflNS());
	    Element inputNode = new Element("input",scuflNS());
	    if (dc.getSink().getProcessor() != model.getWorkflowSinkProcessor()) {
		inputNode.setText(sinkProcessorName+":"+sinkPortName);
	    }
	    else {
		inputNode.setText(sinkPortName);
	    }
	    
	    Element outputNode = new Element("output",scuflNS());
	    if (dc.getSource().getProcessor() != model.getWorkflowSourceProcessor()) {
		outputNode.setText(sourceProcessorName+":"+sourcePortName);
	    }
	    else {
		outputNode.setText(sourcePortName);
	    }
	    link.addContent(inputNode);
	    link.addContent(outputNode);
	    root.addContent(link);
	}
	
	// Create elements for external ports
	Port[] sources = model.getWorkflowSourceProcessor().getPorts();
	for (int i = 0; i<sources.length; i++) {
	    Element sourceElement = new Element("source",scuflNS());
	    sourceElement.setText(sources[i].getName());
	    sourceElement.addContent(sources[i].getMetadata().getConfigurationElement());
	    root.addContent(sourceElement);
	}
	Port[] sinks = model.getWorkflowSinkProcessor().getPorts();
	for (int i = 0; i < sinks.length; i++) {
	    Element sinkElement = new Element("sink",scuflNS());
	    sinkElement.setText(sinks[i].getName());
	    sinkElement.addContent(sinks[i].getMetadata().getConfigurationElement());
	    root.addContent(sinkElement);
	}


	// Create elements corresponding to external port definitions
	// DEPRECATED
	/**
	Port[] externalPorts = model.getExternalPorts();
	for (int i = 0; i < externalPorts.length; i++) {
	Element external = new Element("external",scuflNS());
	external.setText(externalPorts[i].getProcessor().getName()+":"+externalPorts[i].getName());
	root.addContent(external);
	}
	*/

	// Create elements corresponding to concurrency constraints
	ConcurrencyConstraint[] constraints = model.getConcurrencyConstraints();
	for (int i = 0; i < constraints.length; i++) {
	    Element coordination = new Element("coordination", scuflNS());
	    coordination.setAttribute("name",constraints[i].getName());
	    root.addContent(coordination);
	    Element condition = new Element("condition", scuflNS());
	    Element action = new Element("action", scuflNS());
	    coordination.addContent(condition);
	    coordination.addContent(action);

	    // Define the condition
	    // <condition>
	    //   <target>ControllingProcessor</target>
	    //   <state>COMPLETED</state>
	    // </condition>
	    Element state = new Element("state", scuflNS());
	    state.setText(ConcurrencyConstraint.statusCodeToString(constraints[i].getControllerStateGuard()));
	    condition.addContent(state);
	    Element ctarget = new Element("target", scuflNS());
	    ctarget.setText(constraints[i].getControllingProcessor().getName());
	    condition.addContent(ctarget);
	    
	    // Define the action
	    // <action>
	    //   <target>targetProcessor</target>
	    //   <statechange>
	    //     <from>SCHEDULED</from>
	    //     <to>RUNNING</to>
	    //   </statechange>
	    // </action>
	    Element target = new Element("target", scuflNS());
	    target.setText(constraints[i].getTargetProcessor().getName());
	    action.addContent(target);
	    Element statechange = new Element("statechange", scuflNS());
	    action.addContent(statechange);
	    Element from = new Element("from", scuflNS());
	    Element to = new Element("to", scuflNS());
	    from.setText(ConcurrencyConstraint.statusCodeToString(constraints[i].getTargetStateFrom()));
	    to.setText(ConcurrencyConstraint.statusCodeToString(constraints[i].getTargetStateTo()));
	    statechange.addContent(from);
	    statechange.addContent(to);
	}

	// Generate the textual version and cache it.
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	this.cachedRepresentation = xo.outputString(this.cachedDocument);

	// Cache is now valid.
	this.cacheValid = true;
    }

    /**
     * The namespace for the generated nodes, 
     * references the scufl.XScufl class
     */
    private Namespace scuflNS() {
	return XScufl.XScuflNS;
    }

}
