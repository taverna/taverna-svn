/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import java.sql.SQLException;

import org.ensembl.mart.lib.BasicFilter;
import org.ensembl.mart.lib.BooleanFilter;
import org.ensembl.mart.lib.DetailedDataSource;
import org.ensembl.mart.lib.Engine;
import org.ensembl.mart.lib.FieldAttribute;
import org.ensembl.mart.lib.FormatException;
import org.ensembl.mart.lib.FormatSpec;
import org.ensembl.mart.lib.InvalidQueryException;
import org.ensembl.mart.lib.LoggingUtils;
import org.ensembl.mart.lib.Query;
import org.ensembl.mart.lib.SequenceException;
import org.ensembl.mart.lib.config.AttributeDescription;
import org.ensembl.mart.lib.config.ConfigurationException;
import org.ensembl.mart.lib.config.DSConfigAdaptor;
import org.ensembl.mart.lib.config.DatabaseDSConfigAdaptor;
import org.ensembl.mart.lib.config.DatasetConfig;
import org.ensembl.mart.lib.config.FilterDescription;

import org.jdom.Element;
import org.jdom.output.*;

/**
 * Test the Query serializer code
 * @author Tom Oinn
 */
public class TestQuerySerializer {

    public static void main(String[] args) throws Exception {
	
	Query query = new Query();
	DetailedDataSource ds =
	    new DetailedDataSource("mysql",
				   "martdb.ebi.ac.uk",
				   "3306",
				   "ensembl_mart_22_1",
				   "jdbc:mysql://martdb.ebi.ac.uk:3306/ensembl_mart_22_1",
				   "anonymous",
				   null,
				   10,
				   "com.mysql.jdbc.Driver");
	DSConfigAdaptor adaptor = new DatabaseDSConfigAdaptor(ds, 
							      ds.getUser(), 
							      true, 
							      false, 
							      false);
	DatasetConfig config = adaptor.getDatasetConfigByDatasetInternalName("hsapiens_gene_ensembl", "default");
	query.setDataSource(ds);

	// dataset query applies to
	query.setDataset(config.getDataset());

	// prefixes for databases we want to use
	query.setMainTables(config.getStarBases());

	// primary keys available for sql table joins 
	query.setPrimaryKeys(config.getPrimaryKeys());

	// Attributes to return
	AttributeDescription adesc = config.getAttributeDescriptionByInternalName("gene_stable_id");
    
	query.addAttribute(new FieldAttribute(adesc.getField(), adesc.getTableConstraint(), adesc.getKey()));

	adesc = config.getAttributeDescriptionByInternalName("chr_name");
	query.addAttribute(new FieldAttribute(adesc.getField(), adesc.getTableConstraint(), adesc.getKey()));
    
	adesc = config.getAttributeDescriptionByInternalName("mouse_ensembl_id");
	query.addAttribute(new FieldAttribute(adesc.getField(), adesc.getTableConstraint(), adesc.getKey()));
        
	adesc = config.getAttributeDescriptionByInternalName("mouse_dn_ds");
	query.addAttribute(new FieldAttribute(adesc.getField(), adesc.getTableConstraint(), adesc.getKey()));
    
	String name = "chr_name";    
	FilterDescription fdesc = config.getFilterDescriptionByInternalName(name);
    
	//note, the config system actually masks alot of complexity with regard to filters by requiring the internalName
	//again when calling the getXXX methods
	query.addFilter(new BasicFilter(fdesc.getField(name), fdesc.getTableConstraint(name), fdesc.getKey(name), "=", "22"));

	name = "mmusculus_homolog";
	fdesc = config.getFilterDescriptionByInternalName(name);
    
	//note there are different types of BooleanFilter
	if (fdesc.getType(name).equals("boolean"))
	    query.addFilter(new BooleanFilter(fdesc.getField(name), fdesc.getTableConstraint(name), fdesc.getKey(name), BooleanFilter.isNotNULL, null));
	else
	    query.addFilter(new BooleanFilter(fdesc.getField(name), fdesc.getTableConstraint(name), fdesc.getKey(name), BooleanFilter.isNotNULL_NUM, null));
    
	// Serialize the Query object to XML
	BiomartXMLHandler bxh = new BiomartXMLHandler();
	Element e = bxh.queryToElement(query);
	XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	System.out.println(xo.outputString(e));
    }

}
