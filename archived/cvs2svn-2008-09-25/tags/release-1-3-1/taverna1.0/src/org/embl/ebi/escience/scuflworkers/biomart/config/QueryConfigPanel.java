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
import org.ensembl.mart.explorer.*;
import javax.swing.*;
import java.util.*;
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
	    DatasetConfig config = theProcessor.getDatasetConfig();
	    JTabbedPane attributes = new JTabbedPane();
	    AttributePage[] atPages = config.getAttributePages();
	    boolean foundValidPage = false;
	    System.out.println("Found pages, adding valid ones");
	    for (int i = 0; i < atPages.length; i++) {
		if (skipAttributePage(atPages[i]) == false) {
		    AttributePageEditor ape = new AttributePageEditor(query, atPages[i], theProcessor);
		    attributes.add(atPages[i].getDisplayName(),ape);
		    if (ape.lastValid && !foundValidPage) {
			attributes.setSelectedComponent(ape);
			foundValidPage = true;
		    }
		}
	    }
	    System.out.println("Done");
	    JTabbedPane filters = new JTabbedPane();
	    FilterPage[] fPages = config.getFilterPages();
	    System.out.println("Found filter pages");
	    for (int i = 0; i < fPages.length; i++) {
		if (fPages[i].getInternalName().equals("link_filters")) continue;
		if (fPages[i].getHidden() != null && fPages[i].getHidden().equals("true")) continue;
		if (fPages[i].getAttribute("hideDisplay") != null && fPages[i].getAttribute("hideDisplay").equals("true")) continue;
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
    
    boolean skipAttributePage(AttributePage page) {
	if (page.getHidden() != null && page.getHidden().equals("true")) return true;
	if (page.getAttribute("hideDisplay") != null && page.getAttribute("hideDisplay").equals("true")) return true;
	if (page.getInternalName().equals("structure")) return true;
	boolean skip = false;
	AdaptorManager manager = theProcessor.manager;
	//we only support sequences with pointer attributes
	if (!skip) {
	    if (page.containsOnlyPointerAttributes()) {
		AttributeGroup seqGroup = (AttributeGroup) page.getAttributeGroupByName("sequence");
		
		//skip if this does not contain a sequence group (non ensembl)
		if (seqGroup == null)
		    skip = true;
		else {
		    AttributeCollection seqCol = null;
		    
		    AttributeCollection[] cols = seqGroup.getAttributeCollections();
		    for (int i = 0, n = cols.length; i < n; i++) {
			AttributeCollection collection = cols[i];
			if (collection.getInternalName().matches("\\w*seq_scope\\w*")) {
			    seqCol = collection;
			    break;
			}
		    }
		    
		    //skip if the sequence group does not contain a page called "seq_scope_type" (non ensembl)
		    if (seqCol == null)
			skip = true;
		    
		    if (!skip) {
			//test for presence of sequence dataset
			AttributeDescription seqDesc = (AttributeDescription) seqCol.getAttributeDescriptions().get(0);
			String seqDataset = seqDesc.getInternalName().split("\\.")[0];
			//if (manager.getRootAdaptor().getNumDatasetConfigsByDataset(seqDataset) < 1) {
			//    skip = true;
			//}
		    }
		}
		
		if (!skip) {
		    //test for ambiguous links
		    AttributeGroup nonSeqGroup = null;
		    List groups = page.getAttributeGroups();
		    for (int i = 0, n = groups.size(); i < n; i++) {
			AttributeGroup element = (AttributeGroup) groups.get(i);
			if (!element.getInternalName().equals("sequence")) {
			    nonSeqGroup = element;
			    break;
			}
		    }
		    
		    //get the first attribute, and test its dataset to see if it is duplicated
		    AttributeDescription firstAtt = (AttributeDescription) nonSeqGroup.getAttributeCollections()[0].getAttributeDescriptions().get(0);
		    String dataset = firstAtt.getInternalName().split("\\.")[0];
		    
		    //if (manager.getRootAdaptor().getNumDatasetConfigsByDataset(dataset) > 1) {
		    //skip = true;
		    //}   
		}
	    }
	}
	return skip;

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
