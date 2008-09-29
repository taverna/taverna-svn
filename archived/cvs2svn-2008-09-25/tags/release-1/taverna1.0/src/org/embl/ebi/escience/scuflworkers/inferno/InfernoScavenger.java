/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import java.net.URL;
import java.net.InetSocketAddress;

import javax.swing.tree.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import uk.ac.rdg.resc.jstyx.client.StyxClientSession;
import uk.ac.rdg.resc.jstyx.client.CStyxFile;
import uk.ac.rdg.resc.jstyx.client.StyxFileInputStream;
import uk.ac.rdg.resc.jstyx.client.StyxFileInputStreamReader;
import uk.ac.rdg.resc.jstyx.client.StyxFileOutputStream;
import uk.ac.rdg.resc.jstyx.client.StyxFileOutputStreamWriter;
import uk.ac.rdg.resc.jstyx.StyxException;

/**
 * Collects the various services within a specified SGS container
 * @author Tom Oinn
 */
public class InfernoScavenger extends Scavenger {

    /**
     * Create a new inferno scavenger with the specified
     * host and port
     */
    public InfernoScavenger(String host, int port)
	throws ScavengerCreationException {
	super("Inferno @ "+host+":"+port);
	try {
	    StyxClientSession session = 
		StyxClientSession.createSession(host, port);
	    session.connect();
	    CStyxFile directory = new CStyxFile(session, "/");
	    if (directory.isDirectory() == false) {
		throw new ScavengerCreationException("Root isn't a directory, bad!");
	    }
	    CStyxFile[] services = directory.getChildren();
	    for (int i = 0; i < services.length; i++) {
		CStyxFile serviceNode = services[i];
		String serviceName = serviceNode.getName();
		InfernoProcessorFactory ipf =
		    new InfernoProcessorFactory(host, port, serviceName);
		add(new DefaultMutableTreeNode(ipf));
	    }
	    session.close();
	}
	catch (Exception ex) {
	    if (ex instanceof ScavengerCreationException) {
		throw (ScavengerCreationException)ex;
	    }
	    ScavengerCreationException sce = 
		new ScavengerCreationException("Unable to create inferno scavenger : "+
					       ex.getMessage());
	    sce.initCause(ex);
	    throw sce;
	}
    }

}
