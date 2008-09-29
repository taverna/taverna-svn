package org.embl.ebi.escience.scuflworkers.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ArrayTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ComplexTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.WSDLParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A LocalWorker that takes a given input port for a complex type, and splits it
 * to a single level generating an array of simple type inputs. Nested complex
 * types remain as type/xml and can be split further by adding additional
 * XMLInputSplitter processors.
 * 
 * 
 */

public class XMLInputSplitter implements LocalWorker, XMLExtensible {
	
	private static Logger logger = Logger.getLogger(XMLInputSplitter.class);
	
	private String[] inputNames = {};

	private String[] inputTypes = {};

	private String[] outputNames = { "output" };

	private String[] outputTypes = { "'text/xml'" };

	private TypeDescriptor typeDescriptor;

	public String[] inputNames() {

		return inputNames;
	}

	public String[] inputTypes() {

		return inputTypes;
	}

	public String[] outputNames() {

		return outputNames;
	}

	public String[] outputTypes() {

		return outputTypes;
	}

	/**
	 * Takes the inputs and generates an XML output based upon these inputs.
	 * Only inputs provided are included as tags within the resulting XML.
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		Map result = new HashMap();

		Element outputElement = (this.typeDescriptor.getName().length() > 0 ? new Element(this.typeDescriptor.getName())
				: new Element(this.typeDescriptor.getType()));
		try {
			if (typeDescriptor instanceof ComplexTypeDescriptor) {
				executeForComplexType(inputMap, result, outputElement);

			} else {
				for (Iterator iterator = inputMap.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					DataThing thing = (DataThing) inputMap.get(key);
					if (thing != null) {
						Object dataObject = thing.getDataObject();

						if (dataObject instanceof List) {
							Element dataElement = buildElementFromObject(key, "");
							for (Iterator listIterator = ((List) dataObject).iterator(); listIterator.hasNext();) {
								Element itemElement = buildElementFromObject(key, listIterator.next());
								dataElement.addContent(itemElement);
							}
							XMLOutputter outputter = new XMLOutputter();
							String xmlText = outputter.outputString(dataElement);
							DataThing outputThing = DataThingFactory.bake(xmlText);
							result.put(outputNames[0], outputThing);
						} else {
							Element dataElement = buildElementFromObject(key, dataObject);
							outputElement.addContent(dataElement);
							XMLOutputter outputter = new XMLOutputter();
							String xmlText = outputter.outputString(outputElement);
							DataThing outputThing = DataThingFactory.bake(xmlText);
							result.put(outputNames[0], outputThing);
						}

					}
				}
			}

		} catch (Exception e) {
			throw new TaskExecutionException("Problem executing task.", e);
		}

		return result;

	}

	/**
	 * Determines whether the given input port supports being splitted
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isSplittable(InputPort input) {
		boolean result = false;
		if (input.getSyntacticType().equalsIgnoreCase("'text/xml'")
				|| input.getSyntacticType().equalsIgnoreCase("l('text/xml')")) {
			if (input.getProcessor() instanceof WSDLBasedProcessor) {
				result = true;
			} else if (input.getProcessor() instanceof LocalServiceProcessor) {
				LocalServiceProcessor processor = (LocalServiceProcessor) input.getProcessor();
				if (processor.getWorker() instanceof XMLInputSplitter) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Dynamically generates the inputNames and inputTypes according to the
	 * TypeDescriptor associated with the supplied InputPort.
	 * 
	 * @param portToSplit
	 */
	public void setUpInputs(InputPort portToSplit) {
		if (portToSplit.getProcessor() instanceof WSDLBasedProcessor) {
			WSDLBasedProcessor proc = (WSDLBasedProcessor) portToSplit.getProcessor();
			WSDLParser parser = proc.getParser();

			try {
				List inputs = parser.getOperationInputParameters(proc.getOperationName());
				for (Iterator it = inputs.iterator(); it.hasNext();) {
					TypeDescriptor desc = (TypeDescriptor) it.next();
					if (desc.getName().equalsIgnoreCase(portToSplit.getName())) {
						typeDescriptor = desc;
						break;
					}
				}
			} catch (Exception e) {
				logger.error("Exception thrown splitting inputs",e);
			}
		} else if (portToSplit.getProcessor() instanceof LocalServiceProcessor) {
			LocalServiceProcessor processor = (LocalServiceProcessor) portToSplit.getProcessor();
			if (processor.getWorker() instanceof XMLInputSplitter) {
				XMLInputSplitter splitter = (XMLInputSplitter) processor.getWorker();
				TypeDescriptor workerDesc = splitter.typeDescriptor;
				if (workerDesc instanceof ComplexTypeDescriptor) {
					for (Iterator iterator = ((ComplexTypeDescriptor) workerDesc).getElements().iterator(); iterator
							.hasNext();) {
						TypeDescriptor desc = (TypeDescriptor) iterator.next();
						if (desc.getName().equals(portToSplit.getName())) {
							typeDescriptor = desc;
							break;
						}
					}
				} else if (workerDesc instanceof ArrayTypeDescriptor) {
					typeDescriptor = ((ArrayTypeDescriptor) workerDesc).getElementType();
				}
			}
		}
		if (typeDescriptor != null)
			defineFromTypeDescriptor();
	}

	private void executeForComplexType(Map inputMap, Map result, Element outputElement) throws JDOMException,
			IOException {
		ComplexTypeDescriptor complexDescriptor = (ComplexTypeDescriptor) typeDescriptor;
		for (Iterator inputIterator = complexDescriptor.getElements().iterator(); inputIterator.hasNext();) {
			TypeDescriptor elementType = (TypeDescriptor) inputIterator.next();
			String key = elementType.getName();
			DataThing thing = (DataThing) inputMap.get(key);
			if (thing != null) {
				Object dataObject = thing.getDataObject();

				if (dataObject instanceof List) {
					Element arrayElement = buildElementFromObject(key, "");
					for (Iterator itemIterator = ((List) dataObject).iterator(); itemIterator.hasNext();) {

						Object itemObject = itemIterator.next();
						Element dataElement = buildElementFromObject("item", itemObject);
						arrayElement.addContent(dataElement);
					}
					outputElement.addContent(arrayElement);
				} else {
					Element dataElement = buildElementFromObject(key, dataObject);
					outputElement.addContent(dataElement);
				}
			}
		}

		XMLOutputter outputter = new XMLOutputter();
		String xmlText = outputter.outputString(outputElement);
		DataThing outputThing = new DataThing(xmlText);
		result.put(outputNames[0], outputThing);
	}

	/**
	 * Generates the TypeDescriptor structure, and then the relevant inputs from
	 * the XML element provided. This assumes that the root of the structure is
	 * <complextype/>. This will be the same xml generated by provideXML.
	 */
	public void consumeXML(Element element) {
		typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
		defineFromTypeDescriptor();
	}

	/**
	 * Generates the XML that describes the TypeDescriptor, and therefore the
	 * inputs for this worker, to allow it to be reconstructed using consumeXML.
	 */
	public Element provideXML() {
		return XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(typeDescriptor);
	}

	private Element buildElementFromObject(String key, Object dataObject) throws JDOMException, IOException {
		Element dataElement = new Element(key);
		if (isXMLInput(key)) {
			String xml = dataObject.toString();
			if (xml.length() > 0) {
				Document doc = new SAXBuilder().build(new StringReader(xml));
				dataElement = doc.getRootElement();
				dataElement.detach();
			}
		} else {
			dataElement.setText(dataObject.toString());
		}
		return dataElement;
	}

	private void defineFromTypeDescriptor() {
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			List elements = ((ComplexTypeDescriptor) typeDescriptor).getElements();
			inputNames = new String[elements.size()];
			inputTypes = new String[elements.size()];
			Class[] types = new Class[elements.size()];
			TypeDescriptor.retrieveSignature(elements, inputNames, types);
			for (int i = 0; i < types.length; i++) {
				inputTypes[i] = TypeDescriptor.translateJavaType(types[i]);
			}
		} else if (typeDescriptor instanceof ArrayTypeDescriptor) {
			inputNames = new String[] { typeDescriptor.getType() };
			inputTypes = new String[] { "l('text/xml')" };
		}
	}

	private boolean isXMLInput(String key) {
		boolean result = false;
		for (int i = 0; i < inputNames.length; i++) {
			if (inputNames[i].equals(key)) {
				result = inputTypes[i].indexOf("'text/xml'") != -1;
			}
		}
		return result;
	}

}
