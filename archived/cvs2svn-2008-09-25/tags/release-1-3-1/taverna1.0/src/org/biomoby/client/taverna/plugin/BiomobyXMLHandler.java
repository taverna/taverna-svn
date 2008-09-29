/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
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
 * Handles XML store and load for the biomoby processor. <p>
 *
 * @version $Id: BiomobyXMLHandler.java,v 1.2 2005-09-15 23:24:59 edwardkawas Exp $
 * @author Martin Senger
 */
public class BiomobyXMLHandler implements XMLHandler {

    // XML element names
    final static String MOBY_SPEC = "biomobywsdl";

    final static String MOBY_ENDPOINT = "mobyEndpoint";

    final static String SERVICE_NAME = "serviceName";

    final static String AUTHORITY_NAME = "authorityName";

    public Element elementForProcessor(Processor p) {
        BiomobyProcessor bmproc = (BiomobyProcessor) p;
        return getElement(bmproc.getMobyEndpoint(), bmproc.getServiceName(),
                bmproc.getAuthorityName());
    }

    public Element elementForFactory(ProcessorFactory pf) {
        BiomobyProcessorFactory bpf = (BiomobyProcessorFactory) pf;
        return getElement(bpf.getMobyEndpoint(), bpf.getServiceName(), bpf
                .getAuthorityName());
    }

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
        return new BiomobyProcessorFactory(mobyEndpoint, authorityName,
                serviceName);
    }

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

        return new BiomobyProcessor(model, processorName, authorityName,
                serviceName, mobyEndpoint);
    }
}