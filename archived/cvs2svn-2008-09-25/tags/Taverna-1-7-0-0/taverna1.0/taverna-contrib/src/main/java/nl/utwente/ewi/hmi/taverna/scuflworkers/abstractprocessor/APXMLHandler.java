/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import java.util.Iterator;

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
 * Class for handling the xml belonging to the abstract processor
 */
public class APXMLHandler implements XMLHandler {

	private static final String ABSTRACT_PROCESSOR_TAG = "abstractprocessor";

	private static final String INPUT_PORT_LIST_TAG = "inputPortList";

	private static final String INPUT_PORT_TAG = "inputPort";

	private static final String SYNTACTIC_TYPE_ATTRIBUTE = "syntacticType";

	private static final String OUTPUT_PORT_LIST_TAG = "outputPortList";

	private static final String OUTPUT_PORT_TAG = "outputPort";

	/**
	 * Method for getting the XML element of the prototype processor
	 * 
	 * @param processorFactory
	 *            of the processor
	 * @return the XML element
	 */
	public Element elementForFactory(ProcessorFactory processorFactory) {
		return new Element(ABSTRACT_PROCESSOR_TAG, XScufl.XScuflNS);
	}

	/**
	 * Method for getting a factory for an Element specification of a processor
	 * 
	 * @param processElement
	 *            the XML element defining the prototype
	 * @return the processor factory
	 */
	public ProcessorFactory getFactory(Element processElement) {
		return new APProcessorFactory();
	}

	/**
	 * Method for getting the xml element for the processor
	 * 
	 * @param processor
	 *            the processor to export to an xml element
	 * @return the XML rshell element
	 */
	public Element elementForProcessor(Processor processor) {
		APProcessor apProcessor = (APProcessor) processor;
		Element apElement = new Element(ABSTRACT_PROCESSOR_TAG, XScufl.XScuflNS);

		// set the description
		apElement.setText(apProcessor.getTaskDescription());

		// Input list
		Element inputList = new Element(INPUT_PORT_LIST_TAG, XScufl.XScuflNS);
		for (InputPort inputPort : apProcessor.getInputPorts()) {
			Element inputElement = new Element(INPUT_PORT_TAG, XScufl.XScuflNS);
			inputElement.setText(inputPort.getName());
			inputElement.setAttribute(SYNTACTIC_TYPE_ATTRIBUTE, inputPort
					.getSyntacticType(), XScufl.XScuflNS);
			inputList.addContent(inputElement);
		}
		apElement.addContent(inputList);

		Element outputList = new Element(OUTPUT_PORT_LIST_TAG, XScufl.XScuflNS);

		for (OutputPort outputPort : apProcessor.getOutputPorts()) {
			Element outputElement = new Element(OUTPUT_PORT_TAG,
					XScufl.XScuflNS);
			outputElement.setText(outputPort.getName());
			outputElement.setAttribute(SYNTACTIC_TYPE_ATTRIBUTE, outputPort
					.getSyntacticType(), XScufl.XScuflNS);
			outputList.addContent(outputElement);
		}
		apElement.addContent(outputList);

		return apElement;
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

		APProcessor apProcessor = new APProcessor(model, name);
		Element apElement = processorElement.getChild(ABSTRACT_PROCESSOR_TAG,
				XScufl.XScuflNS);

		String taskDescription = apElement.getTextTrim();
		apProcessor.setTaskDescription(taskDescription);

		// Handle inputs
		Element inputList = apElement.getChild(INPUT_PORT_LIST_TAG,
				XScufl.XScuflNS);
		if (inputList != null) {
			Iterator i = inputList.getChildren().iterator();
			while (i.hasNext()) {
				Element inputElement = (Element) i.next();
				String inputName = inputElement.getTextTrim();
				String syntacticType = inputElement.getAttributeValue(
						SYNTACTIC_TYPE_ATTRIBUTE, XScufl.XScuflNS);

				try {
					InputPort inputPort = new InputPort(apProcessor, inputName);
					inputPort.setSyntacticType(syntacticType);
					apProcessor.addPort(inputPort);
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
		Element outputList = apElement.getChild(OUTPUT_PORT_LIST_TAG,
				XScufl.XScuflNS);
		if (outputList != null) {
			Iterator i = outputList.getChildren().iterator();
			while (i.hasNext()) {
				Element outputElement = (Element) i.next();
				String outputName = outputElement.getTextTrim();
				String syntacticType = outputElement.getAttributeValue(
						SYNTACTIC_TYPE_ATTRIBUTE, XScufl.XScuflNS);

				try {
					OutputPort outputPort = new OutputPort(apProcessor,
							outputName);
					outputPort.setSyntacticType(syntacticType);
					apProcessor.addPort(outputPort);
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

		return apProcessor;
	}
}
