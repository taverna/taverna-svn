/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import java.util.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import java.sql.SQLException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import java.io.*;
import org.embl.ebi.escience.baclava.*;


/**
 * Task to invoke a search over a Biomart data warehouse
 * @author Tom Oinn
 */
public class BiomartTask implements ProcessorTaskWorker {
    
    private BiomartProcessor processor;
    
    public BiomartTask(Processor p) {
	this.processor = (BiomartProcessor)p;
    }

    public Map execute(Map inputMap, ProcessorTask parentTask)
	throws TaskExecutionException {
	try {
	    BiomartConfigBean info = processor.getConfig();
	    // Get a query including data source etc, creating
	    // a copy so that any filter value settings are not
	    // overwritten by input values
	    Query query = new Query(processor.getFullyPopulatedQuery());
	    // Copy across the DataSetConfig object!
	    query.setDatasetConfig(processor.getFullyPopulatedQuery().getDatasetConfig());
	    /**String dataSourceName = processor.getDataSourceName();
	     // Create new DetailedDataSource
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
	     DatasetConfig config = adaptor.getDatasetConfigByDatasetInternalName(dataSourceName,
	     "default");
	     query.setDataSource(ds);
	     // dataset query applies to
	     query.setDataset(config.getDataset());
	     // prefixes for databases we want to use
	     query.setMainTables(config.getStarBases());
	     // primary keys available for sql table joins 
	     query.setPrimaryKeys(config.getPrimaryKeys());
	    */
	    
	    // Configure any filters
	    Filter[] filters = query.getFilters();
	    for (int i = 0; i < filters.length; i++) {
		String filterField = filters[i].getField();
		if (inputMap.containsKey(filterField+"_filter")) {
		    DataThing filterThing = (DataThing)inputMap.get(filterField+"_filter");
		    if (filters[i] instanceof BasicFilter) {
			if (filters[i].getValue() == null) {
			    // Remove the placeholder query
			    query.removeFilter(filters[i]);
			}
			else {
			    String filterValue = (String)filterThing.getDataObject();
			    BasicFilter newFilter = new BasicFilter(filters[i].getField(),
								    filters[i].getTableConstraint(),
								    filters[i].getKey(),
								    filters[i].getQualifier(),
								    filterValue,
								    filters[i].getHandler());
			    query.replaceFilter(filters[i], newFilter);
			}
		    }
		    else if (filters[i] instanceof IDListFilter) {
			List idList = (List)filterThing.getDataObject();
			String[] idArray = (String[])idList.toArray(new String[0]);
			IDListFilter newFilter = new IDListFilter(filters[i].getField(),
								  filters[i].getTableConstraint(),
								  filters[i].getKey(),
								  idArray,
								  filters[i].getHandler());
			query.replaceFilter(filters[i], newFilter);
		    }
		}
	    }

	    Engine engine = new Engine();
	    final Map results = new HashMap();
	    OutputPort[] outputs = this.processor.getOutputPorts();
	    final String[] outputNames = new String[outputs.length];
	    for (int i = 0; i < outputs.length; i++) {
		outputNames[i] = outputs[i].getName();
		results.put(outputNames[i], new DataThing(new ArrayList()));
	    }
	    final BiomartProcessor pb = this.processor;
	    OutputStream os = null;
	    if (query.getSequenceDescription() == null) {
		// No query so can use sensible processing
		os = new OutputStream() {
			ByteArrayOutputStream currentLine = new ByteArrayOutputStream();
			public void close() {
			    doString(currentLine.toString());
			}
			public void flush() {
			    //
			}
			public void write(int b) throws IOException {
			    if (b == (int)'\n') {
				doString(currentLine.toString());
				currentLine.reset();
			    }
			    else {
				currentLine.write(b);
			    }
			}
			private void doString(String theString) {
			    String[] items = theString.split("\t",-1);
			    for (int i = 0; i < items.length && i < outputNames.length; i++) {
				String outputName = outputNames[i];
				DataThing outputThing = (DataThing)results.get(outputName);
				((List)outputThing.getDataObject()).add(items[i]);
			    }
			}
			
		    };
	    }
	    else {
		// Has a query so biomart is going to ignore everything we asked for and
		// respond with some half assed keyed format. Mutter.
		os = new OutputStream() {
			ByteArrayOutputStream currentLine = new ByteArrayOutputStream();
			public void close() {
			    doString(currentLine.toString());
			}
			public void flush() {
			    //
			}
			public void write(int b) throws IOException {
			    if (b == (int)'\n') {
				doString(currentLine.toString());
				currentLine.reset();
			    }
			    else {
				currentLine.write(b);
			    }
			}
			private void doString(String theString) {
			    String[] items = theString.split("\t",-1);
			    for (int i = 0; i < outputNames.length; i++) {
				String outputName = outputNames[i];
				String result = "";
				if (outputName.equals("sequenceexport")) {
				    // sequence always the last item
				    result = items[items.length-1];
				}
				else {
				    // Go from item number length-(1+outputNames.length)
				    // up to length-2
				    for (int j = items.length - (1 + outputNames.length);
					 j < items.length - 2;
					 j++) {
					String[] parts = items[j].split("=");
					if (parts.length == 2) {
					    if (parts[0].equals(outputName)) {
						result = parts[1];
					    }
					}
				    }
				}
				// Iterate over all the parts...
				DataThing outputThing = (DataThing)results.get(outputName);
				((List)outputThing.getDataObject()).add(result);
			    }
			}
		    };
	    }
	    engine.execute(query,
			   new FormatSpec(FormatSpec.TABULATED, "\t"),
			   os);
	    return results;
	}
	catch (Exception ex) {
	    TaskExecutionException tee = new TaskExecutionException("Failure calling biomart");
	    tee.initCause(ex);
	    throw tee;
	}
    }
}
