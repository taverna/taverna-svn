/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.ensembl.mart.lib.*;
import java.util.*;

/**
 * A processor representing an arbitrary query over a biomart
 * data store
 * @author Tom Oinn
 */
public class BiomartProcessor extends Processor {

    private BiomartConfigBean info;
    private String dataSourceName;
    private Query query = null;
    private QueryListener queryListener;

    public BiomartProcessor(ScuflModel model,
			    String processorName,
			    BiomartConfigBean info, 
			    String dataSourceName,
			    Query query)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, processorName);
	this.info = info;
	this.dataSourceName = dataSourceName;
	this.query = query;
	try {
	    buildPortsFromQuery();
	}
	catch (Exception ex) {
	    ProcessorCreationException pce = new ProcessorCreationException("Can't build output ports");
	    pce.initCause(ex);
	    throw pce;
	}
	// Register a query change listener to trap changes to the query object and
	// fire them off as minor model events
	queryListener = new QueryAdaptor() {
		public void attributeAdded(Query sourceQuery, int index, Attribute attribute) {
		    update();
		}
		public void attributeRemoved(Query sourceQuery, int index, Attribute attribute) {
		    update();
		}
		public void sequenceDescriptionChanged(Query sourceQuery, 
						       SequenceDescription sd1, 
						       SequenceDescription sd2) {
		    update();
		}
		private void update() {
		    try {
			buildPortsFromQuery();
		    }
		    catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    };
	query.addQueryChangeListener(queryListener);
	
    }
    
    protected void finalize() throws Throwable {
	query.removeQueryChangeListener(queryListener);
    }

    BiomartConfigBean getConfig() {
	return this.info;
    }

    String getDataSourceName() {
	return this.dataSourceName;
    }
    
    Query getQuery() {
	return this.query;
    }

    public Properties getProperties() {
	return new Properties();
    }
    
    private void buildPortsFromQuery()
	throws PortCreationException,
	       DuplicatePortNameException {
	Attribute[] attributes = query.getAttributes();
	Set attributeNames = new HashSet();
	// Create new ports corresponding to attributes
	for (int i = 0; i < attributes.length; i++) {
	    String fieldName = attributes[i].getField();
	    attributeNames.add(fieldName);
	    try {
		locatePort(fieldName);
	    }
	    catch (UnknownPortException upe) {
		Port newPort = new OutputPort(this, fieldName);
		newPort.setSyntacticType("l('text/plain')");
		addPort(newPort);
	    }
	}
	// Create new port for the sequence if defined
	String sequencePortName = "sequenceexport";
	if (query.getSequenceDescription() != null) {
	    attributeNames.add(sequencePortName);
	    // Try to find the sequence port
	    OutputPort sequencePort = null;
	    try {
		sequencePort = (OutputPort)locatePort(sequencePortName);
		// Exists, move it to the end of the output port list
		if (ports.indexOf(sequencePort) < ports.size()-1) {
		    ports.remove(sequencePort);
		    ports.add(sequencePort);
		    fireModelEvent(new ScuflModelEvent(this, "Shifted sequence output to end of port list"));
		}
	    }
	    catch (UnknownPortException upe) {
		Port newPort = new OutputPort(this, sequencePortName);
		newPort.setSyntacticType("l('text/plain')");
		addPort(newPort);
	    }
	}

	// Remove any ports which don't have attributes with corresponding field names
	OutputPort[] ports = getOutputPorts();
	for (int i = 0; i < ports.length; i++) {
	    Port outputPort = ports[i];
	    if (attributeNames.contains(outputPort.getName()) == false) {
		removePort(outputPort);
	    }
	}
    }
    
}
