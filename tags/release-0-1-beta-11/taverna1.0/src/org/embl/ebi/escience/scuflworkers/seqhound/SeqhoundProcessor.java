/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;


import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Properties;

import java.lang.Class;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.reflect.*;
import org.blueprint.seqhound.*;
import java.util.*;
import java.net.URL;
import org.jdom.*;
import org.jdom.input.*;
import java.io.*;

public class SeqhoundProcessor extends Processor {

    private String methodName, server, path, jseqremServer, jseqremPath;
    
    Map inputTypes = new HashMap();
    SeqHoundForTaverna seqhound;
    Method targetMethod;
    Properties config = new Properties();
    
    static Map descriptions = new HashMap();
    static Map parameters = new HashMap();

    static {
	// Load information from the services.xml if it exists. If not
	// then don't worry too much about it!
	ClassLoader loader = SeqhoundProcessor.class.getClassLoader();
	if (loader == null) {
	    loader = Thread.currentThread().getContextClassLoader();
	}
	URL serviceMetadata = loader.getResource("org/embl/ebi/escience/scuflworkers/seqhound/services.xml");
	if (serviceMetadata != null) {
	    Document document = null;
	    try {
		InputStream is = serviceMetadata.openStream();
		InputStreamReader isr = new InputStreamReader(is);
		SAXBuilder builder = new SAXBuilder(false);
		document = builder.build(isr);
		List services = document.getRootElement().getChildren("service");
		for (Iterator i = services.iterator(); i.hasNext(); ) {
		    Element service = (Element)i.next();
		    String serviceName = service.getAttributeValue("name");
		    // Get the service description from the javadoc
		    String serviceDescription = service.getChild("description").getTextTrim();
		    descriptions.put(serviceName, serviceDescription);
		    // Now get the list of parameter names
		    List parameterElements = service.getChild("parameters").getChildren("parameter");
		    String[] parameterNames = new String[parameterElements.size()];
		    for (int j = 0; j < parameterNames.length; j++) {
			Element parameterElement = (Element)parameterElements.get(j);
			parameterNames[j] = parameterElement.getAttributeValue("name");
		    }
		    parameters.put(serviceName, parameterNames);
		}
	    
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	else {
	    System.out.println("No seqhound service metadata available");
	}
    }

    public SeqhoundProcessor(ScuflModel theModel, 
			     String processorName, 
			     String methodName,
			     String server,
			     String path,
			     String jseqremServer,
			     String jseqremPath) throws 
				 ProcessorCreationException,
				 DuplicateProcessorNameException {
	
	super(theModel, processorName);
	this.methodName = methodName;
	this.server = server;
	this.path = path;
	this.jseqremServer = jseqremServer;
	this.jseqremPath = jseqremPath;
	
	this.config.setProperty("server", server);
	this.config.setProperty("jseqremserver", jseqremServer);
	this.config.setProperty("cgi", path);
	this.config.setProperty("jseqremcgi", jseqremPath);
	
	this.seqhound = new SeqHoundForTaverna(this.config);
	try {
	    // System.out.println("Attempting to create a seqhound proxy instance");
	    this.seqhound.SHoundInit(true, "Taverna");
	    // System.out.println("Done");
	}
	catch (java.io.IOException ioe) {
	    throw new ProcessorCreationException("Unable to contact the seqhound server!");
	}
	String description = (String)descriptions.get(methodName);
	if (description != null) {
	    setDescription(description);
	}
	// Locate the Method object corresponding to this method name
	// and inspect it to get the available inputs and output
	// classes. Use these to then build the appropriate ports
	Class theClass = org.blueprint.seqhound.SeqHound.class;
	Method[] methods = theClass.getDeclaredMethods();
	Method theMethod = null;
	for (int i = 0; i < methods.length; i++) {
	    if (methods[i].getName().equals(methodName)) {
		theMethod = methods[i];
		break;
	    }
	}
	if (theMethod == null) {
	    throw new ProcessorCreationException("Unable to locate method '"+ 
						 methodName + "' in SeqHound API");
	}
	Class parameterTypes[] = theMethod.getParameterTypes();
	Class returnType = theMethod.getReturnType();
	targetMethod = theMethod;
	
	String[] parameterNames = (String[])parameters.get(methodName);
	
	try {
	    for (int i = 0; i < parameterTypes.length; i++) {
		InputPort ip;
		if (parameterNames == null) {
		    ip = new InputPort(this, "in"+i);
		}
		else {
		    ip = new InputPort(this, parameterNames[i]);
		}
		if (parameterTypes[i].isArray()) {
		    ip.setSyntacticType("l('text/plain')");
		}
		else {
		    ip.setSyntacticType("'text/plain'");
		}
		inputTypes.put(ip.getName(), parameterTypes[i]);
		addPort(ip);
	    }
	    if (returnType.isArray()) {
		OutputPort op = new OutputPort(this, "result");
		op.setSyntacticType("l('text/plain')");
		addPort(op);
	    }
	    else if (returnType.equals(java.util.Hashtable.class)) {
		OutputPort op1 = new OutputPort(this, "keys");
		op1.setSyntacticType("l('text/plain')");
		addPort(op1);
		OutputPort op2 = new OutputPort(this, "values");
		op2.setSyntacticType("l('text/plain')");
		addPort(op2);
	    }
	    else {
		OutputPort op = new OutputPort(this, "result");
		op.setSyntacticType("'text/plain'");
		addPort(op);
	    }
	}
	catch (DuplicatePortNameException dpne) {
	    //
	}
	catch (PortCreationException pce) {
	    //
	}
	
    }
    
    public String getMethodName() {
	return this.methodName;
    }

    public String getServer() {
	return this.server;
    }

    public String getPath() {
	return this.path;
    }
    
    public String getJseqremServer() {
	return this.jseqremServer;
    }
    
    public String getJseqremPath() {
	return this.jseqremPath;
    }

    public Properties getProperties() {
	return this.config;
    }

    public String getResourceHost() {
	return this.server;
    }
}
