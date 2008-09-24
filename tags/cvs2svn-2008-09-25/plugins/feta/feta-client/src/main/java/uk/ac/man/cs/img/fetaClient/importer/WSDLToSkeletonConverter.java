/*
 * Created on Jun 07, 2004
 *
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
 */
package uk.ac.man.cs.img.fetaClient.importer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.DnDFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WSDLFragment;
import uk.ac.man.cs.img.fetaClient.util.DOMUtil;
import uk.ac.man.cs.img.fetaClient.util.WSDLResolver;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

public class WSDLToSkeletonConverter {

	private Definition definition;

	private QName addressQName;

	private static final String addressTagNS = "http://schemas.xmlsoap.org/wsdl/soap/";

	private static final String addressTagName = "address";

	private DnDFragment fraggy;

	private String wsdlURL;

	private String operationName;

	public WSDLToSkeletonConverter(InputStream WSDLStream) {

		try {
			this.wsdlURL = "http://"; // template to be filled in by the Pedro
										// user
			this.operationName = null;
			definition = WSDLResolver.getInstance().resolveStream(WSDLStream);
			addressQName = new QName(addressTagNS, addressTagName);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}

	public WSDLToSkeletonConverter(String WSDLURL) {

		this(WSDLURL, null);

	}

	public WSDLToSkeletonConverter(String WSDLURL, String OperationName) {
		try {
			this.wsdlURL = WSDLURL;
			this.operationName = OperationName;
			definition = getDefinition(WSDLURL);
			addressQName = new QName(addressTagNS, addressTagName);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}

	public WSDLToSkeletonConverter(WSDLFragment specElement) {

		this(specElement.getWsdlLoc(), specElement.getOperName());
		fraggy = specElement;

	}

	public Map convert() throws FetaImportException {
		try {
			Map conversionResults = new HashMap();
			boolean found = (operationName == null ? true : false);
			for (Iterator serviceIterator = definition.getServices().values()
					.iterator(); serviceIterator.hasNext();) {

				Document doc = new DocumentImpl();

				// GUARANTEE that we have a single top level element named
				// Service Descriptions
				// eventhough it contains a single ServiceDescription
				// sub-element we do not give up on this element
				Element el_descriptions = doc
						.createElement(FetaModelXSD.SERVICE_DESCS);
				doc.appendChild(el_descriptions);

				Service servicee = (Service) serviceIterator.next();

				String serviceName = servicee.getQName().getLocalPart();

				// Currently assuming that the service is delivered via single
				// portType
				Port portt = (Port) servicee.getPorts().values().toArray()[0];
				String serviceLocation = getSOAPAddress(portt);

				// Element Declarations for the Service
				// Generate a SERVICE ELEMENT for each service decription in the
				// WSDL file
				Element el_description = doc
						.createElement(FetaModelXSD.SERVICE_DESC);
				// Create element

				Element el_serviceName = doc
						.createElement(FetaModelXSD.SERVICE_NAME);
				// Create elementthat holds name which is the local part of the
				// qualified name
				el_serviceName.appendChild(doc.createTextNode(serviceName));

				Element el_serviceLoc = doc
						.createElement(FetaModelXSD.LOCATION_URL);
				// Create element that holds location URL
				el_serviceLoc.appendChild(doc.createTextNode(serviceLocation));

				Element el_serviceIntWSDL = doc
						.createElement(FetaModelXSD.INTERFACE_WSDL);
				// Create element that holds the WSDL location
				el_serviceIntWSDL.appendChild(doc.createTextNode(this.wsdlURL));

				Element el_serviceDescText = doc
						.createElement(FetaModelXSD.SERV_DESC_TEXT);
				// Create element that will hold description of service
				el_serviceDescText.appendChild(doc.createTextNode(serviceName));

				Element el_serviceType = doc
						.createElement(FetaModelXSD.SERVICE_TYPE); // Create
																	// element
				el_serviceType.appendChild(doc.createTextNode(ServiceType.WSDL
						.toString()));
				// Create element that holds enumerated value of service type

				el_description.appendChild(el_serviceName);
				el_description.appendChild(el_serviceLoc);
				el_description.appendChild(el_serviceIntWSDL);
				el_description.appendChild(el_serviceDescText);
				el_description.appendChild(el_serviceType);

				el_descriptions.appendChild(el_description);

				// Element Declarations for operations of the Service

				PortType portTypee = (PortType) portt.getBinding()
						.getPortType();
				java.util.List serviceOperations = portTypee.getOperations();

				Element el_operations = doc
						.createElement(FetaModelXSD.OPERATIONS); // Create
																	// element
				el_description.appendChild(el_operations);

				for (int i = 0; i < serviceOperations.size(); i++) {
					Operation oper = (Operation) serviceOperations.get(i);
					if (this.operationName != null) {
						if (oper.getName().equalsIgnoreCase(this.operationName)) {
							found = true;
						} else
							continue;
					}

					Element el_operation = doc
							.createElement(FetaModelXSD.SERVICE_OPERATION); // Create
																			// element
					el_operations.appendChild(el_operation);

					Element el_operationName = doc
							.createElement(FetaModelXSD.OPERATION_NAME); // Create
																			// element
					el_operationName.appendChild(doc.createTextNode(oper
							.getName()));
					el_operation.appendChild(el_operationName);

					if (fraggy != null) {
						Element el_operationSpec = doc
								.createElement(FetaModelXSD.OPERATION_SPEC); // Create
																				// element
						el_operationSpec.appendChild(doc.createTextNode(fraggy
								.getFragmentAsString()));
						el_operation.appendChild(el_operationSpec);
					}

					Element el_portName = doc
							.createElement(FetaModelXSD.PORT_NAME); // Create
																	// element
					el_portName.appendChild(doc.createTextNode(portTypee
							.getQName().toString()));
					el_operation.appendChild(el_portName);

					// Inputs
					Message inputMessage = oper.getInput().getMessage();
					Map inputParts = inputMessage.getParts();
					Element el_operationInputs = doc
							.createElement(FetaModelXSD.OPERATION_INPUTS); // Create
																			// element
					el_operation.appendChild(el_operationInputs);

					for (Iterator inpPartIterator = inputParts.values()
							.iterator(); inpPartIterator.hasNext();) {
						Part part = (Part) inpPartIterator.next();
						Element el_parameter = createParameterElement(doc,
								part, inputMessage);
						el_operationInputs.appendChild(el_parameter);
					}// for

					// Outputs
					Message outputMessage = oper.getOutput().getMessage();
					Map outputParts = outputMessage.getParts();
					Element el_operationOutputs = doc
							.createElement(FetaModelXSD.OPERATION_OUTPUTS); // Create
																			// element
					el_operation.appendChild(el_operationOutputs);

					for (Iterator outPartIterator = outputParts.values()
							.iterator(); outPartIterator.hasNext();) {
						Part part = (Part) outPartIterator.next();
						Element el_parameter = createParameterElement(doc,
								part, outputMessage);
						el_operationOutputs.appendChild(el_parameter);
					}// for
				}// for

				// writeOut(doc,OutputPath, serviceName);
				if (found) {
					conversionResults.put(serviceName, doc);
				}
			}// for

			return conversionResults;
		}// try

		catch (Exception exp) {
			throw new FetaImportException("Exception occured: "
					+ exp.getMessage());

		}// catch

	}

	private Element createParameterElement(Document doc, Part part,
			Message message) {

		Element parameter = doc.createElement(FetaModelXSD.PARAMETER); // Create
																		// element

		Element parameterName = doc.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																				// element
		parameterName.appendChild(doc.createTextNode(part.getName()));

		Element messageName = doc.createElement(FetaModelXSD.MESSAGE_NAME); // Create
																			// element
		messageName.appendChild(doc.createTextNode(message.getQName()
				.getLocalPart()));

		parameter.appendChild(parameterName);
		parameter.appendChild(messageName);

		return parameter;

	}

	private String getSOAPAddress(Port port) {
		for (Iterator i = port.getExtensibilityElements().iterator(); i
				.hasNext();) {
			ExtensibilityElement ee = (ExtensibilityElement) i.next();
			if (ee.getElementType().equals(addressQName)) {
				return ((SOAPAddress) ee).getLocationURI();
			}
		}
		return "";
	}

	private Definition getDefinition(String wsdlURL) throws WSDLException {

		definition = WSDLResolver.getInstance().resolveURL(wsdlURL);
		return definition;
	}

	private String getPartType(String messageName, String partName) {
		return definition.getMessage(QName.valueOf(messageName)).getPart(
				partName).getTypeName().toString();

	}

	public static void main(String[] args) throws Exception {

		// arg 0 WSDL URL
		// args 1 output path for files

		WSDLToSkeletonConverter myConv;

		if (args.length >= 1) {

			try {
				java.net.URL wsdlURL = new java.net.URL(args[0]);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
				System.exit(1);
			}

			myConv = new WSDLToSkeletonConverter(args[0]);
			Map testMap = myConv.convert();

			String outputPath;

			if (args.length > 1) {
				outputPath = args[1];
			} else {
				outputPath = ".";
			}

			for (Iterator j = testMap.entrySet().iterator(); j.hasNext();) {
				Map.Entry entry = (Map.Entry) j.next();
				DOMUtil.writeOut((Document) entry.getValue(), outputPath,
						(String) entry.getKey());
			}
		} else {
			printUsage();
			System.exit(1);
		}

	}

	private static void printUsage() {

		System.out
				.println("************************************************************");
		System.out
				.println("*****TO USE THE WSDL IMPORTER*******************************");
		System.out
				.println("*****Arg 1: A Valid URL for the WSDL File ******************");
		System.out
				.println("*****Arg 2: (Optional) OutputPath for Generated Skeletons***");
		System.out
				.println("************************************************************");
	}

}
