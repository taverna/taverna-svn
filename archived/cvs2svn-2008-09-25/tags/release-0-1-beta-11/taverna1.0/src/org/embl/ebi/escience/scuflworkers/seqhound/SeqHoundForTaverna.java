/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;

import org.blueprint.seqhound.SeqHound;
import java.util.Properties;
import java.io.*;

/**
 * Extends the basic functionality of the SeqHound class
 * and allows the definition of the remote endpoint etc
 * from parameters in the constructor and init functions
 * rather than from the config file, the latter not working
 * particularly well from an enactment engine as the file
 * may be in an arbitrary place. Besides, we want to be
 * able to specify the endpoints when the scavenger is
 * created.
 * @author Tom Oinn
 */
public class SeqHoundForTaverna extends SeqHound {
    
    /**
     * Create a new SeqHound object specialized for Taverna 
     * including the explicit paths and server names for the
     * various protocols.
     * @param config configure this instance of the SeqHound
     * client using a java Properties object. The available
     * keys and default values in this object are as follows.
     * <table>
     * <tr><th>name</th><th>default</th><th>description</th></tr>
     * <tr><td>server</td><td>seqhound.blueprint.org</td><td>The remote seqhound server to contact</td></tr>
     * <tr><td>jseqremserver</td><td>skinner.blueprint.org:8080</td><td>The server for jseqrem</td></tr>
     * <tr><td>cgi</td><td>/cgi-bin/seqrem</td><td>Path to the seqhound cgi on the server</td></tr>
     * <tr><td>jseqremcgi</td><td>/jseqhound/jseqrem</td><td>Path to the jseqrem cgi on the server</td></tr>
     * </table>
     */
    public SeqHoundForTaverna(Properties config) {
	this.server = config.getProperty("server", "seqhound.blueprint.org");
	this.jseqrem_server = config.getProperty("jseqremserver", "skinner.blueprint.org:8080");
	this.slri_cgi_path = config.getProperty("cgi", "/cgi-bin/seqrem");
	this.jseqrem_cgi_path = config.getProperty("jseqremcgi", "/jseqhound/jseqrem");
	// Set the server 'protocol', really not a protocol at all.
	this.serverProtocol = "http://"+this.server;
	this.jseqrem_serverProtocol = "http://"+this.jseqrem_server;
    }

    /**
     * Initializes the SeqHound object, must be called before
     * any other methods. Tests for an active server and configures
     * the various endpoint properties to use it.
     * @param netEntrezOnToo turn on net entrez on
     * @param appName the applicaton name
     * @return true if SeqHound initialized properly, else false
     * @throws IOException if an I/O Exception occurred
     */
    public boolean SHoundInit(boolean netEntrezOnToo, String appName) 
	throws IOException {
	
	// Check for a valid application name, in our case this
	// will presumably be 'Taverna' but it could be others
	if (appName == null) {
	    return false;
	}
	
	String boo = String.valueOf(netEntrezOnToo); 
	String args = "NetEntrezOnToo=" + boo + "&appname=" + appName;
	
	if (this.SHoundIsServerAlive(null, null, args)) {
	    return (isInited = true);
	}
	else {
	    return false;
	}
    }

    /**
     * Ping the jseqrem server to determine whether it's alive, dumb
     * implementation always returns true.
     */
    protected boolean SHoundIsJseqremServerAlive(String configServerKey, 
						 String path) {
	return true;
    }

    /**
     * Ping the seqhound server, ignores the first two parameters
     * as these will have already been set in the constructor.
     */
    protected boolean SHoundIsServerAlive(String configServerKey,
					  String path, 
					  String args) {
	String address = this.GetFunctionCall("SeqHoundInit", args);
	StringBuffer result = new StringBuffer();
	try {
	    int i = this.ProcessContents(address, result, 600000);
	    if (i == SEQHOUND_OK) {
		return (new Boolean(result.toString())).booleanValue();
	    }
	}
	catch (IOException ioe) {
	    return false;
	}
	return false;
    }	

}
