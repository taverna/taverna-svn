/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.biomoby;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.*;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

// import java.lang.NullPointerException;
// import java.lang.Object;
// import java.lang.String;

import org.biomoby.client.*;
import org.biomoby.shared.*;


/**
 * A processor based on the Biomoby compliant web services.  This
 * processor implementation will contact Biomoby registry in order to
 * find the list of extant ports at creation time. <p>
 *
 * @version $Id: BiomobyProcessor.java,v 1.1 2004-04-01 14:31:34 mereden Exp $
 * @author Martin Senger
 */
public class BiomobyProcessor extends Processor implements java.io.Serializable {

    private URL endpoint = null;
    private String mobyEndpoint = null;
    private Central worker = null;
    private MobyService mobyService = null;

    /**
     * Construct a new processor with the given model and name,
     * delegates to the superclass.
     */
    public BiomobyProcessor (ScuflModel model,
			     String processorName,
			     String authorityName, String serviceName,
			     String mobyEndpoint)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super (model, processorName);
	this.mobyEndpoint = mobyEndpoint;

	// Find the service endpoint (by calling Moby registry)
	try {
	    worker = new CentralImpl (mobyEndpoint);

	    MobyService pattern = new MobyService (serviceName);
	    pattern.setAuthority (authorityName);
	    MobyService[] services = worker.findService (pattern);
	    if (services == null || services.length == 0)
		throw new ProcessorCreationException (formatError ("I cannot find the service."));
	    mobyService = services[0];
	    String serviceEndpoint = mobyService.getURL();
	    if (serviceEndpoint == null || serviceEndpoint.equals (""))
		throw new ProcessorCreationException (formatError ("Service has an empty endpoint."));
	    try {
		setEndpoint (serviceEndpoint);
	    } catch (MalformedURLException e2) {
		throw new ProcessorCreationException (formatError ("Service has malformed endpoint: '" +
								   serviceEndpoint + "'."));
	    }
	} catch (Exception e) {
	    throw new ProcessorCreationException (formatError (e.toString()));
	}
    }

    /**
     * Get the properties for this processor for display purposes
     */

    public Properties getProperties() {

	Properties props = new Properties();
	props.put ("Biomoby service URL", getEndpoint().toString());
	return props;
    }

    /**
     * Set the endpoint for this biomoby processor
     */
    void setEndpoint (String specifier)
	throws MalformedURLException,
	       ProcessorCreationException {

	URL new_endpoint = new URL(specifier);
	if (this.endpoint != null) {
	    if (! this.endpoint.equals (new_endpoint)) {
		fireModelEvent (new ScuflModelEvent (this, "Service endpoint changed to '"+specifier+"'"));

	    } else {
		// Do nothing if the endpoint was the same as before
		return;
	    }
	} else {
	    fireModelEvent (new ScuflModelEvent (this, "Service endpoint set to '"+specifier+"'"));
	}

	this.endpoint = new_endpoint;

	try {
	    generatePorts();
	    getDescriptionText();
	} catch (PortCreationException e) {
	    throw new ProcessorCreationException (formatError ("When trying to create ports: " +
							       e.getMessage()));
	} catch (DuplicatePortNameException e) {
	    throw new ProcessorCreationException (formatError ("When trying to create ports: " +
							       e.getMessage()));
	}
    }

    /**
     * Set the description field
     */
    public void getDescriptionText() 
	throws ProcessorCreationException {

	if (mobyService.getDescription() != null)
	    setDescription (mobyService.getDescription());
    }

    /**
     * Use the endpoint data to create new ports and attach them to
     * the processor.
     *
     * TBD better - for now just take that every service eats a string
     * and produces a string... (MS)
     */
    public void generatePorts()
	throws ProcessorCreationException,
	PortCreationException,
	DuplicatePortNameException {

	// Wipe the existing port declarations
	ports = new ArrayList();

	// inputs
	Port input_port = new InputPort (this, "input");
	input_port.setSyntacticType ("'text/xml'");
	this.addPort (input_port);
	    
	// outputs
	Port output_port = new OutputPort (this, "output");
	output_port.setSyntacticType ("'text/xml'");
	this.addPort (output_port);
    }

    /**
     * Get the URL for this endpoint
     */
    public URL getEndpoint() {
	return this.endpoint;
    }

    /**
     * Get the name of this Moby-compliant service
     */
    public String getServiceName() {
	return mobyService.getName();
    }

    /**
     * Get the authority of this Moby-compliant service
     */
    public String getAuthorityName() {
	return mobyService.getAuthority();
    }

    //
    protected String formatError (String msg) {
	return ("Problems with service '" + mobyService.getName() +
		"' provided by authority '" + mobyService.getAuthority() +
		"' from Moby registry at " + mobyEndpoint +
		": " + msg);
    }
}
