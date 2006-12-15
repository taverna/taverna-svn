/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: XMLOutputSplitter.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-08-25 13:57:00 $
 *               by   $Author: sowen70 $
 * Created on 16-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * A LocalWorker that facilitates in extracting information from the inner
 * elements of an XML output from a SOAP based service that returns a complex
 * type. Information stored within deeper elements can be retreived by adding
 * additional XMLOutputSplitters.
 * 
 * @author sowen
 * 
 */
public class XMLOutputSplitter implements LocalWorkerWithPorts, XMLExtensible {

	private static Logger logger = Logger.getLogger(XMLInputSplitter.class);

	private String[] inputNames = { "input" };

	private String[] inputTypes = { "'text/xml'" };

	private String[] outputNames = {};

	private String[] outputTypes = {};

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
	 * Returns true if the given port maps to a complex type whose data
	 * structure contains cyclic references
	 * 
	 * @param port
	 * @return
	 */
	public boolean doesTypeContainCyclicReferences(OutputPort port) {
		TypeDescriptor td = getTypeDescriptor(port);
		return TypeDescriptor.isCyclic(td);
	}

	/**
	 * Determines whether the given output port supports being splitted
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isSplittable(OutputPort output) {
		boolean result = false;
		if (output.getSyntacticType().equalsIgnoreCase("'text/xml'")
				|| output.getSyntacticType().equalsIgnoreCase("l('text/xml')")) {
			if (output.getProcessor() instanceof WSDLBasedProcessor) {
				result = true;
			} else if (output.getProcessor() instanceof LocalServiceProcessor) {
				LocalServiceProcessor processor = (LocalServiceProcessor) output
						.getProcessor();
				if (processor.getWorker() instanceof XMLOutputSplitter) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Sets up the outputs for the worker based on the type descriptions of the
	 * OutputPort provided.
	 * 
	 * @param portToSplit
	 */
	public void setUpOutputs(OutputPort portToSplit) {
		TypeDescriptor typeDescriptor = getTypeDescriptor(portToSplit);
		if (typeDescriptor != null)
			defineFromTypeDescriptor();
	}

	private TypeDescriptor getTypeDescriptor(OutputPort portToSplit) {
		if (typeDescriptor == null) {
			if (portToSplit.getProcessor() instanceof WSDLBasedProcessor) {
				WSDLBasedProcessor proc = (WSDLBasedProcessor) portToSplit
						.getProcessor();
				WSDLParser parser = proc.getParser();
				try {
					List outputs = parser.getOperationOutputParameters(proc
							.getOperationName());
					for (Iterator it = outputs.iterator(); it.hasNext();) {
						TypeDescriptor desc = (TypeDescriptor) it.next();
						if (desc.getName().equalsIgnoreCase(
								portToSplit.getName())) {
							typeDescriptor = desc;
							break;
						}
					}
				} catch (Exception e) {
					logger.error("Exception thrown splitting outputs", e);
				}
			} else if (portToSplit.getProcessor() instanceof LocalServiceProcessor) {
				LocalServiceProcessor processor = (LocalServiceProcessor) portToSplit
						.getProcessor();
				if (processor.getWorker() instanceof XMLOutputSplitter) {
					XMLOutputSplitter splitter = (XMLOutputSplitter) processor
							.getWorker();
					TypeDescriptor workerDesc = splitter.typeDescriptor;
					if (workerDesc instanceof ComplexTypeDescriptor) {
						for (Iterator iterator = ((ComplexTypeDescriptor) workerDesc)
								.getElements().iterator(); iterator.hasNext();) {
							TypeDescriptor desc = (TypeDescriptor) iterator
									.next();
							if (desc.getName().equals(portToSplit.getName())) {
								typeDescriptor = desc;
								break;
							}
						}
					} else if (workerDesc instanceof ArrayTypeDescriptor) {
						typeDescriptor = ((ArrayTypeDescriptor) workerDesc)
								.getElementType();
					}
				}
			}
		}
		return typeDescriptor;
	}

	private void defineFromTypeDescriptor() {
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			List elements = ((ComplexTypeDescriptor) typeDescriptor)
					.getElements();
			outputNames = new String[elements.size()];
			outputTypes = new String[elements.size()];
			Class[] types = new Class[elements.size()];
			TypeDescriptor.retrieveSignature(elements, outputNames, types);
			for (int i = 0; i < types.length; i++) {
				outputTypes[i] = TypeDescriptor.translateJavaType(types[i]);
			}
		} else if (typeDescriptor instanceof ArrayTypeDescriptor) {
			outputNames = new String[] { typeDescriptor.getName() };
			if (((ArrayTypeDescriptor) typeDescriptor).getElementType() instanceof BaseTypeDescriptor) {
				outputTypes = new String[] { "l('text/plain')" };
			} else {
				outputTypes = new String[] { "l('text/xml')" };
			}
		}
	}

	/**
	 * Generates the TypeDescriptor structure, and then the relevant outputs
	 * from the XML element provided. This assumes that the root of the
	 * structure is <complextype/>. This will be the same xml generated by
	 * provideXML.
	 */
	public void consumeXML(Element element) {
		typeDescriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);
		defineFromTypeDescriptor();
	}

	/**
	 * Generates the XML that describes the TypeDescriptor, and therefore the
	 * outputs for this worker, to allow it to be reconstructed using
	 * consumeXML.
	 */
	public Element provideXML() {
		return XMLSplitterSerialisationHelper
				.typeDescriptorToExtensionXML(typeDescriptor);
	}

	/**
	 * Takes an XML input, and peels away the first layer and populates the
	 * outputs with the children of this layer. If a child holds a primitive
	 * value then the output for this field is simply populated, for complex
	 * types the xml is passed forward to that output. For arrays, an ArrayList
	 * is populated. Outputs that are not present in the incoming XML are either
	 * populated with an empty text value, or an empty XML element for the
	 * output name if it is a complex type. Arrays are populated as an empty
	 * array.
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThing inputThing = (DataThing) inputMap.get(inputNames[0]);
		Map result = new HashMap();
		List outputNameList = Arrays.asList(outputNames);
		if (inputThing != null) {
			String xml = inputThing.getDataObject().toString();
			try {
				Document doc = new SAXBuilder().build(new StringReader(xml));
				List children = doc.getRootElement().getChildren();
				if (typeDescriptor instanceof ArrayTypeDescriptor) {
					if (outputNames.length > 1)
						throw new TaskExecutionException(
								"Unexpected, multiple output names for ArrayType");
					executeForArrayType(result, children);
				} else {
					executeForComplexType(result, outputNameList, children);
				}

				// populate missing outputs with empty strings for basic types,
				// empty elements for complex/array types.
				for (int i = 0; i < outputNames.length; i++) {
					if (result.get(outputNames[i]) == null) {
						if (outputTypes[i].equals("'text/xml'")) {
							result.put(outputNames[i], new DataThing("<"
									+ outputNames[i] + " />"));
						} else if (outputTypes[i].startsWith("l('")) {
							result.put(outputNames[i], DataThingFactory
									.bake(new ArrayList()));
						} else {
							result.put(outputNames[i], new DataThing(""));
						}

					}
				}
			} catch (JDOMException e) {
				throw new TaskExecutionException("Unable to parse XML: " + xml,
						e);
			} catch (IOException e) {
				throw new TaskExecutionException("IOException parsing XML: "
						+ xml, e);
			}

		}

		return result;
	}

	private void executeForArrayType(Map result, List children) {
		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) typeDescriptor;
		List values = new ArrayList();
		XMLOutputter outputter = new XMLOutputter();

		boolean isInnerBaseType = arrayDescriptor.getElementType() instanceof BaseTypeDescriptor;
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Element child = (Element) iterator.next();
			if (isInnerBaseType) {
				values.add(child.getText());
			} else {
				values.add(outputter.outputString(child));
			}
		}
		result.put(outputNames[0], DataThingFactory.bake(values));
	}

	private void executeForComplexType(Map result, List outputNameList,
			List children) {
		XMLOutputter outputter = new XMLOutputter();
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Element child = (Element) iterator.next();
			if (outputNameList.contains(child.getName())) {
				int i = outputNameList.indexOf(child.getName());
				if (outputTypes[i].equals("'text/xml'")
						|| outputTypes[i].equals("l('text/xml')")) {
					String xmlText = outputter.outputString(child);
					result.put(child.getName(), new DataThing(xmlText));
				} else if (outputTypes[i].equals("'application/octet-stream'")) { // base64Binary
					byte[] data = Base64.decode(child.getText());
					result.put(child.getName(), DataThingFactory.bake(data));
				} else {
					result.put(child.getName(), new DataThing(child.getText()));
				}
			}

		}
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
