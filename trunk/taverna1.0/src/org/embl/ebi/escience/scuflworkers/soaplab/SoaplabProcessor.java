/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;




/**
 * A processor based on the Soaplab web service
 * around the EMBOSS tools. This processor
 * implementation will contact Soaplab in order
 * to find the list of extant ports at creation
 * time. It is therefore important when creating
 * an instance of this class that the creating
 * thread should be able to make an HTTP connection
 * to the supplied endpoint.
 * @author Tom Oinn
 */
public class SoaplabProcessor extends Processor implements java.io.Serializable {

    private URL endpoint = null;
    
    /**
     * Construct a new processor with the given model and
     * name, delegates to the superclass.
     */
    public SoaplabProcessor(ScuflModel model, String name, String endpoint)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	// Set the endpoint, this then populates the ports appropriately
	// from the returned parameters of the soap call.
	try {
	    setEndpoint(endpoint);
	}
	catch (MalformedURLException mue) {
	    throw new ProcessorCreationException("The supplied endpoint url was malformed, endpoint was specified as '"+endpoint+"'");
	}
    }

    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	props.put("Soaplab URL",getEndpoint().toString());
	return props;
    }

    /**
     * Set the endpoint for this soaplab processor
     */
    void setEndpoint(String specifier)
	throws MalformedURLException,
	       ProcessorCreationException {
	URL new_endpoint = new URL(specifier);
	if (this.endpoint!=null) {
	    if (this.endpoint.equals(new_endpoint)==false) {
		fireModelEvent(new ScuflModelEvent(this, "Service endpoint changed to '"+specifier+"'"));
	    }
	    else {
		// Do nothing if the endpoint was the same as before
		return;
	    }
	}
	else {
	    fireModelEvent(new ScuflModelEvent(this, "Service endpoint set to '"+specifier+"'"));
	}
	this.endpoint = new_endpoint;
	try {
	    generatePorts();
	    getDescriptionText();
	}
	catch (PortCreationException pce) {
	    throw new ProcessorCreationException("Exception when trying to create ports from Soaplab endpoint : "+pce.getMessage());
	}
	catch (DuplicatePortNameException dpne) {
	    throw new ProcessorCreationException("Exception when trying to create ports from Soaplab endpoint : "+dpne.getMessage());
	}
    }

    /**
     * Use the endpoint data to set the description field
     */
    public void getDescriptionText() 
	throws ProcessorCreationException {
	try {
	    Call call = (Call)new Service().createCall();
	    call.setTargetEndpointAddress(this.endpoint.toString());
	    call.setOperationName(new QName("getAnalysisType"));
	    Map info = (Map)call.invoke(new Object[0]);
	    // Get the description element from the map
	    String description = (String)info.get("description");
	    if (description != null) {
		setDescription(description);
	    }
	}
	catch (javax.xml.rpc.ServiceException se) {
	    throw new ProcessorCreationException("Unable to create a new call to connect to soaplab, error was : "+se.getMessage());
	}
	catch (java.rmi.RemoteException re) {
	    throw new ProcessorCreationException("Unable to call the get description method : "+re.getMessage());
	}
    }

    /**
     * Use the endpoint data to create new ports and attach them to 
     * the processor. Interogates Soaplab for names of input and
     * output parameters, and additionally for their syntactic types,
     * as we might as well keep that information while we have it.
     */
    public void generatePorts()
	throws ProcessorCreationException,
	       PortCreationException,
	       DuplicatePortNameException {
	// Wipe the existing port declarations
	ports = new ArrayList();
	try {

	    // Do web service type stuff[tm]
	    Call call = (Call)new Service().createCall();
	    call.setTargetEndpointAddress(this.endpoint.toString());
	    
	    // Get inputs
	    call.setOperationName(new QName("getInputSpec"));
	    Map inputs[] = (Map[])call.invoke(new Object[0]);
	    // Iterate over the inputs
	    for (int i = 0; i<inputs.length; i++) {
		Map input_spec = inputs[i];
		String input_name = (String)input_spec.get("name");
		String input_type = ((String)(input_spec.get("type"))).toLowerCase();
		// Could get other properties such as defaults here
		// but at the moment we've got nowhere to put them
		// so we don't bother.
		Port new_port = new InputPort(this, input_name);
		if (input_type.equals("string")) {
		    new_port.setSyntacticType("'text/plain'");
		}
		else if (input_type.equals("string[]")) {
		    new_port.setSyntacticType("l('text/plain')");
		}
		else if (input_type.equals("byte[]")) {
		    new_port.setSyntacticType("'application/octet-stream'");
		}
		else if (input_type.equals("byte[][]")) {
		    new_port.setSyntacticType("l('application/octet-stream')");
		}
		this.addPort(new_port);
	    }
	    
	    // Get outputs
	    call = (Call)new Service().createCall();
	    call.setTargetEndpointAddress(this.endpoint.toString());
	    call.setOperationName(new QName("getResultSpec"));
	    Map[] results = (Map[])call.invoke(new Object[0]);
	    // Iterate over the outputs
	    for (int i = 0; i<results.length; i++) {
		Map output_spec = results[i];
		String output_name = (String)output_spec.get("name");
		String output_type = ((String)(output_spec.get("type"))).toLowerCase();
		// Check to see whether the output is either report or detailed_status, in 
		// which cases we ignore it, this is soaplab metadata rather than application
		// data.
		if ((!output_name.equalsIgnoreCase("detailed_status")) 
		    && (!output_name.equalsIgnoreCase("report"))) {
		    Port new_port = new OutputPort(this, output_name);
		    if (output_type.equals("string")) {
			new_port.setSyntacticType("'text/plain'");
		    }
		    else if (output_type.equals("string[]")) {
			new_port.setSyntacticType("l('text/plain')");
		    }
		    else if (output_type.equals("byte[]")) {
			new_port.setSyntacticType("'application/octet-stream'");
		    }
		    else if (output_type.equals("byte[][]")) {
			new_port.setSyntacticType("l('application/octet-stream')");
		    }
		    this.addPort(new_port);
		}
	    }

	}
	catch (javax.xml.rpc.ServiceException se) {
	    throw new ProcessorCreationException("Unable to create a new call to connect to soaplab, error was : "+se.getMessage());
	}
	catch (java.rmi.RemoteException re) {
	    throw new ProcessorCreationException("Unable to call the get spec method : "+re.getMessage());
	}
	catch (NullPointerException npe) {
	    // If we had a null pointer exception, go around again - this is a bug somewhere between axis and soaplab
	    // that occasionally causes NPEs to happen in the first call or two to a given soaplab installation. It also
	    // manifests in the Talisman soaplab clients.
	    generatePorts();
	}
    }
    
    /**
     * Get the URL for this endpoint
     */
    public URL getEndpoint() {
	return this.endpoint;
    }


}
