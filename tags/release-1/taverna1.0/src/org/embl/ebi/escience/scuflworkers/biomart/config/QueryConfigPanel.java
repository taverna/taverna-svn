/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart.config;

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
	JTabbedPane tabs = new JTabbedPane();
	this.theProcessor = bp;
	BiomartConfigBean info = theProcessor.getConfig();
	String dataSourceName = theProcessor.getDataSourceName();
	Query query = theProcessor.getQuery();
	try {
	    /**
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
	    */
	    DatasetConfig config = theProcessor.getDatasetConfig();
	    JTabbedPane attributes = new JTabbedPane();
	    AttributePage[] atPages = config.getAttributePages();
	    boolean foundValidPage = false;
	    for (int i = 0; i < atPages.length; i++) {
		AttributePageEditor ape = new AttributePageEditor(query, atPages[i]);
		attributes.add(atPages[i].getDisplayName(),ape);
		if (ape.lastValid && !foundValidPage) {
		    attributes.setSelectedComponent(ape);
		    foundValidPage = true;
		}
	    }
	    JTabbedPane filters = new JTabbedPane();
	    FilterPage[] fPages = config.getFilterPages();
	    for (int i = 0; i < fPages.length; i++) {
		filters.add(fPages[i].getDisplayName(), new FilterPageEditor(query, fPages[i]));
	    }
	    tabs.add("Attributes",attributes);
	    tabs.add("Filters",filters);
	    add(tabs);
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
