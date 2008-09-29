/*
 * CVS
 * $Author: iandunlop $
 * $Date: 2008-02-12 15:08:57 $
 * $Revision: 1.2 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.util.Iterator;

import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellPortTypes.SymanticTypes;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;

/**
 * Handle XML store and load for the Rserv processor
 * 
 * @author Stian Soiland, Ingo Wassink
 */
public class RshellXMLHandler implements XMLHandler {
	private static final String PROCESSOR_TAG = "processor";

	private static final String RSHELL_TAG = "rshell";

	private static final String INPUT_PORT_LIST_TAG = "rshellInputPortList";

	private static final String INPUT_PORT_TAG = "rshellInputPort";

	private static final String OUTPUT_PORT_LIST_TAG = "rshellOutputPortList";

	private static final String OUTPUT_PORT_TAG = "rshellOutputPort";

	private static final String SYMANTIC_TYPE_ATTRIBUTE = "symanticType";

	private static final String SYNTACTIC_TYPE_ATTRIBUTE = "syntacticType";

	private static final String HOSTNAME_ATTRIBUTE = "hostname";

	private static final String PORT_ATTRIBUTE = "port";

	private static final String USERNAME_ATTRIBUTE = "username";

	private static final String PASSWORD_ATTRIBUTE = "password";

	private static final String KEEP_SESSION_ALIVE_ATTRIBUTE = "keepSessionAlive";

	/**
	 * Method for getting the XML element of the prototype processor
	 * 
	 * @param processorFactory
	 *            of the processor
	 * @return the XML element
	 */
	public Element elementForFactory(ProcessorFactory processorFactory) {
		RshellProcessorFactory rshellProcessorFactory = (RshellProcessorFactory) processorFactory;

		RshellProcessor prototype = rshellProcessorFactory.getPrototype();
		Element processorElement = (prototype != null) ? elementForProcessor(prototype)
				: new Element(RSHELL_TAG, XScufl.XScuflNS);

		return processorElement;
	}

	/**
	 * Method for getting a factory for an Element specification of a processor
	 * 
	 * @param processElement
	 *            the XML element defining the prototype
	 * @return the processor factory
	 */
	public ProcessorFactory getFactory(Element processElement) {
		// make a clone of the element
		Element clone = (Element) processElement.clone();
		clone.detach(); // remove parents

		// add parent node "processor" to the clone, because the method
		// loadProcessorFromXML expects a processor element
		Element processorElement = new Element(PROCESSOR_TAG, XScufl.XScuflNS);
		processorElement.addContent(clone);

		// try to get the prototype
		RshellProcessor prototype = null;
		try {
			prototype = (RshellProcessor) loadProcessorFromXML(processorElement, null,
					PROCESSOR_TAG);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		RshellProcessorFactory processorFactory = (prototype != null) ? new RshellProcessorFactory(
				prototype)
				: new RshellProcessorFactory();

		return processorFactory;
	}

	/**
	 * Method for getting the xml element for the processor
	 * 
	 * @param processor
	 *            the processor to export to an xml element
	 * @return the XML rshell element
	 */
	public Element elementForProcessor(Processor processor) {
		RshellProcessor rshellProcessor = (RshellProcessor) processor;
		Element rshellElement = new Element(RSHELL_TAG, XScufl.XScuflNS);

		// set the script contents
		rshellElement.setText(rshellProcessor.getScript());
		// set connection elements
		RshellConnectionSettings connectionSettings = rshellProcessor
				.getConnectionSettings();
		rshellElement.setAttribute(HOSTNAME_ATTRIBUTE, connectionSettings
				.getHost(), XScufl.XScuflNS);
		rshellElement.setAttribute(PORT_ATTRIBUTE, Integer
				.toString(connectionSettings.getPort()), XScufl.XScuflNS);
		rshellElement.setAttribute(USERNAME_ATTRIBUTE, connectionSettings
				.getUsername(), XScufl.XScuflNS);
		rshellElement.setAttribute(PASSWORD_ATTRIBUTE, connectionSettings
				.getPassword(), XScufl.XScuflNS);
		rshellElement.setAttribute(KEEP_SESSION_ALIVE_ATTRIBUTE, Boolean
				.toString(connectionSettings.isKeepSessionAlive()),
				XScufl.XScuflNS);

		// Input list
		Element inputList = new Element(INPUT_PORT_LIST_TAG, XScufl.XScuflNS);
		InputPort[] inputs = rshellProcessor.getInputPorts();
		for (int i = 0; i < inputs.length; i++) {
			RshellInputPort input = (RshellInputPort) inputs[i];
			Element inputElement = new Element(INPUT_PORT_TAG, XScufl.XScuflNS);
			inputElement.setText(input.getName());
			inputElement.setAttribute(SYNTACTIC_TYPE_ATTRIBUTE, input
					.getSyntacticType(), XScufl.XScuflNS);
			inputElement.setAttribute(SYMANTIC_TYPE_ATTRIBUTE, input
					.getSymanticType().toString(), XScufl.XScuflNS);
			inputList.addContent(inputElement);
		}
		rshellElement.addContent(inputList);

		Element outputList = new Element(OUTPUT_PORT_LIST_TAG, XScufl.XScuflNS);
		OutputPort[] outputs = rshellProcessor.getOutputPorts();
		for (int i = 0; i < outputs.length; i++) {
			RshellOutputPort output = (RshellOutputPort) outputs[i];
			Element outputElement = new Element(OUTPUT_PORT_TAG,
					XScufl.XScuflNS);
			outputElement.setText(output.getName());
			outputElement.setAttribute(SYNTACTIC_TYPE_ATTRIBUTE, output
					.getSyntacticType(), XScufl.XScuflNS);
			outputElement.setAttribute(SYMANTIC_TYPE_ATTRIBUTE, output
					.getSymanticType().toString(), XScufl.XScuflNS);
			outputList.addContent(outputElement);
		}
		rshellElement.addContent(outputList);

		return rshellElement;
	}

	/**
	 * Method for loading an xml processor from XML
	 * 
	 * @param processorElement
	 *            the XML processor element
	 * @param model
	 *            the scufl model where the process belongs to
	 * @param name
	 *            the name of the processor
	 * @return the processor element
	 */
	public Processor loadProcessorFromXML(Element processorElement,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {

		RshellProcessor rshellProcessor = new RshellProcessor(model, name);
		Element rshellElement = processorElement.getChild(RSHELL_TAG,
				XScufl.XScuflNS);

		String script = rshellElement.getTextTrim();
		rshellProcessor.setScript(script);

		// connection settings
		RshellConnectionSettings connectionSettings = rshellProcessor
				.getConnectionSettings();
		connectionSettings.setHost(rshellElement.getAttributeValue(
				HOSTNAME_ATTRIBUTE, XScufl.XScuflNS));
		connectionSettings.setPort(rshellElement.getAttributeValue(
				PORT_ATTRIBUTE, XScufl.XScuflNS));
		connectionSettings.setUsername(rshellElement.getAttributeValue(
				USERNAME_ATTRIBUTE, XScufl.XScuflNS));
		connectionSettings.setPassword(rshellElement.getAttributeValue(
				PASSWORD_ATTRIBUTE, XScufl.XScuflNS));
		connectionSettings.setKeepSessionAlive(rshellElement.getAttributeValue(
				KEEP_SESSION_ALIVE_ATTRIBUTE, XScufl.XScuflNS));

		// Handle inputs
		Element inputList = rshellElement.getChild(INPUT_PORT_LIST_TAG,
				XScufl.XScuflNS);
		if (inputList != null) {
			Iterator i = inputList.getChildren().iterator();
			while (i.hasNext()) {
				Element inputElement = (Element) i.next();
				String inputName = inputElement.getTextTrim();
				String symanticType = inputElement.getAttributeValue(
						SYMANTIC_TYPE_ATTRIBUTE, XScufl.XScuflNS);

				try {
					RshellInputPort inputPort = new RshellInputPort(
							rshellProcessor, inputName);
					if (symanticType != null) {
						try {
							inputPort.setSymanticType(SymanticTypes
									.valueOf(symanticType));
						} catch (IllegalArgumentException pce) {
							throw new ProcessorCreationException(
									"Unable to create input port! "
											+ pce.getMessage());
						}
					}
					rshellProcessor.addPort(inputPort);
				} catch (PortCreationException pce) {
					throw new ProcessorCreationException(
							"Unable to create input port! " + pce.getMessage());
				} catch (DuplicatePortNameException dpne) {
					throw new ProcessorCreationException(
							"Unable to create input port! " + dpne.getMessage());
				}
			}
		}

		// Handle outputs
		Element outputList = rshellElement.getChild(OUTPUT_PORT_LIST_TAG,
				XScufl.XScuflNS);
		if (outputList != null) {
			Iterator i = outputList.getChildren().iterator();
			while (i.hasNext()) {
				Element outputElement = (Element) i.next();
				String outputName = outputElement.getTextTrim();
				String symanticType = outputElement.getAttributeValue(
						SYMANTIC_TYPE_ATTRIBUTE, XScufl.XScuflNS);

				try {
					RshellOutputPort outputPort = new RshellOutputPort(
							rshellProcessor, outputName);
					if (symanticType != null) {
						try {
							outputPort.setSymanticType(SymanticTypes
									.valueOf(symanticType));
						} catch (IllegalArgumentException pce) {
							throw new ProcessorCreationException(
									"Unable to create output port! "
											+ pce.getMessage());
						}
					}
					rshellProcessor.addPort(outputPort);
				} catch (PortCreationException pce) {
					throw new ProcessorCreationException(
							"Unable to create output port! " + pce.getMessage());
				} catch (DuplicatePortNameException dpne) {
					throw new ProcessorCreationException(
							"Unable to create output port! "
									+ dpne.getMessage());
				}
			}
		}

		return rshellProcessor;
	}

}
