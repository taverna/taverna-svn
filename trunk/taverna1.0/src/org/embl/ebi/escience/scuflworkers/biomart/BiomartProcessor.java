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
	for (int i = 0; i < attributes.length; i++) {
	    String fieldName = attributes[i].getField();
	    Port newPort = new InputPort(this, fieldName);
	    newPort.setSyntacticType("l('text/plain')");
	    addPort(newPort);
	}
    }
    
}
