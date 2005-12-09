/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scuflworkers.*;
import org.ensembl.mart.lib.Query;

/**
 * A processor factory for the Biomart query processor
 * @author Tom Oinn
 */
public class BiomartProcessorFactory extends ProcessorFactory {

    private BiomartConfigBean info;
    private String dataSourceName;
    private Query query;

    public BiomartProcessorFactory(BiomartConfigBean info,
				   String dataSourceName) {
	this(info, dataSourceName, null);
    }

    public BiomartProcessorFactory(BiomartConfigBean info,
				   String dataSourceName,
				   Query query) {
	setName(dataSourceName);
	this.info = info;
	this.query = query;
	this.dataSourceName = dataSourceName;
    }
    
    public String getProcessorDescription() {
	return "Generic query over a Biomart data store";
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

    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor.class;
    }

}
