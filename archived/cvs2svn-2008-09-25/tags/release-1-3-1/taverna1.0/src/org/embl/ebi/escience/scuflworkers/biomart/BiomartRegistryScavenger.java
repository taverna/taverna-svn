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
import java.net.*;
import java.util.*;

/**
 * Scavenges from a biomart registry
 * @author Tom Oinn
 */
public class BiomartRegistryScavenger extends Scavenger {
    
    public BiomartRegistryScavenger(String registryURL)
	throws ScavengerCreationException {
	super("Biomart registry @ "+registryURL);
	URL registryLocation;
	try {
	    registryLocation = new URL(registryURL);
	}
	catch (MalformedURLException mue) {
	    throw new ScavengerCreationException("Not a valid URL : "+mue.getMessage());
	}
	try {
	    RegistryDSConfigAdaptor ra = new RegistryDSConfigAdaptor(registryLocation, false, false, true);
	    DSConfigAdaptor[] as = ra.getLeafAdaptors();
	    for (int i = 0; i < as.length; i++) {
		// One adaptor per database, pretty much
		DefaultMutableTreeNode adaptorNode = new DefaultMutableTreeNode(as[i].getName());
		add(adaptorNode);
		String[] datasetNames = as[i].getDatasetNames(false);
		Arrays.sort(datasetNames);
		DetailedDataSource dds = as[i].getDataSource();
		BiomartConfigBean info = new BiomartConfigBean(dds);
		info.setRegistryURL(registryLocation.toString());
		for (int j = 0; j < datasetNames.length; j++) {
		    String dataset = datasetNames[j];
		    DatasetConfigIterator configs = as[i].getDatasetConfigsByDataset(dataset);
		    while (configs.hasNext()) {
			DatasetConfig config = (DatasetConfig)configs.next();
			if (config.getInternalName().toLowerCase().equals("default")) {
			    String dataSetName = config.getDataset();
			    BiomartProcessorFactory bpf = new BiomartProcessorFactory(info, dataSetName);
			    adaptorNode.add(new DefaultMutableTreeNode(bpf));
			}
		    }
		}
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
