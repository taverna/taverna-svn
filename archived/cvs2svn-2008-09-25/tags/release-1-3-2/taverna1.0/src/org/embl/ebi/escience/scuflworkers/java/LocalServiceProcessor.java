/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.*;
import org.jdom.Element;

/**
 * A processor that runs the quick Java plugins defined by the LocalService
 * interface in this package.
 * 
 * @author Tom Oinn
 */
public class LocalServiceProcessor extends Processor {

	private String workerClassName;

	private static Logger logger = Logger.getLogger(LocalServiceProcessor.class);

	private LocalWorker theImplementation;

	public String getWorkerClassName() {
		return this.workerClassName;
	}

	protected LocalWorker getWorker() {
		return this.theImplementation;
	}

	public int getMaximumWorkers() {
		return 5;
	}

	public LocalServiceProcessor(ScuflModel model, String name, LocalWorker worker) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, name);
		this.workerClassName = worker.getClass().getName();
		this.theImplementation = worker;
		initialise(model, name);
	}

	public LocalServiceProcessor(ScuflModel model, String name, String workerClassName, Element additionalXML)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);
		this.workerClassName = workerClassName;
		try {
			// Get the instance of the worker
			Class theClass = Class.forName(workerClassName);
			theImplementation = (LocalWorker) theClass.newInstance();

		} catch (Exception e) {
			ProcessorCreationException pce = new ProcessorCreationException(
					"Unable to instantiate processor for local service class " + workerClassName);
			e.printStackTrace();
			pce.initCause(e);
			throw pce;
		}
		if (this.theImplementation instanceof XMLExtensible) {
			((XMLExtensible) this.theImplementation).consumeXML(additionalXML);
		} else {
			logger.warn(workerClassName + " has been provided with additional XML but is not XMLExtensible");
		}
		initialise(model, name);
	}

	public LocalServiceProcessor(ScuflModel model, String name, String workerClassName)
			throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);
		this.workerClassName = workerClassName;
		try {
			// Get the instance of the worker
			Class theClass = Class.forName(workerClassName);
			theImplementation = (LocalWorker) theClass.newInstance();

		} catch (Exception e) {
			ProcessorCreationException pce = new ProcessorCreationException(
					"Unable to instantiate processor for local service class " + workerClassName);
			e.printStackTrace();
			pce.initCause(e);
			throw pce;
		}
		initialise(model, name);
	}

	protected void initialise(ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		try {
			for (int i = 0; i < theImplementation.inputNames().length; i++) {
				// Create input ports
				Port p = new InputPort(this, theImplementation.inputNames()[i]);
				p.setSyntacticType(theImplementation.inputTypes()[i]);
				addPort(p);
			}
			for (int i = 0; i < theImplementation.outputNames().length; i++) {
				// Create output ports
				Port p = new OutputPort(this, theImplementation.outputNames()[i]);
				p.setSyntacticType(theImplementation.outputTypes()[i]);
				SemanticMarkup m = p.getMetadata();
				String[] mimeTypes = ((theImplementation.outputTypes()[i].split("\\'"))[1]).split(",");
				for (int j = 0; j < mimeTypes.length; j++) {
					System.out.println(mimeTypes[j]);
					m.addMIMEType(mimeTypes[j]);
				}
				addPort(p);
			}
		} catch (DuplicatePortNameException dpne) {
			throw new ProcessorCreationException("The supplied specification for the local service processor '" + name
					+ "' contained a duplicate port '" + dpne.getMessage() + "'");
		} catch (PortCreationException pce) {
			throw new ProcessorCreationException(
					"An error occured whilst generating ports for the local service processor " + pce.getMessage());
		}

	}

	/**
	 * Get the properties for this processor
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		props.put("WorkerClass", this.workerClassName);
		return props;
	}

}
