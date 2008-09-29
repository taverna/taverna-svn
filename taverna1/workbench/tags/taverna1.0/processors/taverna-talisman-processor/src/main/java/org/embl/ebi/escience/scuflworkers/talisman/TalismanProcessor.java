/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;



/**
 * A processor that makes use of a local installation
 * of the Talisman classes to perform its operations.
 * Construct with a talisman script document describing
 * the inputs and outputs, see the tscript.xml document
 * in the test package for an example.
 * @author Tom Oinn
 */
public class TalismanProcessor extends Processor implements java.io.Serializable {

    // Holds the string form of the URL that the script is
    // loaded from to create the processor. Effectively the
    // script defines the service.
    private String tscriptURL = null;
    private URL tscriptURLObject = null;
    // Hash of input port names to Talisman node names, the
    // key is the port name, so i.e. 'input1=field:foo'
    private Map inputs = null;
    // Hash of output port names to Talisman node names, the
    // key is the port name as in the inputs hash.
    private Map outputs = null;
    // The path to the trigger to invoke when invoking this
    // service.
    private String triggerName = null;
    // The path to the Talisman definition file used by this
    // service.
    private String talismanDefinitionURL = null;

    /**
     * Return the URL of the talisman script used
     * to build this processor (string form)
     */
    public String getTScriptURL() {
	return this.tscriptURL;
    }
    /**
     * Return the URL of the talisman script used
     * to build this processor (URL object)
     */
    public URL getTScriptURLObject() {
	return this.tscriptURLObject;
    }
    
    /**
     * Return the map of input port name (key) to talisman field locator (value)
     */
    public Map getInputMappings() {
	return this.inputs;
    }
    
    /**
     * Return the map of output port name (key) to talisman field locator (value)
     */
    public Map getOutputMappings() {
	return this.outputs;
    }

    /**
     * Return the name of the trigger that gets invoked in this processor invocation
     */
    public String getTriggerName() {
	return this.triggerName;
    }

    /**
     * Return the URL that the talisman page definition is found at (string form)
     */
    public String getTalismanDefinitionURL() {
	return this.talismanDefinitionURL;
    }

    /**
     * Construct a new processor from the supplied Talisman
     * script document.
     */
    public TalismanProcessor(ScuflModel model, String name, String tscript)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	this.tscriptURL = tscript;
	this.inputs = new HashMap();
	this.outputs = new HashMap();
	// Now read from the tscript url to get the input
	// and output hashes as well as the url and trigger name
	// The script looks something like this :
	// <tscript url="..." trigger="...">
	//  <input name="inputPortName">field:foo</input>
	//  ...
	//  <output name="outputPortName">field:bar</output>
	//  ...
	// </tscript>
	try {
	    URL scriptLocation = new URL(tscript);
	    this.tscriptURLObject = scriptLocation;
	    SAXBuilder builder = new SAXBuilder(false);
	    Document tscriptDocument = builder.build(scriptLocation.openStream());
	    Element tscriptElement = tscriptDocument.getRootElement();
	    this.triggerName = tscriptElement.getAttributeValue("trigger");
	    this.talismanDefinitionURL = tscriptElement.getAttributeValue("url");
	    // Get inputs
	    List inputElements = tscriptElement.getChildren("input");
	    for (Iterator i = inputElements.iterator(); i.hasNext(); ) {
		Element inputElement = (Element)i.next();
		// Add port name -> talisman name to input map
		this.inputs.put(inputElement.getAttributeValue("name"), inputElement.getTextTrim());
		// Create new port
		try {
		    Port newPort = new InputPort(this, inputElement.getAttributeValue("name"));
		    // All talisman ports are string for now, will adapt this if needed!
		    newPort.setSyntacticType("'text/plain'");
		    // Register port
		    this.addPort(newPort);
		}
		catch (PortCreationException pce) {
		    throw new ProcessorCreationException("Unable to create port '"+inputElement.getAttributeValue("name")+"' in '"+name+"' : "+pce.getMessage());
		}
		catch (DuplicatePortNameException dpne) {
		    throw new ProcessorCreationException("Duplicate port name '"+inputElement.getAttributeValue("name")+"' in '"+name+"' : "+dpne.getMessage());
		}
	    }
	    // Get outputs
	    List outputElements = tscriptElement.getChildren("output");
	    for (Iterator i = outputElements.iterator(); i.hasNext(); ) {
		Element outputElement = (Element)i.next();
		// Add port name -> talisman name to output map
		this.outputs.put(outputElement.getAttributeValue("name"), outputElement.getTextTrim());
		// Create new port
		try {
		    Port newPort = new OutputPort(this, outputElement.getAttributeValue("name"));
		    // All talisman ports are string for now, will adapt this if needed!
		    newPort.setSyntacticType("'text/plain'");
		    // Register port
		    this.addPort(newPort);
		}	
		catch (PortCreationException pce) {
		    throw new ProcessorCreationException("Unable to create port '"+outputElement.getAttributeValue("name")+"' in '"+name+"' : "+pce.getMessage());
		}
		catch (DuplicatePortNameException dpne) {
		    throw new ProcessorCreationException("Duplicate port name '"+outputElement.getAttributeValue("name")+"' in '"+name+"' : "+dpne.getMessage());
		}
		
	    }
	    
	}
	catch (JDOMException jde) {
	    throw new ProcessorCreationException("Unable to instantiate the TalismanProcessor '"+name+"', error was : "+jde.getMessage());
	}
	catch (MalformedURLException mue) {
	    throw new ProcessorCreationException("Unable to read script from '"+tscript+"' for '"+name+"', error was : "+mue.getMessage());
	}
	catch (IOException ioe) {
	     throw new ProcessorCreationException("Unable to read script from '"+tscript+"' for '"+name+"', error was : "+ioe.getMessage());
	}

    }
    
    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	props.put("TScript URL",getTScriptURL());
	return props;
    }
}
