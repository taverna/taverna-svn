/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.taverna;

import java.io.*;
import org.jdom.*;
import org.jdom.output.*;
import java.net.*;

/**
 * A set of methods to parse the human readable
 * scufl file format.
 * @author Tom Oinn
 */
public class ScuflParser {

    /**
     * Read in a scufl file from the supplied URL, and write
     * it out into the console. Just a test really.
     */
    public static void main(String[] args) throws Exception {
	System.out.println(getScuflAndParse(args[0]));
    }

    /**
     * Produce a string containing the xscufl representation
     * of the scufl document found at the supplied URL.
     */
    public static String getScuflAndParse(String string_url) throws java.io.IOException {
	URL url = new URL(string_url);
	java.io.InputStream is = url.openStream();
	java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.BufferedInputStream(is));
	StringBuffer sb = new StringBuffer();
	String s = null;
	while ((s = dis.readLine()) != null) {
	    sb.append(s);
	    sb.append("\n");
	}
	Document document = parseScufl(sb.toString());
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	return xo.outputString(document);
    }

    /**
     * Create a JDOM Document containing the XML
     * format representation of the Scufl file
     * passed in as the single string parameter
     */
    public static Document parseScufl(String scufl) {
	
	// Create a root scufl element, version 1.0
	Element root = new Element("scufl",scuflNS());
	Document document = new Document(root);
	root.setAttribute("version","0.1");
	
	// Iterate over all the lines in the input
	String[] lines = scufl.split("\n");
	for (int i = 0; i < lines.length; i++) {
	    doLine(lines[i],root);
	}
	
	// Return the root element
	return document;
    }
    
    /**
     * Process a single line of scufl, creating appropriate
     * element declarations and adding them onto the supplied
     * element.
     */
    private static void doLine(String line, Element root) {
	String[] tokens = line.split(" ");
	
	// Bail if the array is empty
	if (tokens.length == 0) {
	    return;
	}
	
	// Check what the first token is
	if (tokens[0].equalsIgnoreCase("define")) {
	    // define processor <NAME> by <PROTOCOL> <SPECIFIER>
	    if (tokens.length!=6) {
		throw new RuntimeException("Incorrect number of tokens for processor definition.");
	    }
	    doProcessorDefinition(tokens[2],tokens[4],tokens[5],root);
	    return;
	}
	if (tokens[0].equalsIgnoreCase("read")) {
	    // read <INPUT> from <OUTPUT>
	    if (tokens.length!=4) {
		throw new RuntimeException("Incorrect number of tokens for link definition.");
	    }
	    doLinkDefinition(tokens[1],tokens[3],root);
	    return;
	}
	if (tokens[0].equalsIgnoreCase("block")) {
	    // block <NAME> transition <STATE1> to <STATE2> until <NAME2> is <STATE3>
	    if (tokens.length!=10) {
		throw new RuntimeException("Incorrect number of tokens for constraint definition.");
	    }
	    doCoordinationDefinition(tokens[1],tokens[3],tokens[5],tokens[7],tokens[9],root);
	    return;
	}
    }

    /**
     * Create a processor definition
     */
    private static void doProcessorDefinition(String name, String protocol, String specifier, Element root) {
	Element processor = new Element("processor",scuflNS());
	processor.setAttribute("name",name);
	Element spec = new Element(protocol, scuflNS());
	spec.setText(specifier);
	processor.addContent(spec);
	root.addContent(processor);
    }

    /**
     * Create a data link definition
     */
    private static void doLinkDefinition(String input, String output, Element root) {
	Element link = new Element("link",scuflNS());
	Element inputnode = new Element("input",scuflNS());
	inputnode.setText(input);
	Element outputnode = new Element("output",scuflNS());
	outputnode.setText(output);
	link.addContent(inputnode);
	link.addContent(outputnode);
	root.addContent(link);
    }

    /**
     * Create a coordination definition
     */
    private static void doCoordinationDefinition(String name1, String state1, String state2, String name2, String state3, Element root) {
	Element coordination = new Element("coordination",scuflNS());
	Element action = new Element("action",scuflNS());
	Element condition = new Element("condition",scuflNS());
	coordination.addContent(action);
	coordination.addContent(condition);
	Element target = new Element("target",scuflNS());
	target.setText(name1);
	action.addContent(target);
	Element statechange = new Element("statechange",scuflNS());
	Element fromstate = new Element("from",scuflNS());
	Element tostate = new Element("to",scuflNS());
	fromstate.setText(state1);
	tostate.setText(state2);
	statechange.addContent(fromstate);
	statechange.addContent(tostate);
	action.addContent(statechange);
	Element target2 = new Element("target",scuflNS());
	target2.setText(name2);
	Element targetstate = new Element("state",scuflNS());
	targetstate.setText(state3);
	condition.addContent(target2);
	condition.addContent(targetstate);
	root.addContent(coordination);
    }

    /**
     * The namespace for the Scufl elements
     */
    public static Namespace scuflNS() {
	return Namespace.getNamespace("s","http://org.embl.ebi.escience/xscufl/0.1alpha");
    }

}
