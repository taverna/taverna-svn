/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.jdom.Element;
import org.jdom.Namespace;
import java.util.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.ensembl.mart.lib.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;

/**
 * Handler to serialize the Biomart Query, Processor and ProcessorFactory
 * classes to and from XML
 * @author Tom Oinn
 */
public class BiomartXMLHandler implements XMLHandler {
    
    public static Namespace NAMESPACE = Namespace.getNamespace("biomart","http://org.embl.ebi.escience/xscufl-biomart/0.1alpha");

    /**
     * Get the spec element for the specified BiomartProcessor.
     * @param p The Processor object to serialize to an Element
     */
    public Element elementForProcessor(Processor p) {
	BiomartProcessor bp = (BiomartProcessor)p;
	return getElement(bp.getConfig(), bp.getDataSourceName(), bp.getQuery()); 
    }
    
    /**
     * Get the spec element for the specified BiomartProcessorFactory.
     * @param pf The ProcessorFactory object to serialize to an Element
     */
    public Element elementForFactory(ProcessorFactory pf) {
	BiomartProcessorFactory bpf = (BiomartProcessorFactory)pf;
	return getElement(bpf.getConfig(), bpf.getDataSourceName(), bpf.getQuery());
    }

    /**
     * Build a new BiomartProcessorFactory from the specified element
     */
    public ProcessorFactory getFactory(Element specElement) {
	Element martConfig = specElement.getChild("biomartconfig", NAMESPACE);
	Element dsNameElement = specElement.getChild("biomartds", NAMESPACE);
	Element queryElement = specElement.getChild("query", NAMESPACE);
	Query query = null;
	if (queryElement != null) {
	    query = elementToQuery(queryElement);
	}
	// Ignores Query for now
	return new BiomartProcessorFactory(getConfigBeanFromElement(martConfig),
					   dsNameElement.getTextTrim(),
					   query);
    }
    
    /**
     * Build a new BiomartProcessor from the specified element
     */
    public Processor loadProcessorFromXML(Element processorNode,
					  ScuflModel model,
					  String processorName) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException,
	       XScuflFormatException {
	Element biomart = processorNode.getChild("biomart", XScufl.XScuflNS);
	Element martConfig = biomart.getChild("biomartconfig", NAMESPACE);
	Element dsNameElement = biomart.getChild("biomartds", NAMESPACE);
	Element queryElement = biomart.getChild("query", NAMESPACE);
	Query q = null;
	if (queryElement != null) {
	    q = elementToQuery(queryElement);
	}
	else {
	    // Create a blank query object
	    q = new Query();
	}
	return new BiomartProcessor(model,
				    processorName,
				    getConfigBeanFromElement(martConfig),
				    dsNameElement.getTextTrim(),
				    q);
    }

    /**
     * Get an element corresponding to the specified config, datasource
     * name and Query object. If the query is null then don't create
     * a query child element.
     * @param martConfig A BiomartConfigBean specifying the mart configuration
     * @param dataSourceName The name of the datasource within the specified mart
     * @param query An optional Query object, may be null if not applicable (i.e
     * for a default mart factory)
     */
    private Element getElement(BiomartConfigBean martConfig, String dataSourceName, Query query) {
	Element spec = new Element("biomart", XScufl.XScuflNS);
	spec.addContent(getConfigElement(martConfig));
	Element dsElement = new Element("biomartds", NAMESPACE);
	dsElement.setText(dataSourceName);
	spec.addContent(dsElement);	
	if (query != null) {
	    spec.addContent(queryToElement(query));
	}
	return spec;
    }

    /**
     * Pull back an Element corresponding to the specified config
     * @param info the BiomartConfigBean to serialize to an Element
     */
    private Element getConfigElement(BiomartConfigBean info) {
	Element martConfig = new Element("biomartconfig", NAMESPACE);
	martConfig.setAttribute("dbtype", info.dbType);
	martConfig.setAttribute("dbdriver", info.dbDriver);
	martConfig.setAttribute("dbhost", info.dbHost);
	martConfig.setAttribute("dbport", info.dbPort);
	martConfig.setAttribute("dbinstance", info.dbInstance);
	martConfig.setAttribute("dbuser", info.dbUser);
	if (info.dbPassword != null) {
	    martConfig.setAttribute("dbpassword", info.dbPassword);
	}
	return martConfig;
    }
    /**
     * Deserialize a BiomartConfigBean from Element
     */
    private BiomartConfigBean getConfigBeanFromElement(Element e) {
	return new BiomartConfigBean(e.getAttributeValue("dbtype"),
				     e.getAttributeValue("dbdriver"),
				     e.getAttributeValue("dbhost"),
				     e.getAttributeValue("dbport"),
				     e.getAttributeValue("dbinstance"),
				     e.getAttributeValue("dbuser"),
				     e.getAttributeValue("dbpassword"));
    }

    /**
     * Serialize a Query object to Element
     */
    Element queryToElement(Query q) {
	Element e = new Element("query", NAMESPACE);
	
	// Do attributes
	Attribute[] attributes = q.getAttributes();
	if (attributes.length > 0) {
	    Element attributesElement = new Element("attributes", NAMESPACE);
	    e.addContent(attributesElement);
	    for (int i = 0; i < attributes.length; i++) {
		Attribute at = attributes[i];
		if (at instanceof FieldAttribute) {
		    attributesElement.addContent(fieldAttributeToElement((FieldAttribute)at));
		}
	    }
	}
	
	// Do filters
	Filter[] filters = q.getFilters();
	if (filters.length > 0) {
	    Element filtersElement = new Element("filters", NAMESPACE);
	    e.addContent(filtersElement);
	    for (int i = 0; i < filters.length; i++) {
		Filter f = filters[i];
		if (f instanceof BasicFilter) {
		    filtersElement.addContent(basicFilterToElement((BasicFilter)f));
		}
		else if (f instanceof BooleanFilter) {
		    filtersElement.addContent(booleanFilterToElement((BooleanFilter)f));
		}
	    }
	}
	return e;
    }

    /**
     * Deserialize a Query from an Element
     */
    private Query elementToQuery(Element e) {
	// Create new query object
	Query q = new Query();
	
	// Get attributes
	Element attributesElement = e.getChild("attributes", NAMESPACE);
	if (attributesElement != null) {
	    for (Iterator i = attributesElement.getChildren().iterator(); i.hasNext();) {
		Element ae = (Element)i.next();
		if (ae.getName().equals("fieldattribute")) {
		    q.addAttribute(elementToFieldAttribute(ae));
		}
	    }
	}

	// Get filters
	Element filtersElement = e.getChild("filters", NAMESPACE);
	if (filtersElement != null) {
	    for (Iterator i = filtersElement.getChildren().iterator(); i.hasNext();) {
		Element fe = (Element)i.next();
		if (fe.getName().equals("basicfilter")) {
		    q.addFilter(elementToBasicFilter(fe));
		}
		else if (fe.getName().equals("booleanfilter")) {
		    q.addFilter(elementToBooleanFilter(fe));
		}
	    }
	}
	
	return q;	
    }


    /**
     * Serialize FieldAttribute
     */
    private Element fieldAttributeToElement(FieldAttribute f) {
	Element e = new Element("fieldattribute", NAMESPACE);
	e.setAttribute("field",f.getField());
	e.setAttribute("key",f.getKey());
	e.setAttribute("constraint",f.getTableConstraint());
	return e;	
    }
    /**
     * Deserialize FieldAttribute
     */
    private FieldAttribute elementToFieldAttribute(Element e) {
	return new FieldAttribute(e.getAttributeValue("field"),
				  e.getAttributeValue("constraint"),
				  e.getAttributeValue("key"));
    }
    
    /**
     * Serialize BasicFilter
     */
    private Element basicFilterToElement(BasicFilter bf) {
	Element e = new Element("basicfilter", NAMESPACE);
	e.setAttribute("field", bf.getField());
	e.setAttribute("constraint", bf.getTableConstraint());
	e.setAttribute("key", bf.getKey());
	e.setAttribute("qualifier", bf.getQualifier());
	e.setAttribute("value", bf.getValue());
	if (bf.getHandler() != null) {
	    e.setAttribute("handler", bf.getHandler());
	}
	return e;	
    }
    /**
     * Deserialize BasicFilter
     */
    private BasicFilter elementToBasicFilter(Element e) {
	return new BasicFilter(e.getAttributeValue("field"),
			       e.getAttributeValue("constraint"),
			       e.getAttributeValue("key"),
			       e.getAttributeValue("qualifier"),
			       e.getAttributeValue("value"),
			       e.getAttributeValue("handler"));
    }
    
    /**
     * Serialize BooleanFilter
     */
    private Element booleanFilterToElement(BooleanFilter bf) {
	Element e = new Element("booleanfilter", NAMESPACE);
	e.setAttribute("field", bf.getField());
	e.setAttribute("constraint", bf.getTableConstraint());
	e.setAttribute("key", bf.getKey());
	e.setAttribute("qualifier", bf.getQualifier());
	if (bf.getHandler() != null) {
	    e.setAttribute("handler", bf.getHandler());
	}
	return e;
    }
    /**
     * Deserialize BooleanFilter
     */
    private BooleanFilter elementToBooleanFilter(Element e) {
	return new BooleanFilter(e.getAttributeValue("field"),
				 e.getAttributeValue("constraint"),
				 e.getAttributeValue("key"),
				 e.getAttributeValue("qualifier"),
				 e.getAttributeValue("handler"));
    }
    
    
}
