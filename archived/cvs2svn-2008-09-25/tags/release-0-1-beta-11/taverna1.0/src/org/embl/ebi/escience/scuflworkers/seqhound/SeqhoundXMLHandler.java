/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// JDOM Imports
import org.jdom.Element;

/**
 * Handle the XML stuff for the seqhound operation
 * @author Tom Oinn
 */
public class SeqhoundXMLHandler implements XMLHandler {

    public Element elementForProcessor(Processor p) {
	SeqhoundProcessor sp = (SeqhoundProcessor)p;
	return getElement(sp.getMethodName(), sp.getServer(), sp.getPath(),
			  sp.getJseqremServer(), sp.getJseqremPath());
    }

    public Element elementForFactory(ProcessorFactory pf) {
	SeqhoundProcessorFactory spf = (SeqhoundProcessorFactory)pf;
	return getElement(spf.getMethodName(), spf.getServer(), spf.getPath(),
			  spf.getJseqremServer(), spf.getJseqremPath());
    }

    public Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, 
	       DuplicateProcessorNameException, 
	       XScuflFormatException {
	Element seqhoundProcessor = processorNode.getChild("seqhound", XScufl.XScuflNS);
	// Method name is mandatory
	String methodName = seqhoundProcessor.getChild("method", XScufl.XScuflNS).getTextTrim();
	
	String server = seqhoundProcessor.getChild("server", XScufl.XScuflNS).getTextTrim();
	String path = seqhoundProcessor.getChild("path", XScufl.XScuflNS).getTextTrim();
	String jseqremServer = seqhoundProcessor.getChild("jseqremserver", XScufl.XScuflNS).getTextTrim();
	String jseqremPath = seqhoundProcessor.getChild("jseqrempath", XScufl.XScuflNS).getTextTrim();
	
	return new SeqhoundProcessor(model, name, methodName, server, path, jseqremServer, jseqremPath);
    }
    
    public ProcessorFactory getFactory(Element specElement) {
	String methodName = specElement.getChild("method", XScufl.XScuflNS).getTextTrim();
	String server = specElement.getChild("server", XScufl.XScuflNS).getTextTrim();
	String path = specElement.getChild("path", XScufl.XScuflNS).getTextTrim();
	String jseqremServer = specElement.getChild("jseqremserver", XScufl.XScuflNS).getTextTrim();
	String jseqremPath = specElement.getChild("jseqrempath", XScufl.XScuflNS).getTextTrim();
	return new SeqhoundProcessorFactory(methodName, server, path, jseqremServer, jseqremPath);
    }

    private Element getElement(String methodName, String server, 
			       String path, String jseqremServer, 
			       String jseqremPath) {
	Element spec = new Element("seqhound", XScufl.XScuflNS);
	Element methodElement = new Element("method", XScufl.XScuflNS);
	methodElement.setText(methodName);
	spec.addContent(methodElement);
	Element serverElement = new Element("server", XScufl.XScuflNS);
	serverElement.setText(server);
	spec.addContent(serverElement);
	Element jseqremServerElement = new Element("jseqremserver", XScufl.XScuflNS);
	jseqremServerElement.setText(jseqremServer);
	spec.addContent(jseqremServerElement);
	Element pathElement = new Element("path", XScufl.XScuflNS);
	pathElement.setText(path);
	spec.addContent(pathElement);
	Element jseqremPathElement = new Element("jseqrempath", XScufl.XScuflNS);
	jseqremPathElement.setText(jseqremPath);
	spec.addContent(jseqremPathElement);
	return spec;
    }

}
