/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import java.lang.ClassCastException;
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
	if (!cacheValid) {
	    updateCachedView();
	}
	return this.cachedDocument;
    }

    /**
     * Get the XML String from this view
     */
    public String getXMLText() {
	if (!cacheValid) {
	    updateCachedView();
	}
	return this.cachedRepresentation;
    }

    /**
     * Handle model events
     */
    public void receiveModelEvent(ScuflModelEvent event) {
	// Invalidate cache, this will
	// force a recalculation next time
	// the view is queried.
	this.cacheValid = false;
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
	this.cachedDocument = new Document(root);
	
	// Create elements corresponding to processors
	Processor[] processors = model.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    Element processor = new Element("processor",scuflNS());
	    processor.setAttribute("name",processors[i].getName());
	    // Catch Soaplab processors - this should be more
	    // extensible! Will do for now however...
	    try {
		SoaplabProcessor slp = (SoaplabProcessor)processors[i];
		// No exception therefore we have a soaplab processor
		Element spec = new Element("soaplabwsdl",scuflNS());
		spec.setText(slp.getEndpoint().toString());
		processor.addContent(spec);
		root.addContent(processor);
	    }
	    catch (ClassCastException cce) {
		//
	    }
	    // Catch WSDLBasedProcessor
	    try {
		WSDLBasedProcessor wsdlp = (WSDLBasedProcessor)processors[i];
		Element spec = new Element("arbitrarywsdl",scuflNS());
		Element wsdl = new Element("wsdl",scuflNS());
		Element port = new Element("porttype",scuflNS());
		Element operation = new Element("operation",scuflNS());
		wsdl.setText(wsdlp.getWSDLLocation());
		port.setText(wsdlp.getPortTypeName());
		operation.setText(wsdlp.getOperationName());
		spec.addContent(wsdl);
		spec.addContent(port);
		spec.addContent(operation);
		processor.addContent(spec);
		root.addContent(processor);
	    }
	    catch (ClassCastException cce) {
		//
	    }
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
	    inputNode.setText(sinkProcessorName+":"+sinkPortName);
	    Element outputNode = new Element("output",scuflNS());
	    outputNode.setText(sourceProcessorName+":"+sourcePortName);

	    link.addContent(inputNode);
	    link.addContent(outputNode);
	    root.addContent(link);
	}
	
	// Create elements corresponding to external port definitions
	Port[] externalPorts = model.getExternalPorts();
	for (int i = 0; i < externalPorts.length; i++) {
	    Element external = new Element("external",scuflNS());
	    external.setText(externalPorts[i].getProcessor().getName()+":"+externalPorts[i].getName());
	    root.addContent(external);
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
