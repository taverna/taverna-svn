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
	    Query query = processor.getQuery();
	    String dataSourceName = processor.getDataSourceName();
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
	    
	    Engine engine = new Engine();
	    final Map results = new HashMap();
	    OutputPort[] outputs = this.processor.getOutputPorts();
	    final String[] outputNames = new String[outputs.length];
	    for (int i = 0; i < outputs.length; i++) {
		outputNames[i] = outputs[i].getName();
		results.put(outputNames[i], new DataThing(new ArrayList()));
	    }
	    final BiomartProcessor pb = this.processor;
	    OutputStream os = new OutputStream() {
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
			for (int i = 0; i < items.length; i++) {
			    String outputName = outputNames[i];
			    DataThing outputThing = (DataThing)results.get(outputName);
			    ((List)outputThing.getDataObject()).add(items[i]);
			}
		    }
		    
		};
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
