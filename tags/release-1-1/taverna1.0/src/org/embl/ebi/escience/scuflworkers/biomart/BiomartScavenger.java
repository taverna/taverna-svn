/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import javax.swing.tree.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import org.embl.ebi.escience.scuflworkers.*;

import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import java.sql.SQLException;

/**
 * A scavenger which knows how to extract a list of
 * datasources from a given Biomart instance
 * @author Tom Oinn
 */
public class BiomartScavenger extends Scavenger {
    
    public BiomartScavenger(BiomartConfigBean info) 
	throws ScavengerCreationException {
	super("Biomart "+info.dbInstance+"@"+info.dbHost);
	try {
	    DetailedDataSource ds = new DetailedDataSource(info.dbType,
							   info.dbHost,
							   info.dbPort,
							   info.dbInstance,
							   info.dbUser,
							   info.dbPassword,
							   1,
							   info.dbDriver);
	    DSConfigAdaptor adaptor = new DatabaseDSConfigAdaptor(ds, ds.getUser(), true, false, false);
	    String[] dataSetNames = adaptor.getDatasetNames();
	    for (int i = 0; i < dataSetNames.length; i++) {
		BiomartProcessorFactory bpf = new BiomartProcessorFactory(info, dataSetNames[i]);
		//DatasetConfig dc = adaptor.getDatasetConfigByDatasetInternalName(dataSetNames[i], "default");
		//bpf.setDescription(dc.getDescription());
		add(new DefaultMutableTreeNode(bpf));
	    }
	}
	catch (Exception ex) {
	    ScavengerCreationException sce = new ScavengerCreationException("Cannot create Biomart scavenger!\n"+
									    ex.getMessage());
	    sce.initCause(ex);
	    throw sce;
	}
    }
    
}
