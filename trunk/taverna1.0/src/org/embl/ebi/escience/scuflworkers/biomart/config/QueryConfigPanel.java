/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflworkers.biomart.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import javax.swing.*;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;


/**
 * JPanel subclass manifesting an editor over a BiomartProcessor
 * Query object.
 * @author Tom Oinn
 */
public class QueryConfigPanel extends JPanel
    implements ScuflUIComponent {

    private BiomartProcessor theProcessor;

    public QueryConfigPanel(BiomartProcessor bp) {
	super(new BorderLayout());
	this.theProcessor = bp;
	BiomartConfigBean info = theProcessor.getConfig();
	String dataSourceName = theProcessor.getDataSourceName();
	Query query = theProcessor.getQuery();
	try {
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
	    JTabbedPane attributes = new JTabbedPane();
	    AttributePage[] atPages = config.getAttributePages();
	    for (int i = 0; i < atPages.length; i++) {
		attributes.add(atPages[i].getDisplayName(),new AttributePageEditor(query,atPages[i]));
	    }
	    add(attributes);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    public void attachToModel(ScuflModel theModel) {
	//
    }

    public void detachFromModel() {
	//
    }
    
    public String getName() {
	return "Configuring query for " + theProcessor.getName();
    }

    public ImageIcon getIcon() {
	return ProcessorHelper.getPreferredIcon(theProcessor);
    }

}
