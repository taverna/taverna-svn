package org.embl.ebi.escience.scuflworkers.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.Base64;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
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

public class XMLInputSplitter implements LocalWorkerWithPorts, XMLExtensible {

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
		Map<String, DataThing> result = new HashMap<String, DataThing>();

		Element outputElement = (this.typeDescriptor.getName().length() > 0 ? new Element(
				this.typeDescriptor.getName())
				: new Element(this.typeDescriptor.getType()));
		try {
			if (typeDescriptor instanceof ComplexTypeDescriptor) {
				executeForComplexType(inputMap, result, outputElement);

			} else {
				for (Iterator iterator = inputMap.keySet().iterator(); iterator
						.hasNext();) {
					String key = (String) iterator.next();
					DataThing thing = (DataThing) inputMap.get(key);
					if (thing != null) {
						Object dataObject = thing.getDataObject();

						if (dataObject instanceof List) {
							Element dataElement = buildElementFromObject(key,
									"");
							for (Iterator listIterator = ((List) dataObject)
									.iterator(); listIterator.hasNext();) {
								Element itemElement = buildElementFromObject(
										key, listIterator.next());
								dataElement.addContent(itemElement);
							}

							XMLOutputter outputter = new XMLOutputter();
							String xmlText = outputter
									.outputString(dataElement);
							DataThing outputThing = DataThingFactory
									.bake(xmlText);
							result.put(outputNames[0], outputThing);
						} else {
							Element dataElement = buildElementFromObject(key,
									dataObject);
							outputElement.addContent(dataElement);
							XMLOutputter outputter = new XMLOutputter();
							String xmlText = outputter
									.outputString(outputElement);
							DataThing outputThing = DataThingFactory
									.bake(xmlText);
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
				LocalServiceProcessor processor = (LocalServiceProcessor) input
						.getProcessor();
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
			WSDLBasedProcessor proc = (WSDLBasedProcessor) portToSplit
					.getProcessor();
			WSDLParser parser = proc.getParser();

			try {
				List inputs = parser.getOperationInputParameters(proc
						.getOperationName());
				for (Iterator it = inputs.iterator(); it.hasNext();) {
					TypeDescriptor desc = (TypeDescriptor) it.next();
					if (desc.getName().equalsIgnoreCase(portToSplit.getName())) {
						typeDescriptor = desc;
						break;
					}
				}
			} catch (Exception e) {
				logger.error("Exception thrown splitting inputs", e);
			}
		} else if (portToSplit.getProcessor() instanceof LocalServiceProcessor) {
			LocalServiceProcessor processor = (LocalServiceProcessor) portToSplit
					.getProcessor();
			if (processor.getWorker() instanceof XMLInputSplitter) {
				XMLInputSplitter splitter = (XMLInputSplitter) processor
						.getWorker();
				TypeDescriptor workerDesc = splitter.typeDescriptor;
				if (workerDesc instanceof ComplexTypeDescriptor) {
					for (Iterator iterator = ((ComplexTypeDescriptor) workerDesc)
							.getElements().iterator(); iterator.hasNext();) {
						TypeDescriptor desc = (TypeDescriptor) iterator.next();
						if (desc.getName().equals(portToSplit.getName())) {
							typeDescriptor = desc;
							if (typeDescriptor instanceof ArrayTypeDescriptor
									&& !((ArrayTypeDescriptor) typeDescriptor)
											.isWrapped()) {
								typeDescriptor = ((ArrayTypeDescriptor) typeDescriptor)
										.getElementType();
							}
							break;
						}
					}
				} else if (workerDesc instanceof ArrayTypeDescriptor) {
					typeDescriptor = ((ArrayTypeDescriptor) workerDesc)
							.getElementType();
				}
			}
		}
		if (typeDescriptor != null)
			defineFromTypeDescriptor();
	}

	private void executeForComplexType(Map inputMap,
			Map<String, DataThing> result, Element outputElement)
			throws JDOMException, IOException {
		ComplexTypeDescriptor complexDescriptor = (ComplexTypeDescriptor) typeDescriptor;
		for (Iterator inputIterator = complexDescriptor.getElements()
				.iterator(); inputIterator.hasNext();) {
			TypeDescriptor elementType = (TypeDescriptor) inputIterator.next();
			String key = elementType.getName();
			DataThing thing = (DataThing) inputMap.get(key);
			if (thing != null) {
				Object dataObject = thing.getDataObject();

				if (dataObject instanceof List) {
					Element arrayElement = buildElementFromObject(key, "");

					String itemkey = "item";
					boolean wrapped = false;
					if (elementType instanceof ArrayTypeDescriptor) {
						wrapped = ((ArrayTypeDescriptor) elementType)
								.isWrapped();
						TypeDescriptor arrayElementType = ((ArrayTypeDescriptor) elementType)
								.getElementType();
						if (!wrapped) {
							itemkey = elementType.getName();
						} else {
							if (arrayElementType.getName() != null
									&& arrayElementType.getName().length() > 0) {
								itemkey = arrayElementType.getName();
							} else {
								itemkey = arrayElementType.getType();
							}
						}

					}

					for (Iterator itemIterator = ((List) dataObject).iterator(); itemIterator
							.hasNext();) {

						Object itemObject = itemIterator.next();
						Element dataElement = buildElementFromObject(itemkey,
								itemObject);
						dataElement.setNamespace(Namespace
								.getNamespace(elementType.getNamespaceURI()));
						if (!wrapped) {
							dataElement.setName(itemkey);
							outputElement.addContent(dataElement);
						} else {
							arrayElement.addContent(dataElement);
						}
					}
					if (wrapped)
						outputElement.addContent(arrayElement);
				} else {
					Element dataElement = buildElementFromObject(key,
							dataObject);
					outputElement.addContent(dataElement);
				}
			}
		}
		outputElement.setNamespace(Namespace.getNamespace(typeDescriptor
				.getNamespaceURI()));
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
		typeDescriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);
		defineFromTypeDescriptor();
	}

	/**
	 * Generates the XML that describes the TypeDescriptor, and therefore the
	 * inputs for this worker, to allow it to be reconstructed using consumeXML.
	 */
	public Element provideXML() {
		return XMLSplitterSerialisationHelper
				.typeDescriptorToExtensionXML(typeDescriptor);
	}

	private Element buildElementFromObject(String key, Object dataObject)
			throws JDOMException, IOException {

		Element dataElement = null;

		if (isXMLInput(key)) {
			dataElement = createDataElementForXMLInput(dataObject, key);
		} else {
			dataElement = new Element(key);
			setDataElementNamespace(key, dataElement);
			if (dataObject.toString().equals("nil")) {
				dataElement.setAttribute("nil", "true"); // changes nil value
				// to nil=true
				// attribute.
			} else {
				if (dataObject instanceof byte[]) {
					dataElement
							.setAttribute(
									"type",
									"xsd:base64Binary",
									org.jdom.Namespace
											.getNamespace("xsi",
													"http://www.w3.org/2001/XMLSchema-instance"));
					dataObject = Base64.encodeBytes((byte[]) dataObject);
				}
				dataElement.setText(dataObject.toString());
			}

		}
		return dataElement;
	}

	private Element createDataElementForXMLInput(Object dataObject, String key)
			throws JDOMException, IOException {
		Element dataElement = null;
		String xml = dataObject.toString();
		if (xml.length() > 0) {
			Document doc = new SAXBuilder().build(new StringReader(xml));
			dataElement = doc.getRootElement();
			dataElement.detach();
		} else {
			dataElement = new Element(key);
		}

		setDataElementNamespace(key, dataElement);
		return dataElement;
	}

	// set the namespace if it can be determined from the element TypeDescriptor
	// by the key
	private void setDataElementNamespace(String key, Element dataElement) {
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			TypeDescriptor elementTypeDescriptor = ((ComplexTypeDescriptor) typeDescriptor)
					.elementForName(key);
			if (elementTypeDescriptor != null) {
				String nsURI=null;
				if (elementTypeDescriptor instanceof BaseTypeDescriptor) {
					nsURI=elementTypeDescriptor.getNamespaceURI();
					//this is some protective code against old workflows that had the base element namespace incorrectly
					//declared (it was using the type NS, rather than the element NS. 
					if (nsURI.contains("XMLSchema") && nsURI.contains("http://www.w3.org")) {
						nsURI=typeDescriptor.getNamespaceURI();
					}
				}
				else {
					nsURI=elementTypeDescriptor.getNamespaceURI();
				}
				if (nsURI!=null && nsURI.length()>0) {
					updateElementNamespace(dataElement, nsURI);
				}
			}
		}
	}

	/**
	 * Updates the element namespace, and also iterates all descendant elements.
	 * If these elements have no default namespace, or is blank then it is also
	 * set to namespaceURI (JDOM by default will not set the child elements to
	 * the same namespace as the element modified but will override them with
	 * blank namespaces).
	 * 
	 * @param dataElement
	 * @param namespaceURI
	 */
	private void updateElementNamespace(Element dataElement, String namespaceURI) {
		dataElement.setNamespace(Namespace.getNamespace(namespaceURI));
		Iterator iterator = dataElement.getDescendants();
		while (iterator.hasNext()) {
			Object descendantObject = iterator.next();
			if (descendantObject instanceof Element) {
				Element childElement = (Element) descendantObject;
				if (childElement.getNamespaceURI() == null
						|| childElement.getNamespaceURI().length() == 0)
					childElement.setNamespace(Namespace
							.getNamespace(namespaceURI));
			}
		}
	}

	private void defineFromTypeDescriptor() {
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			List elements = ((ComplexTypeDescriptor) typeDescriptor)
					.getElements();
			inputNames = new String[elements.size()];
			inputTypes = new String[elements.size()];
			Class[] types = new Class[elements.size()];
			TypeDescriptor.retrieveSignature(elements, inputNames, types);
			for (int i = 0; i < types.length; i++) {
				inputTypes[i] = TypeDescriptor.translateJavaType(types[i]);
			}
		} else if (typeDescriptor instanceof ArrayTypeDescriptor) {
			inputNames = new String[] { typeDescriptor.getType() };
			if (((ArrayTypeDescriptor) typeDescriptor).getElementType() instanceof BaseTypeDescriptor) {
				inputTypes = new String[] { "l('text/plain')" };
			} else {
				inputTypes = new String[] { "l('text/xml')" };
			}
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

	public List<InputPort> inputPorts(LocalServiceProcessor processor)
			throws DuplicatePortNameException, PortCreationException {
		List<InputPort> result = new ArrayList<InputPort>();
		for (int i = 0; i < inputNames().length; i++) {
			// Create input ports
			InputPort port = new XMLSplittableInputPort(processor,
					inputNames()[i]);
			port.setSyntacticType(inputTypes()[i]);
			result.add(port);
		}
		return result;
	}

	public List<OutputPort> outputPorts(LocalServiceProcessor processor)
			throws DuplicatePortNameException, PortCreationException {
		List<OutputPort> result = new ArrayList<OutputPort>();
		for (int i = 0; i < outputNames().length; i++) {
			// Create output ports
			OutputPort port = new XMLSplittableOutputPort(processor,
					outputNames()[i]);
			port.setSyntacticType(outputTypes()[i]);
			SemanticMarkup m = port.getMetadata();
			String[] mimeTypes = ((outputTypes()[i].split("\\'"))[1])
					.split(",");
			for (int j = 0; j < mimeTypes.length; j++) {
				logger.debug("Mime type " + mimeTypes[j]);
				m.addMIMEType(mimeTypes[j]);
			}
			result.add(port);
		}
		return result;
	}

}
