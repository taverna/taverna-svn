/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
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

    public String getResourceHost() {
	return info.dbHost;
    }

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
	    buildInputPortsFromQuery();
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
		    if ((sd1 == null && sd2 != null) ||
			(sd1 != null && sd2 == null)) {
			// Only update if there's either a removed sequence or a newly placed one
			update();
		    }
		}
		public void filterAdded(Query sourceQuery, int index, Filter filter) {
		    updateInputs();
		}
		public void filterRemoved(Query sourceQuery, int index, Filter filter) {
		    updateInputs();
		}
		public void filterChanged(Query sourceQuery, int index, Filter oldFilter, Filter newFilter) {
		    pingModel();
		}
		private void updateInputs() {
		    try {
			buildInputPortsFromQuery();
		    }
		    catch (Exception ex) {
			ex.printStackTrace();
		    }
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

    public BiomartConfigBean getConfig() {
	return this.info;
    }

    public String getDataSourceName() {
	return this.dataSourceName;
    }
    
    public Query getQuery() {
	return this.query;
    }

    Query getFullyPopulatedQuery() throws ConfigurationException {
	synchronized(query) {
	    if (this.query.getDataSource() != null) {
		return getQuery();
	    }
	    else {
		initQuery();
		return getQuery();
	    }
	}
    }
    
    public DatasetConfig getDatasetConfig() throws ConfigurationException {
	synchronized(query) {
	    if (config != null) {
		return config;
	    }
	    else {
		initQuery();
		return config;
	    }
	}
    }
    
    DatasetConfig config = null;

    void initQuery() throws ConfigurationException {
	synchronized(query) {
	    DetailedDataSource ds = 
		new DetailedDataSource(info.dbType,
				       info.dbHost,
				       info.dbPort,
				       info.dbInstance,
				       info.dbUser,
				       info.dbPassword,
				       10,
				       info.dbDriver);
	    DSConfigAdaptor adaptor = new DatabaseDSConfigAdaptor(ds, ds.getUser(), 
								  true, false, false);
	    DatasetConfigIterator configs = adaptor.getDatasetConfigsByDataset(dataSourceName);
	    config = (DatasetConfig)configs.next();
	    //config = adaptor.getDatasetConfigByDatasetInternalName(dataSourceName,
	    //							   "default");
	    query.setDataSource(ds);
	    query.setDataset(config.getDataset());
	    query.setDatasetConfig(config);
	    query.setMainTables(config.getStarBases());
	    query.setPrimaryKeys(config.getPrimaryKeys());
	}
    }
    
    public Properties getProperties() {
	return new Properties();
    }
    
    void pingModel() {
	fireModelEvent(new MinorScuflModelEvent(this, "Filter attributes changed"));
    }

    private void buildInputPortsFromQuery() 
	throws PortCreationException,
	       DuplicatePortNameException {
	Filter[] filters = query.getFilters();
	Set filterNames = new HashSet();
	for (int i = 0; i < filters.length; i++) {
	    if (filters[i] instanceof BasicFilter) {
		if (filters[i].getValue() != null) {
		    String fieldName = filters[i].getField();
		    filterNames.add(fieldName+"_filter");
		    try {
			locatePort(fieldName+"_filter");
		    }
		    catch (UnknownPortException upe) {
			Port newPort = new InputPort(this, fieldName+"_filter");
			newPort.setSyntacticType("'text/plain'");
			addPort(newPort);
		    }
		}
	    }
	    else if (filters[i] instanceof IDListFilter) {
		String fieldName = filters[i].getField();
		filterNames.add(fieldName+"_filter");
		try {
		    locatePort(fieldName+"_filter");
		}
		catch (UnknownPortException upe) {
		    Port newPort = new InputPort(this, fieldName+"_filter");
		    newPort.setSyntacticType("l('text/plain')");
		    addPort(newPort);
		}
	    }
	}
	InputPort[] currentInputs = getInputPorts();
	for (int i = 0; i < currentInputs.length; i++) {
	    Port inputPort = currentInputs[i];
	    if (filterNames.contains(inputPort.getName()) == false) {
		removePort(inputPort);
	    }
	}
    }

    private void buildPortsFromQuery()
	throws PortCreationException,
	       DuplicatePortNameException {
	Attribute[] attributes = query.getAttributes();
	Set attributeNames = new HashSet();
	// Create new ports corresponding to attributes
	for (int i = 0; i < attributes.length; i++) {
	    if (attributes[i] instanceof FieldAttribute) {
		String fieldName = ((FieldAttribute)attributes[i]).getUniqueName();
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
