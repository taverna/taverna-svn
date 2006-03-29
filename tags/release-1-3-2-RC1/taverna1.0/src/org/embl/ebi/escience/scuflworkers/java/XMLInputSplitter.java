package org.embl.ebi.escience.scuflworkers.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ArrayTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.BaseTypeDescriptor;
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
			List inputs = new ArrayList();
			List outputs = new ArrayList();
			try {
				parser.getOperationParameters(proc.getOperationName(), inputs, outputs);
				for (Iterator it = inputs.iterator(); it.hasNext();) {
					TypeDescriptor desc = (TypeDescriptor) it.next();
					if (desc.getName().equalsIgnoreCase(portToSplit.getName())) {
						typeDescriptor = desc;
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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

	/**
	 * Takes the inputs and generates an XML output based upon these inputs.
	 * Only inputs provided are included as tags within the resulting XML.
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		Map result = new HashMap();
		Element outputElement = new Element(this.typeDescriptor.getType());
		try {
			for (Iterator iterator = inputMap.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				DataThing thing = (DataThing) inputMap.get(key);
				Object dataObject = thing.getDataObject();

				if (dataObject instanceof List) {
					for (Iterator itemIterator = ((List) dataObject).iterator(); itemIterator.hasNext();) {
						Object itemObject = itemIterator.next();
						Element dataElement = buildElementFromObject(key, itemObject);
						outputElement.addContent(dataElement);
					}
				} else {
					Element dataElement = buildElementFromObject(key, dataObject);
					outputElement.addContent(dataElement);
				}

			}
		} catch (Exception e) {
			throw new TaskExecutionException("Problem executing task.", e);
		}

		XMLOutputter outputter = new XMLOutputter();
		String xmlText = outputter.outputString(outputElement);
		DataThing outputThing = new DataThing(xmlText);
		result.put(outputNames[0], outputThing);

		return result;

	}
	
	/**
	 * Generates the TypeDescriptor structure, and then the relevant inputs from
	 * the XML element provided. This assumes that the root of the structure is
	 * <complextype/>. This will be the same xml generated by provideXML.
	 */
	public void consumeXML(Element element) {
		Element child = (Element) element.getChildren().get(0);
		typeDescriptor = buildTypeDescriptorFromElement(child);
		defineFromTypeDescriptor();
	}

	/**
	 * Generates the XML that describes the TypeDescriptor, and therefore the
	 * inputs for this worker, to allow it to be reconstructed using consumeXML.
	 */
	public Element provideXML() {
		Element result = new Element("extensions", XScufl.XScuflNS);
		Element type = null;
		if (typeDescriptor instanceof ComplexTypeDescriptor)
			type = constructElementForComplexType((ComplexTypeDescriptor) typeDescriptor);
		else if (typeDescriptor instanceof ArrayTypeDescriptor)
			type = constructElementForArrayType((ArrayTypeDescriptor) typeDescriptor);
		result.addContent(type);
		return result;
	}

	private Element buildElementFromObject(String key, Object dataObject) throws JDOMException, IOException {
		Element dataElement = new Element(key);
		if (isXMLInput(key)) {
			String xml = dataObject.toString();
			Document doc = new SAXBuilder().build(new StringReader(xml));
			dataElement = doc.getRootElement();
			dataElement.detach();
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

	private TypeDescriptor buildTypeDescriptorFromElement(Element element) {
		TypeDescriptor result = null;

		if (element.getName().equalsIgnoreCase("complextype")) {
			ComplexTypeDescriptor desc = new ComplexTypeDescriptor();
			Element elements = element.getChild("elements", XScufl.XScuflNS);
			for (Iterator iterator = elements.getChildren().iterator(); iterator.hasNext();) {
				Element childElement = (Element) iterator.next();
				desc.getElements().add(buildTypeDescriptorFromElement(childElement));
			}
			result = desc;
		} else if (element.getName().equalsIgnoreCase("arraytype")) {
			result = new ArrayTypeDescriptor();
			Element elementType = element.getChild("elementtype", XScufl.XScuflNS);
			((ArrayTypeDescriptor) result).setElementType(buildTypeDescriptorFromElement((Element) elementType
					.getChildren().get(0)));
		} else if (element.getName().equalsIgnoreCase("basetype")) {
			result = new BaseTypeDescriptor();

		}

		result.setName(element.getAttributeValue("name"));
		result.setType(element.getAttributeValue("typename"));
		result.setOptional(element.getAttributeValue("optional").equalsIgnoreCase("true"));
		result.setUnbounded(element.getAttributeValue("unbounded").equalsIgnoreCase("true"));
		return result;
	}

	private void populateElement(Element element, TypeDescriptor descriptor) {
		element.setAttribute("optional", String.valueOf(descriptor.isOptional()));
		element.setAttribute("unbounded", String.valueOf(descriptor.isUnbounded()));
		element.setAttribute("typename", descriptor.getType());
		element.setAttribute("name", descriptor.getName() == null ? "" : descriptor.getName());
	}

	private Element constructElementForArrayType(ArrayTypeDescriptor descriptor) {
		Element result = new Element("arraytype", XScufl.XScuflNS);
		populateElement(result, descriptor);
		Element elementType = new Element("elementtype", XScufl.XScuflNS);
		if (descriptor.getElementType() instanceof ComplexTypeDescriptor) {
			elementType.addContent(constructElementForComplexType((ComplexTypeDescriptor) descriptor.getElementType()));
		} else if (descriptor.getElementType() instanceof ArrayTypeDescriptor) {
			elementType.addContent(constructElementForArrayType((ArrayTypeDescriptor) descriptor.getElementType()));
		} else if (descriptor.getElementType() instanceof BaseTypeDescriptor) {
			Element element = new Element("basetype", XScufl.XScuflNS);
			populateElement(element, descriptor.getElementType());
			elementType.addContent(element);
		}
		result.addContent(elementType);
		return result;
	}

	private Element constructElementForComplexType(ComplexTypeDescriptor descriptor) {
		Element result = new Element("complextype", XScufl.XScuflNS);
		populateElement(result, descriptor);
		Element elements = new Element("elements", XScufl.XScuflNS);
		for (Iterator iterator = descriptor.getElements().iterator(); iterator.hasNext();) {
			TypeDescriptor desc = (TypeDescriptor) iterator.next();
			Element element = null;
			if (desc instanceof ComplexTypeDescriptor) {
				element = constructElementForComplexType((ComplexTypeDescriptor) desc);
			} else if (desc instanceof ArrayTypeDescriptor) {
				element = constructElementForArrayType((ArrayTypeDescriptor) desc);
			} else if (desc instanceof BaseTypeDescriptor) {
				element = new Element("basetype", XScufl.XScuflNS);
				populateElement(element, desc);
			}
			if (element != null)
				elements.addContent(element);
		}
		result.addContent(elements);
		return result;
	}
}
