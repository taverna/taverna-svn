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
			    String dataSourceName)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, processorName);
	this.info = info;
	this.dataSourceName = dataSourceName;
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

}
