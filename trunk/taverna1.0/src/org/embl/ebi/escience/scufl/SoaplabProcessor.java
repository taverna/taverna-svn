/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Iterator;

import org.apache.axis.client.*;
import javax.xml.namespace.QName;
import org.apache.axis.AxisFault;

/**
 * A processor based on a Soaplab service. Currently
 * not fully implemented, but potentially this could
 * use the describe() call in Soaplab to build its
 * own ports and suchlike. This is the next thing to
 * do.
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
	    System.out.println("About to generate ports");
	    generatePorts();
	}
	catch (PortCreationException pce) {
	    throw new ProcessorCreationException("Exception when trying to create ports from Soaplab endpoint : "+pce.getMessage());
	}
	catch (DuplicatePortNameException dpne) {
	    throw new ProcessorCreationException("Exception when trying to create ports from Soaplab endpoint : "+dpne.getMessage());
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
		String input_type = (String)input_spec.get("type");
		// Could get other properties such as defaults here
		// but at the moment we've got nowhere to put them
		// so we don't bother.
		Port new_port = new InputPort(this, input_name);
		new_port.setSyntacticType(input_type);
		this.addPort(new_port);
	    }
	    
	    // Get outputs
	    call = (Call)new Service().createCall();
	    call.setTargetEndpointAddress(this.endpoint.toString());
	    call.setOperationName(new QName("getResultSpec"));
	    Map results = (Map)call.invoke(new Object[0]);
	    // Iterate over the outputs
	    for (Iterator i = results.keySet().iterator(); i.hasNext(); ) {
		String output_name = (String)i.next();
		String output_type = (String)results.get(output_name);
		Port new_port = new OutputPort(this, output_name);
		new_port.setSyntacticType(output_type);
		this.addPort(new_port);
	    }

	}
	catch (javax.xml.rpc.ServiceException se) {
	    throw new ProcessorCreationException("Unable to create a new call to connect to soaplab, error was : "+se.getMessage());
	}
	catch (java.rmi.RemoteException re) {
	    throw new ProcessorCreationException("Unable to call the get spec method : "+re.getMessage());
	}
    }
    
    /**
     * Get the URL for this endpoint
     */
    public URL getEndpoint() {
	return this.endpoint;
    }


}
