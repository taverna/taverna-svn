/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

// Utility Imports
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.*;
import javax.swing.tree.*;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import java.lang.Class;
import java.lang.String;

// Network Imports
import java.net.URL;

import org.apache.log4j.Logger;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;

import org.blueprint.seqhound.*;

import java.lang.reflect.*;


/**
 * A scavenger which introspects over the Seqhound java API to
 * fetch all methods available and expose them as components within
 * Taverna.
 * @author Tom Oinn
 */
public class SeqhoundProcessorFactory extends ProcessorFactory {
    
    private String methodName, server, path, jseqremServer, jseqremPath;

    /**
     * Create a new processor factory
     */
    public SeqhoundProcessorFactory(String methodName,
				    String server,
				    String path,
				    String jseqremServer,
				    String jseqremPath) {
	this.methodName = methodName;
	this.server = server;
	this.path = path;
	this.jseqremServer = jseqremServer;
	this.jseqremPath = jseqremPath;
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
    public String toString() {
	// Trim off the initial 'SHound'
	return this.methodName.split("SHound")[1];
    }
    
    public String getProcessorDescription() {
	return "Processor based on the Blueprint Initiative's SeqHound server";
    }

    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.seqhound.SeqhoundProcessor.class;
    }

}
