/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Processor corresponding to a single instance of an SGS based streaming
 * service.
 * 
 * @author Tom Oinn
 */
public class InfernoProcessor extends Processor {

	String host, service;

	int port;

	public String getHost() {
		return this.host;
	}

	public String getService() {
		return this.service;
	}

	public int getPort() {
		return this.port;
	}

	public String getResourceHost() {
		return this.host;
	}

	public InfernoProcessor(ScuflModel theModel, String processorName,
			String host, int port, String service)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(theModel, processorName);
		this.host = host;
		this.port = port;
		this.service = service;

		try {

			// Create inputs
			InputPort inputRef = new InputPort(this, "refIn");
			inputRef.setSyntacticType("'text/plain'");
			addPort(inputRef);

			InputPort inputString = new InputPort(this, "stringIn");
			inputString.setSyntacticType("'text/plain'");
			addPort(inputString);

			InputPort inputBinary = new InputPort(this, "binaryIn");
			inputBinary.setSyntacticType("'application/octet-stream'");
			addPort(inputBinary);

			InputPort params = new InputPort(this, "params");
			params.setSyntacticType("'text/plain'");
			addPort(params);

			// Create outputs
			OutputPort baseURL = new OutputPort(this, "baseURL");
			baseURL.setSyntacticType("'text/plain'");
			addPort(baseURL);

			OutputPort outputRef = new OutputPort(this, "refStdOut");
			outputRef.setSyntacticType("'text/plain'");
			addPort(outputRef);

			OutputPort errorRef = new OutputPort(this, "refStdErr");
			errorRef.setSyntacticType("'text/plain'");
			addPort(errorRef);

			OutputPort outputString = new OutputPort(this, "stringOut");
			outputString.setSyntacticType("'text/plain'");
			addPort(outputString);

			OutputPort outputBinary = new OutputPort(this, "binaryOut");
			outputBinary.setSyntacticType("'application/octet-stream'");
			addPort(outputBinary);

		} catch (ScuflException ex) {
			// should never happen, hardcoded ports
			throw new ProcessorCreationException(
					"Unable to create inferno processor", ex);
		}
	}

	public Properties getProperties() {
		return new Properties();
	}

}
