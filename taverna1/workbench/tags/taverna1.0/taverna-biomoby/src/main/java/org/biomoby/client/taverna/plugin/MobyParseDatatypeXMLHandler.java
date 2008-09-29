/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;

/**
 * Handles XML store and load for the Moby Datatype Parser
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeXMLHandler implements XMLHandler {
	
    final String MOBY_ELEMENT = "biomobyparser";

    final String ENDPOINT = "endpoint";

    final String DATATYPE_NAME = "datatype";

    final String ARTICLE_NAME = "articleName";
    
    final String INPUT_PORT = "input";
    
    final String OUTPUT_PORTS = "outputs";
    
    final String DESC = "description";

    /*
     *  (non-Javadoc)
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForProcessor(org.embl.ebi.escience.scufl.Processor)
     */
	public Element elementForProcessor(Processor p) {
		MobyParseDatatypeProcessor parser = (MobyParseDatatypeProcessor) p;
        return getElement(parser.getRegistryEndpoint(), parser.getDescription(), parser.getDatatypeName(), parser.getArticleNameUsedByService());
	}

	/*
	 *  (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForFactory(org.embl.ebi.escience.scuflworkers.ProcessorFactory)
	 */
	public Element elementForFactory(ProcessorFactory pf) {
		MobyParseDatatypeProcessorFactory parser = (MobyParseDatatypeProcessorFactory) pf;
        return getElement(parser.getEndpoint(), parser.getDescription(), parser.getDatatypeName(), parser.getArticleName());
	}

	/*
	 *  (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.XMLHandler#getFactory(org.jdom.Element)
	 */
	public ProcessorFactory getFactory(Element specElement) {
		Element mobyEndpointElement = specElement.getChild(ENDPOINT,
                XScufl.XScuflNS);
        String endpoint = mobyEndpointElement.getTextTrim();

        Element datatype_name = specElement.getChild(DATATYPE_NAME,
                XScufl.XScuflNS);
        String dtName = datatype_name.getTextTrim();

        Element article = specElement.getChild(ARTICLE_NAME,
                XScufl.XScuflNS);
        String artName = article.getTextTrim();

        MobyParseDatatypeProcessorFactory pf = new MobyParseDatatypeProcessorFactory();
        pf.setArticleName(artName);
        pf.setDatatypeName(dtName);
        pf.setEndpoint(endpoint);
        
        return pf;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.XMLHandler#loadProcessorFromXML(org.jdom.Element, org.embl.ebi.escience.scufl.ScuflModel, java.lang.String)
	 */
	public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
			throws ProcessorCreationException, DuplicateProcessorNameException,
			XScuflFormatException {
		Element biomoby = processorNode.getChild(MOBY_ELEMENT, XScufl.XScuflNS);
		
		Element mobyEndpointElement = biomoby.getChild(ENDPOINT,
                XScufl.XScuflNS);
        String endpoint = mobyEndpointElement.getTextTrim();

        Element datatype_name = biomoby.getChild(DATATYPE_NAME,
                XScufl.XScuflNS);
        String dtName = datatype_name.getTextTrim();

        Element article = biomoby.getChild(ARTICLE_NAME,
                XScufl.XScuflNS);
        String artName = article.getTextTrim();
        return new MobyParseDatatypeProcessor(model, name, dtName, artName, endpoint);
        
	}
	/*
	 * convenience method that creates a scufl xml fragment  
	 */
	 private Element getElement(String endpoint, String description, String datatypeName,
	            String articleName) {
	        Element spec = new Element(MOBY_ELEMENT, XScufl.XScuflNS);

	        Element mobyEndpointElement = new Element(ENDPOINT,
	                XScufl.XScuflNS);
	        mobyEndpointElement.setText(endpoint);
	        spec.addContent(mobyEndpointElement);

	        Element datatype_name = new Element(DATATYPE_NAME, XScufl.XScuflNS);
	        datatype_name.setText(datatypeName);
	        spec.addContent(datatype_name);

	        Element article = new Element(ARTICLE_NAME,
	                XScufl.XScuflNS);
	        article.setText(articleName);
	        spec.addContent(article);
	        
	        Element desc = new Element(DESC,
	                XScufl.XScuflNS);
	        desc.setText(description);
	        spec.addContent(desc);
	        
	        //TODO save the min/max/enum values for use when workflow is offline.

	        return spec;
	    }
}
