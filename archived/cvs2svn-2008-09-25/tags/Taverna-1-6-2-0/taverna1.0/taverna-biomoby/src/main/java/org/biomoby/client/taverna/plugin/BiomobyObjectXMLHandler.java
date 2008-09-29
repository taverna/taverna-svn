/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
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


public class BiomobyObjectXMLHandler implements XMLHandler {
//  XML element names
    final static String MOBY_SPEC = "biomobyobject";

    final static String MOBY_ENDPOINT = "mobyEndpoint";

    final static String SERVICE_NAME = "serviceName";

    final static String AUTHORITY_NAME = "authorityName";
    
    final static String DESCRIPTION = "description";

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForProcessor(org.embl.ebi.escience.scufl.Processor)
     */
    public Element elementForProcessor(Processor p) {
        BiomobyObjectProcessor bmproc = (BiomobyObjectProcessor) p;
        return getElement(bmproc.getMobyEndpoint(), bmproc.getServiceName(),
                bmproc.getAuthorityName());
    }

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForFactory(org.embl.ebi.escience.scuflworkers.ProcessorFactory)
     */
    public Element elementForFactory(ProcessorFactory pf) {
        BiomobyObjectProcessorFactory bpf = (BiomobyObjectProcessorFactory) pf;
        return getElement(bpf.getMobyEndpoint(), bpf.getServiceName(), bpf
                .getAuthorityName());
    }

    /*
     * 
     */
    private Element getElement(String mobyEndpoint, String serviceName,
            String authorityName) {
        Element spec = new Element(MOBY_SPEC, XScufl.XScuflNS);

        Element mobyEndpointElement = new Element(MOBY_ENDPOINT,
                XScufl.XScuflNS);
        mobyEndpointElement.setText(mobyEndpoint);
        spec.addContent(mobyEndpointElement);

        Element serviceNameElement = new Element(SERVICE_NAME, XScufl.XScuflNS);
        serviceNameElement.setText(serviceName);
        spec.addContent(serviceNameElement);

        Element authorityNameElement = new Element(AUTHORITY_NAME,
                XScufl.XScuflNS);
        authorityNameElement.setText(authorityName);
        spec.addContent(authorityNameElement);

        return spec;
    }

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#getFactory(org.jdom.Element)
     */
    public ProcessorFactory getFactory(Element specElement) {
        Element mobyEndpointElement = specElement.getChild(MOBY_ENDPOINT,
                XScufl.XScuflNS);
        String mobyEndpoint = mobyEndpointElement.getTextTrim();

        Element serviceNameElement = specElement.getChild(SERVICE_NAME,
                XScufl.XScuflNS);
        String serviceName = serviceNameElement.getTextTrim();

        Element authorityNameElement = specElement.getChild(AUTHORITY_NAME,
                XScufl.XScuflNS);
        String authorityName = authorityNameElement.getTextTrim();

        return new BiomobyObjectProcessorFactory(mobyEndpoint, authorityName,
                serviceName);
    }

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#loadProcessorFromXML(org.jdom.Element, org.embl.ebi.escience.scufl.ScuflModel, java.lang.String)
     */
    public Processor loadProcessorFromXML(Element processorNode,
            ScuflModel model, String processorName)
            throws ProcessorCreationException, DuplicateProcessorNameException,
            XScuflFormatException {

        Element biomoby = processorNode.getChild(MOBY_SPEC, XScufl.XScuflNS);

        Element mobyEndpointElement = biomoby.getChild(MOBY_ENDPOINT,
                XScufl.XScuflNS);
        String mobyEndpoint = mobyEndpointElement.getTextTrim();

        Element serviceNameElement = biomoby.getChild(SERVICE_NAME,
                XScufl.XScuflNS);
        String serviceName = serviceNameElement.getTextTrim();

        Element authorityNameElement = biomoby.getChild(AUTHORITY_NAME,
                XScufl.XScuflNS);
        String authorityName = authorityNameElement.getTextTrim();

        return new BiomobyObjectProcessor(model, processorName, authorityName,
                serviceName, mobyEndpoint, false);
    }

}
