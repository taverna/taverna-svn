/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor;

/**
 * Processor for the API consumer worker
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class APIConsumerProcessor extends DependencyProcessor {

	private static Logger logger = Logger.getLogger(APIConsumerProcessor.class);

	APIConsumerDefinition definition;

	public APIConsumerProcessor(ScuflModel model, String name,
		APIConsumerDefinition definition) throws ProcessorCreationException,
		DuplicateProcessorNameException {
		super(model, name);
		this.definition = definition;
		setClassLoaderSharing(ClassLoaderSharing.workflow);
		setDescription(definition.description);

		// If not static create an input for the subject to
		// be operated on.
		if (!definition.isStatic) {
			if (!definition.isConstructor) {
				try {
					InputPort subjectInput = new InputPort(this, "object");
					subjectInput.setSyntacticType(definition.getTavernaObjectTypeString());
					addPort(subjectInput);
				} catch (DuplicatePortNameException e) {
					logger.warn("Could not add duplicate port 'object'", e);
				} catch (PortCreationException e) {
					logger.warn("Could not create port 'object'", e);
				}
			}
			
			try {
				OutputPort subjectOutput = new OutputPort(this, "object");
				subjectOutput.setSyntacticType(definition.getTavernaObjectTypeString());
				addPort(subjectOutput);
			} catch (DuplicatePortNameException e) {
				logger.warn("Could not add duplicate port 'object'", e);
			} catch (PortCreationException e) {
				logger.warn("Could not create port 'object'", e);
			}
		}

		// Add a return value port for non void operations
		if (!definition.tName.equals("void") && !definition.isConstructor) {
			// Use the 'object' port for constructors rather than the 'result'
			// one (created above)
			try {
				OutputPort resultPort = new OutputPort(this, "result");
				resultPort.setSyntacticType(definition.getTavernaOutputTypeString());
				addPort(resultPort);
			} catch (DuplicatePortNameException e) {
				logger.warn("Could not add duplicate port 'result'", e);
			} catch (PortCreationException e) {
				logger.warn("Could not create port 'result'", e);
			}
		}

		// Add input ports for parameters
		String[] tavernaOutputTypes = definition.getTavernaTypeStrings();
		for (int i = 0; i < definition.pNames.length; i++) {
			String portName = definition.pNames[i];
			// Create inputs...
			try {
				InputPort pPort = new InputPort(this, portName);
				pPort.setSyntacticType(tavernaOutputTypes[i]);
				addPort(pPort);
			} catch (DuplicatePortNameException e) {
				logger.warn("Could not add duplicate port '" + portName + "'",
					e);
			} catch (PortCreationException e) {
				logger.warn("Could not create port '" + portName + "'", e);
			}
		}
	}

	public Properties getProperties() {
		return new Properties();
	}

}
