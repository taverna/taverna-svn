/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser;

import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Iterator;
import java.util.List;

// IO Imports
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import java.lang.String;



/**
 * Reads a definition in XScufl format and 
 * populates the supplied model with it.
 * @author Tom Oinn
 */
public class XScuflParser {
    
    /**
     * Read from the given String containing an XScufl document and
     * populate the given ScuflModel with
     * data from the definition. You can optionally
     * specify a name prefix that will be used
     * for all new processors created, this might be useful
     * if you want to import more than one data file
     * into the same model. If the prefix is null,
     * none will be applied. The prefix should not contain
     * characters other than alphanumeric ones, and will
     * have a single underscore appended to it.
     * @exception UnknownProcessorException if a data constraint 
     * refers to a processor that isn't defined in the input
     * @exception UnknownPortException if a data constraint 
     * refers to a port that isn't defined in the input
     * @exception ProcessorCreationException if there is a 
     * general creation failure in a processor, i.e. when attempting
     * to contact soaplab to get its inputs and outputs
     * @exception DataConstraintCreationException if some internal
     * error prevents a data constraint being built
     * @exception DuplicateProcessorNameException if a processor
     * is defined in the input with a name that already exists in the
     * model.
     * @exception MalformedNameException if a data constraint is not
     * specified in the correct format of [PROCESSOR]:[PORT]
     * @exception XScuflFormatException if the format of the input
     * is not valid XScufl, or not valid XML.
     */
    public static void populate(String input, ScuflModel model, String prefix) 
	throws UnknownProcessorException,
	       UnknownPortException,
	       ProcessorCreationException,
	       DataConstraintCreationException,
	       DuplicateProcessorNameException,
	       MalformedNameException,
	       ConcurrencyConstraintCreationException,
	DuplicateConcurrencyConstraintNameException,
	       XScuflFormatException {
	try {
	    SAXBuilder builder = new SAXBuilder(false);
	    Document document = builder.build(new StringReader(input));
	    populate(document, model, prefix);
	}
	catch (JDOMException jde) {
	    throw new XScuflFormatException("Unable to load XScufl file, error : "+jde.getMessage());
	}
	
    }

    /**
     * Read from the given InputStream and
     * populate the given ScuflModel with
     * data from the definition. You can optionally
     * specify a name prefix that will be used
     * for all new processors created, this might be useful
     * if you want to import more than one data file
     * into the same model. If the prefix is null,
     * none will be applied. The prefix should not contain
     * characters other than alphanumeric ones, and will
     * have a single underscore appended to it.
     * @exception UnknownProcessorException if a data constraint 
     * refers to a processor that isn't defined in the input
     * @exception UnknownPortException if a data constraint 
     * refers to a port that isn't defined in the input
     * @exception ProcessorCreationException if there is a 
     * general creation failure in a processor, i.e. when attempting
     * to contact soaplab to get its inputs and outputs
     * @exception DataConstraintCreationException if some internal
     * error prevents a data constraint being built
     * @exception DuplicateProcessorNameException if a processor
     * is defined in the input with a name that already exists in the
     * model.
     * @exception MalformedNameException if a data constraint is not
     * specified in the correct format of [PROCESSOR]:[PORT]
     * @exception XScuflFormatException if the format of the input
     * is not valid XScufl, or not valid XML.
     */
    public static void populate(InputStream is, ScuflModel model, String prefix) 
	throws UnknownProcessorException,
	       UnknownPortException,
	       ProcessorCreationException,
	       DataConstraintCreationException,
	       DuplicateProcessorNameException,
	       MalformedNameException,	       
	ConcurrencyConstraintCreationException,
	DuplicateConcurrencyConstraintNameException,
	       XScuflFormatException {
	
	// Load the data into a JDom Document
	InputStreamReader isr = new InputStreamReader(is);
	SAXBuilder builder = new SAXBuilder(false);
	Document document = null;
	try {
	    document = builder.build(isr);
	}
	catch (JDOMException jde) {
	    throw new XScuflFormatException("Unable to load XScufl file, error : "+jde.getMessage());
	}
	populate(document, model, prefix);
	
    }
    
    /**
     * Read from the given JDOM Document and
     * populate the given ScuflModel with
     * data from the definition. You can optionally
     * specify a name prefix that will be used
     * for all new processors created, this might be useful
     * if you want to import more than one data file
     * into the same model. If the prefix is null,
     * none will be applied. The prefix should not contain
     * characters other than alphanumeric ones, and will
     * have a single underscore appended to it.
     * @exception UnknownProcessorException if a data constraint 
     * refers to a processor that isn't defined in the input
     * @exception UnknownPortException if a data constraint 
     * refers to a port that isn't defined in the input
     * @exception ProcessorCreationException if there is a 
     * general creation failure in a processor, i.e. when attempting
     * to contact soaplab to get its inputs and outputs
     * @exception DataConstraintCreationException if some internal
     * error prevents a data constraint being built
     * @exception DuplicateProcessorNameException if a processor
     * is defined in the input with a name that already exists in the
     * model.
     * @exception MalformedNameException if a data constraint is not
     * specified in the correct format of [PROCESSOR]:[PORT]
     * @exception XScuflFormatException if the format of the input
     * is not valid XScufl, or not valid XML.
     */
    public static void populate(Document document, ScuflModel model, String prefix)
	throws UnknownProcessorException,
	       UnknownPortException,
	       ProcessorCreationException,
	       DataConstraintCreationException,
	       DuplicateProcessorNameException,
	       MalformedNameException,
	       ConcurrencyConstraintCreationException,
	DuplicateConcurrencyConstraintNameException,
	       XScuflFormatException {
	// Check whether we're using prefixes
	boolean usePrefix = false;
	if (prefix != null) {
	    usePrefix = true;
	}
	Element root = document.getRootElement();
	Namespace namespace = root.getNamespace();

	// Build processors
	// All processors are nodes of form <processor name="foo"> .... </processor>
	List processors = root.getChildren("processor", namespace);
	//System.out.println("Found "+processors.size()+" processor nodes.");
	for (Iterator i = processors.iterator(); i.hasNext(); ) {
	    Element processorNode = (Element)i.next();
	    String name = processorNode.getAttributeValue("name");
	    if (usePrefix) {
		name = prefix+"_"+name;
	    }
	    boolean foundSpec = false;
	    
	    // Handle soaplab
	    Element soaplab = processorNode.getChild("soaplabwsdl",namespace);
	    if (soaplab != null) {
		foundSpec = true;
		// Get the textual endpoint
		String endpoint = soaplab.getTextTrim();
		// Check the URL for validity
		try {
		    URL endpointURL = new URL(endpoint);
		}
		catch (MalformedURLException mue) {
		    throw new XScuflFormatException("The url specified for the soaplab endpoint for '"+name+"' was invalid : "+mue);
		}
		model.addProcessor(new SoaplabProcessor(model, name, endpoint));
	    }

	    // Handle arbitrarywsdl
	    Element wsdlProcessor = processorNode.getChild("arbitrarywsdl",namespace);
	    if (wsdlProcessor != null && !foundSpec) {
		foundSpec = true;
	        String wsdlLocation = wsdlProcessor.getChild("wsdl",namespace).getTextTrim();
		String portTypeName = wsdlProcessor.getChild("porttype",namespace).getTextTrim();
		String operationName = wsdlProcessor.getChild("operation",namespace).getTextTrim();
		//throw new XScuflFormatException(wsdlLocation+","+portTypeName+","+operationName);
		model.addProcessor(new WSDLBasedProcessor(model, name, wsdlLocation, portTypeName, operationName));
	    }

	    // Handle talisman
	    Element talismanProcessor = processorNode.getChild("talisman",namespace);
	    if (talismanProcessor != null && !foundSpec) {
		foundSpec = true;
		String tscriptURL = talismanProcessor.getChild("tscript",namespace).getTextTrim();
		model.addProcessor(new TalismanProcessor(model, name, tscriptURL));
	    }
	    
	    // If no specifier has been found then throw an exception
	    if (!foundSpec) {
		throw new XScuflFormatException("Couldn't find a known specification mechanism for processor node '"+name+"'");
	    }
	    // End iterator over processors
	}

	// Iterate over the external declarations and create appropriate input and output 
	// ports in the internal source and sink processors.
	List sourceList = root.getChildren("source", namespace);
	Processor sourceHolder = model.getWorkflowSourceProcessor();
	for (Iterator i = sourceList.iterator(); i.hasNext(); ) {
	    Element sourceElement = (Element)i.next();
	    String portName = sourceElement.getTextTrim();
	    try {
		if (usePrefix) {
		    portName = prefix+"_"+portName;
		}
		sourceHolder.addPort(new OutputPort(sourceHolder, portName));
	    }
	    catch (DuplicatePortNameException dpne) {
		throw new XScuflFormatException("You have a duplicate source port in your definition file, aborting.");
	    }
	    catch (PortCreationException pce) {
		throw new XScuflFormatException("Unable to create source port.");
	    }
	}
	List sinkList = root.getChildren("sink", namespace);
	Processor sinkHolder = model.getWorkflowSinkProcessor();
	for (Iterator i = sinkList.iterator(); i.hasNext(); ) {
	    Element sinkElement = (Element)i.next();
	    String portName = sinkElement.getTextTrim();
	    try {
		if (usePrefix) {
		    portName = prefix+"_"+portName;
		}
		sinkHolder.addPort(new InputPort(sinkHolder, portName));
	    }
	    catch (DuplicatePortNameException dpne) {
		throw new XScuflFormatException("You have a duplicate sink port in your defintion file, aborting.");
	    } 
	    catch (PortCreationException pce) {
		throw new XScuflFormatException("Unable to create sink port.");
	    }
	}

	// Build data constraints
	List dataConstraintList = root.getChildren("link",namespace);
	for (Iterator i = dataConstraintList.iterator(); i.hasNext(); ) {
	    Element linkElement = (Element)i.next();
	    Element inputElement = linkElement.getChild("input",namespace);
	    Element outputElement = linkElement.getChild("output",namespace);
	    if (inputElement == null) {
		throw new XScuflFormatException("A data constraint must have an input child element");
	    }
	    if (outputElement == null) {
		throw new XScuflFormatException("A data constraint must have an output child element");
	    }
	    String inputName = inputElement.getTextTrim();
	    String outputName = outputElement.getTextTrim();
	    if (usePrefix) {
		inputName = prefix+"_"+inputName;
		outputName = prefix+"_"+outputName;
	    }
	    model.addDataConstraint(new DataConstraint(model, outputName, inputName));
	   				    
	    // End iterator over data constraints
	}

	// Iterate over external port declarations
	// DEPRECATED!
	
	List externalPorts = root.getChildren("external",namespace);
	for (Iterator i = externalPorts.iterator(); i.hasNext(); ) {
	    Element external = (Element)i.next();
	    // Should be in the form 'processor:port'
	    String specifier = external.getTextTrim();
	    if (usePrefix) {
		specifier = prefix+"_"+specifier;
	    }
	    Port thePort = model.locatePort(specifier);
	    thePort.setExternal(true);
	}
       


	// Build concurrency constraints
	List concurrencyConstraints = root.getChildren("coordination",namespace);
	for (Iterator i = concurrencyConstraints.iterator(); i.hasNext(); ) {
	    Element coordination = (Element)i.next();
	    String constraintName = coordination.getAttributeValue("name");
	    if (usePrefix) {
		constraintName = prefix+"_"+constraintName;
	    }
	    Element condition = coordination.getChild("condition",namespace);
	    Element action = coordination.getChild("action",namespace);
	    String controllerName = condition.getChild("target",namespace).getTextTrim();
	    if (usePrefix) {
		controllerName = prefix+"_"+controllerName;
	    }
	    Processor controller = model.locateProcessor(controllerName);
	    int controllerStateGuard = ConcurrencyConstraint.statusStringToInt(condition.getChild("state",namespace).getTextTrim());
	    String targetName = action.getChild("target",namespace).getTextTrim();
	    if (usePrefix) {
		targetName = prefix+"_"+targetName;
	    }
	    Processor target = model.locateProcessor(targetName);
	    int targetStateTo = 
		ConcurrencyConstraint.statusStringToInt(action.getChild("statechange",namespace).getChild("to",namespace).getTextTrim());
	    int targetStateFrom = 
		ConcurrencyConstraint.statusStringToInt(action.getChild("statechange",namespace).getChild("from",namespace).getTextTrim());
	    model.addConcurrencyConstraint(new ConcurrencyConstraint(model,
								     constraintName,
								     controller,
								     target,
								     targetStateFrom,
								     targetStateTo,
								     controllerStateGuard));
	}
	
    }

}
