/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.jdom.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.ensembl.mart.lib.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;


public class BiomartXMLHandler implements XMLHandler {
    
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
	return getElement(bpf.getConfig(), bpf.getDataSourceName(), null);
    }

    /**
     * Build a new BiomartProcessorFactory from the specified element
     */
    public ProcessorFactory getFactory(Element specElement) {
	Element martConfig = specElement.getChild("biomartconfig", XScufl.XScuflNS);
	Element dsNameElement = specElement.getChild("biomartds", XScufl.XScuflNS);
	Element queryElement = specElement.getChild("biomartquery", XScufl.XScuflNS);
	// Ignores Query for now
	return new BiomartProcessorFactory(getConfigBeanFromElement(martConfig),
					   dsNameElement.getTextTrim());
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
	Element martConfig = biomart.getChild("biomartconfig", XScufl.XScuflNS);
	Element dsNameElement = biomart.getChild("biomartds", XScufl.XScuflNS);
	Element queryElement = biomart.getChild("biomartquery", XScufl.XScuflNS);
	// Ignore Query for now, code doesn't yet exist to serialize / deserialize it
	return new BiomartProcessor(model,
				    processorName,
				    getConfigBeanFromElement(martConfig),
				    dsNameElement.getTextTrim());
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
	Element dsElement = new Element("biomartds", XScufl.XScuflNS);
	dsElement.setText(dataSourceName);
	spec.addContent(dsElement);	
	return spec;
    }

    /**
     * Pull back an Element corresponding to the specified config
     * @param info the BiomartConfigBean to serialize to an Element
     */
    private Element getConfigElement(BiomartConfigBean info) {
	Element martConfig = new Element("biomartconfig", XScufl.XScuflNS);
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
    
    private BiomartConfigBean getConfigBeanFromElement(Element e) {
	return new BiomartConfigBean(e.getAttributeValue("dbtype"),
				     e.getAttributeValue("dbdriver"),
				     e.getAttributeValue("dbhost"),
				     e.getAttributeValue("dbport"),
				     e.getAttributeValue("dbinstance"),
				     e.getAttributeValue("dbuser"),
				     e.getAttributeValue("dbpassword"));
    }

}
