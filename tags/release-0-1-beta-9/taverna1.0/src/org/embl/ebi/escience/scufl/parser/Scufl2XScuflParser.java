/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser;

import org.embl.ebi.escience.scufl.XScufl;

// IO Imports
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * Given an input stream from which to read a Scufl definition
 * file, this class exposes an InputStream that contains the
 * corresponding XScufl representation. This can be used in
 * conjunction with the XScuflParser to populate a ScuflModel
 * directly from the human readable Scufl format.
 * @author Tom Oinn
 */
public class Scufl2XScuflParser {

    /**
     * Read the Scufl format on the supplied InputStream,
     * return a new InputStream from which may be read the
     * XScufl representation.
     */
    public static InputStream parse(InputStream input) throws IOException {
	
	// Load the input data into a StringBuffer 
	StringBuffer inputBuffer = new StringBuffer();
	DataInputStream dis = new DataInputStream(new BufferedInputStream(input));
	String s = null;
	while ((s = dis.readLine()) != null) {
	    inputBuffer.append(s);
	    inputBuffer.append("\n");
	}
	Document document = parseScufl(inputBuffer.toString());
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	String outputString = xo.outputString(document);
	return new ByteArrayInputStream(outputString.getBytes());
    }
    
    /**
     * Read the Scufl format contained within the String
     * and produce a String containing the XML format XScufl
     */
    public static String parse(String input) {
	Document document = parseScufl(input);
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	String outputString = xo.outputString(document);
	return outputString;
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
	return XScufl.XScuflNS;
    }


    
}
