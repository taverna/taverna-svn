/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scuflworkers.*;

/**
 * A processor factory for the Biomart query processor
 * @author Tom Oinn
 */
public class BiomartProcessorFactory extends ProcessorFactory {

    private BiomartConfigBean info;
    private String dataSourceName;

    public BiomartProcessorFactory(BiomartConfigBean info,
				   String dataSourceName) {
	setName(dataSourceName);
	this.info = info;
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

    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor.class;
    }

}
