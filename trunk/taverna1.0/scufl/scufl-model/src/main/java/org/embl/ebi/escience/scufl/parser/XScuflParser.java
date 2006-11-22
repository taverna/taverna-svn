/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Reads a definition in XScufl format and populates the supplied model with it.
 * 
 * @author Tom Oinn
 */
public class XScuflParser {
	
	private static Logger logger = Logger.getLogger(XScuflParser.class);
	
	/**
	 * Read from the given String containing an XScufl document and populate the
	 * given ScuflModel with data from the definition. You can optionally
	 * specify a name prefix that will be used for all new processors created,
	 * this might be useful if you want to import more than one data file into
	 * the same model. If the prefix is null, none will be applied. The prefix
	 * should not contain characters other than alphanumeric ones, and will have
	 * a single underscore appended to it.
	 * 
	 * @exception UnknownProcessorException
	 *                if a data constraint refers to a processor that isn't
	 *                defined in the input
	 * @exception UnknownPortException
	 *                if a data constraint refers to a port that isn't defined
	 *                in the input
	 * @exception ProcessorCreationException
	 *                if there is a general creation failure in a processor,
	 *                i.e. when attempting to contact soaplab to get its inputs
	 *                and outputs
	 * @exception DataConstraintCreationException
	 *                if some internal error prevents a data constraint being
	 *                built
	 * @exception DuplicateProcessorNameException
	 *                if a processor is defined in the input with a name that
	 *                already exists in the model.
	 * @exception MalformedNameException
	 *                if a data constraint is not specified in the correct
	 *                format of [PROCESSOR]:[PORT]
	 * @exception XScuflFormatException
	 *                if the format of the input is not valid XScufl, or not
	 *                valid XML.
	 */
	public static void populate(String input, ScuflModel model, String prefix)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(new StringReader(input));
			populate(document, model, prefix);
		} catch (JDOMException jde) {
			throw new XScuflFormatException(
					"Unable to load XScufl file, error : " + jde.getMessage());
		} catch (IOException ioe) {
			throw new XScuflFormatException(
					"Unable to load XScufl file, error : " + ioe.getMessage());
		}
	}

	/**
	 * Read from the given InputStream and populate the given ScuflModel with
	 * data from the definition. You can optionally specify a name prefix that
	 * will be used for all new processors created, this might be useful if you
	 * want to import more than one data file into the same model. If the prefix
	 * is null, none will be applied. The prefix should not contain characters
	 * other than alphanumeric ones, and will have a single underscore appended
	 * to it.
	 * 
	 * @exception UnknownProcessorException
	 *                if a data constraint refers to a processor that isn't
	 *                defined in the input
	 * @exception UnknownPortException
	 *                if a data constraint refers to a port that isn't defined
	 *                in the input
	 * @exception ProcessorCreationException
	 *                if there is a general creation failure in a processor,
	 *                i.e. when attempting to contact soaplab to get its inputs
	 *                and outputs
	 * @exception DataConstraintCreationException
	 *                if some internal error prevents a data constraint being
	 *                built
	 * @exception DuplicateProcessorNameException
	 *                if a processor is defined in the input with a name that
	 *                already exists in the model.
	 * @exception MalformedNameException
	 *                if a data constraint is not specified in the correct
	 *                format of [PROCESSOR]:[PORT]
	 * @exception XScuflFormatException
	 *                if the format of the input is not valid XScufl, or not
	 *                valid XML.
	 */
	public static void populate(InputStream is, ScuflModel model, String prefix)
			throws UnknownProcessorException, UnknownPortException,
			ProcessorCreationException, DataConstraintCreationException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {

		// Load the data into a JDom Document
		InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
		SAXBuilder builder = new SAXBuilder(false);
		Document document = null;
		try {
			document = builder.build(isr);
		} catch (JDOMException jde) {
			throw new XScuflFormatException(
					"Unable to load XScufl file, error : " + jde.getMessage());
		} catch (IOException ioe) {
			throw new XScuflFormatException(
					"Unable to load XScufl file, error : " + ioe.getMessage());
		}
		populate(document, model, prefix);

	}

	/**
	 * Read from the given JDOM Document and populate the given ScuflModel with
	 * data from the definition. You can optionally specify a name prefix that
	 * will be used for all new processors created, this might be useful if you
	 * want to import more than one data file into the same model. If the prefix
	 * is null, none will be applied. The prefix should not contain characters
	 * other than alphanumeric ones, and will have a single underscore appended
	 * to it.
	 * 
	 * @exception UnknownProcessorException
	 *                if a data constraint refers to a processor that isn't
	 *                defined in the input
	 * @exception UnknownPortException
	 *                if a data constraint refers to a port that isn't defined
	 *                in the input
	 * @exception ProcessorCreationException
	 *                if there is a general creation failure in a processor,
	 *                i.e. when attempting to contact soaplab to get its inputs
	 *                and outputs
	 * @exception DataConstraintCreationException
	 *                if some internal error prevents a data constraint being
	 *                built
	 * @exception DuplicateProcessorNameException
	 *                if a processor is defined in the input with a name that
	 *                already exists in the model.
	 * @exception MalformedNameException
	 *                if a data constraint is not specified in the correct
	 *                format of [PROCESSOR]:[PORT]
	 * @exception XScuflFormatException
	 *                if the format of the input is not valid XScufl, or not
	 *                valid XML.
	 */
	public static void populate(Document document, ScuflModel model,
			String prefix) throws UnknownProcessorException,
			UnknownPortException, ProcessorCreationException,
			DataConstraintCreationException, DuplicateProcessorNameException,
			MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		// Disable model events for the duration of the load
		model.setEventStatus(false);

		// Check whether we're using prefixes
		boolean usePrefix = false;
		if (prefix != null) {
			usePrefix = true;
		}
		Element root = document.getRootElement();
		if (root.getName().equals("scufl") == false) {
			throw new XScuflFormatException("Doesn't appear to be a workflow!");
		}

		if (root.getAttributeValue("log") != null) {
			model.setLogLevel(Integer.parseInt(root.getAttributeValue("log")));
		}

		Namespace namespace = root.getNamespace();

		// Load the workflow description
		Element descriptionElement = root.getChild("workflowdescription",
				namespace);
		if (descriptionElement != null) {
			model.setDescription(WorkflowDescription.build(descriptionElement));
		} else {
			model.setDescription(new WorkflowDescription());
		}

		// Build processors
		// All processors are nodes of form <processor name="foo"> ....
		// </processor>
		List processors = root.getChildren("processor", namespace);
		logger.debug("Found "+processors.size()+" processor nodes.");
		ExceptionHolder holder = new ExceptionHolder();
		ArrayList<ProcessorLoaderThread> threadList = new ArrayList<ProcessorLoaderThread>();
		for (Iterator i = processors.iterator(); i.hasNext();) {
			Element processorNode = (Element) i.next();
			String name = processorNode.getAttributeValue("name");
			if (usePrefix) {
				name = prefix + "_" + name;
			}
			String workerName = processorNode.getAttributeValue("workers");
			threadList.add(new ProcessorLoaderThread(model, processorNode,
					name, namespace, holder, workerName));
			// End iterator over processors
		}
		// Wait on all the threads in the threadList
		for (ProcessorLoaderThread t : threadList) {			
			logger.debug("Joining with thread " + t);
			try {
				t.join();
			} catch (InterruptedException ie) {
				//				
			}
			logger.debug("Joined all threads");
		}
		// Hack hack hack, since the threads themselves can't throw exceptions,
		// they catch any of the following three exception types and put them into
		// the exception holder. When all the threads have exited we check the
		// holder and throw back any of the exceptions it contains in the main thread.
		// Seems to work okay and is a big optimisation at load time.
		if (! holder.exceptionList.isEmpty()) {
			// Re-enable events before exiting the method.
			model.setEventStatus(true);
			// Build the message
			StringBuffer message = new StringBuffer();
			for (Iterator i = holder.exceptionList.iterator(); i.hasNext();) {
				message.append(((Exception) i.next()).getMessage());
				if (i.hasNext()) {
					message.append("\n");
				}
			}
			// message.append("\n\nTo load this workflow try setting offline
			// mode, this will ");
			// message.append("allow you to load and remove any defunct
			// operations.");
			throw new ProcessorCreationException(message.toString());
			/**
			 * if (holder.theException instanceof ProcessorCreationException) {
			 * throw (ProcessorCreationException)(holder.theException); } else
			 * if (holder.theException instanceof XScuflFormatException) { throw
			 * (XScuflFormatException)(holder.theException); } else if
			 * (holder.theException instanceof DuplicateProcessorNameException) {
			 * throw (DuplicateProcessorNameException)(holder.theException); }
			 */
		}
		try {
			// Iterate over the external declarations and create appropriate
			// input and output
			// ports in the internal source and sink processors.
			List sourceList = root.getChildren("source", namespace);
			Processor sourceHolder = model.getWorkflowSourceProcessor();
			for (Iterator i = sourceList.iterator(); i.hasNext();) {
				Element sourceElement = (Element) i.next();
				String portName = sourceElement.getAttributeValue("name");
				if (portName == null) {
					portName = sourceElement.getTextTrim();
				}
				try {
					if (usePrefix) {
						portName = prefix + "_" + portName;
					}
					OutputPort sourcePort = new OutputPort(sourceHolder,
							portName);
					Element configurationElement = sourceElement.getChild(
							"metadata", namespace);
					if (configurationElement != null) {
						sourcePort.getMetadata().configureFromElement(
								configurationElement);
					}
					sourceHolder.addPort(sourcePort);
				} catch (DuplicatePortNameException dpne) {
					throw new XScuflFormatException(
							"You have a duplicate source port in your definition file, aborting.");
				} catch (PortCreationException pce) {
					throw new XScuflFormatException(
							"Unable to create source port.");
				}
			}
			List sinkList = root.getChildren("sink", namespace);
			Processor sinkHolder = model.getWorkflowSinkProcessor();
			for (Iterator i = sinkList.iterator(); i.hasNext();) {
				Element sinkElement = (Element) i.next();
				String portName = sinkElement.getAttributeValue("name");
				String mergeModeString = sinkElement.getAttributeValue("mode");
				int mergeMode = InputPort.NDSELECT;
				if (mergeModeString != null) {
					if (mergeModeString.equals("merge")) {
						mergeMode = InputPort.MERGE;
					}
				}
				if (portName == null) {
					portName = sinkElement.getTextTrim();
				}
				try {
					if (usePrefix) {
						portName = prefix + "_" + portName;
					}
					InputPort sinkPort = new InputPort(sinkHolder, portName);
					sinkPort.setMergeMode(mergeMode);
					Element configurationElement = sinkElement.getChild(
							"metadata", namespace);
					if (configurationElement != null) {
						sinkPort.getMetadata().configureFromElement(
								configurationElement);
					}
					sinkHolder.addPort(sinkPort);
				} catch (DuplicatePortNameException dpne) {
					throw new XScuflFormatException(
							"You have a duplicate sink port in your defintion file, aborting.");
				} catch (PortCreationException pce) {
					throw new XScuflFormatException(
							"Unable to create sink port.");
				}
			}

			// Build data constraints
			List dataConstraintList = root.getChildren("link", namespace);
			for (Iterator i = dataConstraintList.iterator(); i.hasNext();) {
				Element linkElement = (Element) i.next();
				String sourcePortName = null;
				String sinkPortName = null;
				try {
					Element inputElement = linkElement.getChild("input",
							namespace);
					Element outputElement = linkElement.getChild("output",
							namespace);
					if (inputElement == null) {
						throw new XScuflFormatException(
								"A data constraint must have an input child element");
					}
					if (outputElement == null) {
						throw new XScuflFormatException(
								"A data constraint must have an output child element");
					}
					sinkPortName = inputElement.getTextTrim();
					sourcePortName = outputElement.getTextTrim();
				} catch (XScuflFormatException xfe) {
					sourcePortName = linkElement.getAttributeValue("source");
					sinkPortName = linkElement.getAttributeValue("sink");
					if (sourcePortName == null || sinkPortName == null) {
						throw new XScuflFormatException(
								"Neither nested elements nor attributes found defining the link, aborting parse.");
					}
				}
				if (usePrefix) {
					sinkPortName = prefix + "_" + sinkPortName;
					sourcePortName = prefix + "_" + sourcePortName;
				}
				model.addDataConstraint(new DataConstraint(model,
						sourcePortName, sinkPortName));

				// End iterator over data constraints
			}

			// Build concurrency constraints
			List concurrencyConstraints = root.getChildren("coordination",
					namespace);
			for (Iterator i = concurrencyConstraints.iterator(); i.hasNext();) {
				Element coordination = (Element) i.next();
				String constraintName = coordination.getAttributeValue("name");
				if (usePrefix) {
					constraintName = prefix + "_" + constraintName;
				}
				Element condition = coordination.getChild("condition",
						namespace);
				Element action = coordination.getChild("action", namespace);
				String controllerName = condition.getChild("target", namespace)
						.getTextTrim();
				if (usePrefix) {
					controllerName = prefix + "_" + controllerName;
				}
				Processor controller = model.locateProcessor(controllerName);
				int controllerStateGuard = ConcurrencyConstraint
						.statusStringToInt(condition.getChild("state",
								namespace).getTextTrim());
				String targetName = action.getChild("target", namespace)
						.getTextTrim();
				if (usePrefix) {
					targetName = prefix + "_" + targetName;
				}
				Processor target = model.locateProcessor(targetName);
				int targetStateTo = ConcurrencyConstraint
						.statusStringToInt(action.getChild("statechange",
								namespace).getChild("to", namespace)
								.getTextTrim());
				int targetStateFrom = ConcurrencyConstraint
						.statusStringToInt(action.getChild("statechange",
								namespace).getChild("from", namespace)
								.getTextTrim());
				model.addConcurrencyConstraint(new ConcurrencyConstraint(model,
						constraintName, controller, target, targetStateFrom,
						targetStateTo, controllerStateGuard));
			}

		} catch (XScuflFormatException xfe) {
			model.setEventStatus(true);
			throw xfe;
		}
		model.setEventStatus(true);
	}

}

/**
 * A thread subclass to load a processor without blocking everything
 */
class ProcessorLoaderThread extends Thread {
	private ScuflModel model;

	private Element processorNode;

	private String name;

	private Namespace namespace;

	private ExceptionHolder holder;

	String workerThreads;

	protected ProcessorLoaderThread(ScuflModel model, Element processorNode,
			String name, Namespace namespace, ExceptionHolder holder,
			String workersString) {
		this.model = model;
		this.namespace = namespace;
		this.processorNode = processorNode;
		this.name = name;
		this.holder = holder;
		this.workerThreads = workersString;
		this.start();
	}

	public void run() {
		try {

			String logLevel = processorNode.getAttributeValue("log");
			// Default value for a processor is -1, which translates to
			// 'inherit from model'.
			int log = -1;
			if (logLevel != null) {
				log = Integer.parseInt(logLevel);
			}
			Processor theProcessor = ProcessorHelper
					.loadProcessorFromXML(processorNode, model, name);
			if (theProcessor == null) {
				throw new XScuflFormatException(
						"Couldn't find a known specification mechanism"
								+ " for processor node '" + name + "'");
			}
			theProcessor.setLogLevel(log);
			String boring = processorNode.getAttributeValue("boring");
			if (boring != null) {
				theProcessor.setBoring(true);
			}
			// Set number of worker threads if defined.
			if (workerThreads != null) {
				int workers = Integer.parseInt(workerThreads);
				theProcessor.setWorkers(workers);
			}

			// Get the description if present
			String description = "";
			Element de = processorNode.getChild("description", namespace);
			if (de != null) {
				description = de.getTextTrim();
				theProcessor.setDescription(description);
			}
			for (Iterator i = processorNode.getChildren("mergemode", namespace)
					.iterator(); i.hasNext();) {
				Element inputBehaviourElement = (Element) i.next();
				String inputName = inputBehaviourElement
						.getAttributeValue("input");
				String inputMode = inputBehaviourElement
						.getAttributeValue("mode");
				InputPort[] ports = theProcessor.getInputPorts();
				for (int j = 0; j < ports.length; j++) {
					if (ports[j].getName().equals(inputName)) {
						if (inputMode.equals("merge")) {
							ports[j].setMergeMode(InputPort.MERGE);
						}
					}
				}
			}
			// Get the default set if present
			Element ds = processorNode.getChild("defaults", namespace);
			if (ds != null) {
				for (Iterator i = ds.getChildren().iterator(); i.hasNext();) {
					Element defaultElement = (Element) i.next();
					String portName = defaultElement.getAttributeValue("name");
					String value = defaultElement.getTextTrim();
					try {
						InputPort ip = (InputPort) theProcessor.locatePort(
								portName, true);
						ip.setDefaultValue(value);
					} catch (UnknownPortException upe) {
						throw new ProcessorCreationException(
								"Cannot find port '" + portName
										+ "' to set the default.");
					}
				}
			}
			// Get the iteration strategy if present
			Element iterationStrategyElement = processorNode.getChild(
					"iterationstrategy", namespace);
			if (iterationStrategyElement != null) {
				theProcessor.setIterationStrategy(new IterationStrategy(
						iterationStrategyElement));
			}
			model.addProcessor(theProcessor);
		} catch (XScuflFormatException xfe) {
			holder.addException(xfe);
		} catch (ProcessorCreationException pce) {
			holder.addException(pce);
		} catch (DuplicateProcessorNameException dpne) {
			holder.addException(dpne);
		}
	}

}

/**
 * Used to carry an exception between the worker threads loading the processors
 * and the main thread that actually cares
 */
class ExceptionHolder {
	public List exceptionList = new ArrayList();

	public synchronized void addException(Exception ex) {
		exceptionList.add(ex);
	}
	// public Exception theException = null;
}
