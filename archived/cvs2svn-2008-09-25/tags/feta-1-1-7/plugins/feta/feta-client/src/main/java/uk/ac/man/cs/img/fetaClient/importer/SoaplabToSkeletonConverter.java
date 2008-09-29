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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.DnDFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.SOAPLABFragment;
import uk.ac.man.cs.img.fetaClient.util.DOMUtil;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

public class SoaplabToSkeletonConverter implements IImporter {

	private String sourceURL;

	private String categoryName;

	private String serviceName;

	private Map paramsDescriptions;

	private DnDFragment fraggy;

	public SoaplabToSkeletonConverter(String SourceURL) {

		this.sourceURL = SourceURL;
		paramsDescriptions = new HashMap();

	}

	public SoaplabToSkeletonConverter(String SourceURL, String CategoryName) {

		this.sourceURL = SourceURL;
		this.categoryName = CategoryName;
		paramsDescriptions = new HashMap();

	}

	public SoaplabToSkeletonConverter(String SourceURL, String CategoryName,
			String ServiceName) {

		this.sourceURL = SourceURL;
		this.categoryName = CategoryName;
		this.serviceName = ServiceName;
		paramsDescriptions = new HashMap();

	}

	public SoaplabToSkeletonConverter(SOAPLABFragment specElement) {
		fraggy = specElement;
		paramsDescriptions = new HashMap();

		/*
		 * this code is majorally taken from TOm Oinn's code in Taverna
		 * SoaplabProcessor.java
		 */

		String[] pathbitss = specElement.getSoaplabServiceLoc().split("/");
		System.out.println("pathbits length " + pathbitss.length);
		for (int i = 0; i < pathbitss.length; i++) {
			System.out.println("pathbits " + pathbitss[i]);
		}

		if (pathbitss.length > 0) {
			String name = (String) pathbitss[(pathbitss.length) - 1];
			System.out.println("name is  " + name);
			int dotIndex = name.indexOf('.');
			if (dotIndex > 0)
				/*
				 * for (int j = 0; j<app.length; j++){
				 * System.out.println("apppppp " + app[j]); }
				 */
				// if (app.length >0) {
				categoryName = name.substring(0, dotIndex);
			serviceName = name.substring(dotIndex + 1);
			System.out.println("Service name is " + serviceName);
			System.out.println("category name is " + categoryName);

		} else {
			categoryName = null;
			serviceName = null;

		}

		String[] pathbits = specElement.getSoaplabServiceLoc().split("/");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < pathbits.length - 1; i++) {
			if (i > 0) {
				sb.append("/");
			}
			sb.append(pathbits[i]);
		}

		this.sourceURL = sb.toString();
		if (sourceURL.endsWith("/")) {
			// do nothing
		} else {
			sourceURL = sourceURL + "/";
		}

	}

	public Map convert() throws FetaImportException {
		Map conversionResults = new HashMap();
		// boolean soaplabInstalled = false;
		try {
			Service service = new Service();
			Call call = (Call) service.createCall();

			try {
				// try to call the analysis factory
				call.setTargetEndpointAddress(sourceURL + "AnalysisFactory");
				call.setOperationName(new QName("getAvailableCategories"));
				// soaplabInstalled = true;
				String[] categories = (String[]) (call.invoke(new Object[0]));

				for (int i = 0; i < categories.length; i++) {
					if ((this.categoryName != null)
							&& (!this.categoryName
									.equalsIgnoreCase(categories[i]))) {

						continue;
					} else {
						call = (Call) service.createCall();
						call.setTargetEndpointAddress(sourceURL
								+ "AnalysisFactory");
						call.setOperationName(new QName(
								"getAvailableAnalysesInCategory"));
						String[] services = (String[]) (call
								.invoke(new String[] { categories[i] }));
						if (this.serviceName == null) {
							conversionResults.putAll(convert(services));
						} else {

							for (int j = 0; j < services.length; j++) {
								// stupid api... why do we not get the stripped
								// service name?
								String tempServiceName = this.categoryName
										+ "." + this.serviceName;
								if (tempServiceName
										.equalsIgnoreCase(services[j])) {
									String[] serviceArray = { services[j] };
									conversionResults
											.putAll(convert(serviceArray));
								}
							}

						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// if (!soaplabInstalled){
			try {
				// in addition try to call the gowlab factory
				call.setTargetEndpointAddress(sourceURL + "GowlabFactory");
				call.setOperationName(new QName("getAvailableCategories"));
				String[] categories = (String[]) (call.invoke(new Object[0]));

				for (int i = 0; i < categories.length; i++) {
					if ((this.categoryName != null)
							&& (!this.categoryName
									.equalsIgnoreCase(categories[i]))) {

						continue;
					} else {
						call = (Call) new Service().createCall();
						call.setTargetEndpointAddress(sourceURL
								+ "GowlabFactory");
						call.setOperationName(new QName(
								"getAvailableAnalysesInCategory"));
						String[] services = (String[]) (call
								.invoke(new String[] { categories[i] }));
						if (this.serviceName == null) {
							conversionResults.putAll(convert(services));
						} else {
							for (int j = 0; j < services.length; j++) {
								String tempServiceName = this.categoryName
										+ "." + this.serviceName;
								if (tempServiceName
										.equalsIgnoreCase(services[j])) {
									String[] serviceArray = { services[j] };
									conversionResults
											.putAll(convert(serviceArray));
								}
							}

						}
					}
				}

			} catch (Exception exception) {
				exception.printStackTrace();
			}
			// }
		} catch (Exception exp) {
			System.out.println("NO SOAPLAB INSTALLATION FOUND AT " + sourceURL);
			exp.printStackTrace();
		}
		return conversionResults;
	}

	public String getDescription(URL endpoint) throws FetaImportException {
		try {

			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(endpoint.toString());

			call.setOperationName(new QName("getAnalysisType"));
			Map analysisType = (Map) call.invoke(new Object[0]);
			if (analysisType.containsKey("description")) {
				return (String) analysisType.get("description");
			}
			return "";

		} catch (javax.xml.rpc.ServiceException se) {
			throw new FetaImportException(
					"Unable to create a new call to connect to soaplab, error was : "
							+ se.getMessage());
		} catch (java.rmi.RemoteException re) {
			throw new FetaImportException(
					"Unable to call the getAnalysisType method : "
							+ re.getMessage());
		}

	}

	public String getDetailedDescription(URL endpoint)
			throws FetaImportException {
		try {

			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(endpoint.toString());
			call.setOperationName(new QName("describe"));
			String descriptionXML = (String) call.invoke(new Object[0]);

			try {
				Document document;
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				ByteArrayInputStream s = new ByteArrayInputStream(
						descriptionXML.getBytes());
				document = builder.parse((InputStream) s);

				NodeList parameters = document
						.getElementsByTagName("parameter");
				for (int i = 0; i < parameters.getLength(); i++) {
					Element param = (Element) parameters.item(i);
					if (param.getElementsByTagName("base").getLength() > 0) {
						Element baseElement = (Element) param
								.getElementsByTagName("base").item(0);
						Attr nameAttribute = (Attr) baseElement
								.getAttributeNode("name");

						NodeList helpElements = param
								.getElementsByTagName("help");
						if (helpElements.getLength() > 0) {
							Element helpElement = (Element) helpElements
									.item(0);
							NodeList textHlpElemList = helpElement
									.getChildNodes();
							paramsDescriptions.put((String) nameAttribute
									.getNodeValue(), ((Node) textHlpElemList
									.item(0)).getNodeValue().trim());

							// System.out.println((String)nameAttribute.getNodeValue()+"-----"+((Node)textHlpElemList.item(0)).getNodeValue().trim());

						}

					}
				}

				NodeList appInfos = document.getElementsByTagName("app_info");
				if (appInfos.getLength() == 1) {
					Element appInfoElement = (Element) appInfos.item(0);
					Attr helpURLAttribute = (Attr) appInfoElement
							.getAttributeNode("help_URL");
					return ". Detailed info about this operation can be found at the following link: "
							+ (String) helpURLAttribute.getNodeValue();

				}

			} catch (Exception e) {
				// Debug only
				e.printStackTrace();

			}

			return "NO DETAILED DOCUMENTATION LINK EXISTS FOR THIS OPERATION";
		} catch (javax.xml.rpc.ServiceException se) {
			throw new FetaImportException(
					"Unable to create a new call to connect to soaplab, error was : "
							+ se.getMessage());
		} catch (java.rmi.RemoteException re) {
			throw new FetaImportException(
					"Unable to call the describe method : " + re.getMessage());
		}

	}

	public Vector generateInputParameters(Document doc, URL endpoint)
			throws FetaImportException {
		try {

			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(endpoint.toString());

			// Get inputs
			call.setOperationName(new QName("getInputSpec"));
			Map inputs[] = (Map[]) call.invoke(new Object[0]);
			// Iterate over the inputs
			Element paramElement;
			Vector serviceInputs = new Vector();
			for (int i = 0; i < inputs.length; i++) {
				Map input_spec = inputs[i];
				paramElement = buildParameterDescription(doc, input_spec);
				serviceInputs.add(paramElement);
			}

			return serviceInputs;

		} catch (javax.xml.rpc.ServiceException se) {
			throw new FetaImportException(
					"Unable to create a new call to connect to soaplab, error was : "
							+ se.getMessage());
		} catch (java.rmi.RemoteException re) {
			throw new FetaImportException(
					"Unable to call the get spec method : " + re.getMessage());
		}

	}

	public Vector generateOutputParameters(Document doc, URL endpoint)
			throws FetaImportException {
		try {

			Call call = (Call) new Service().createCall();
			call.setTargetEndpointAddress(endpoint.toString());

			// Get inputs
			call.setOperationName(new QName("getResultSpec"));
			Map outputs[] = (Map[]) call.invoke(new Object[0]);
			// Iterate over the inputs

			Element paramElement;
			Vector serviceOutputs = new Vector();
			for (int i = 0; i < outputs.length; i++) {
				Map output_spec = outputs[i];
				paramElement = buildParameterDescription(doc, output_spec);
				serviceOutputs.add(paramElement);
			}

			return serviceOutputs;

		} catch (javax.xml.rpc.ServiceException se) {
			throw new FetaImportException(
					"Unable to create a new call to connect to soaplab, error was : "
							+ se.getMessage());
		} catch (java.rmi.RemoteException re) {
			throw new FetaImportException(
					"Unable to call the get spec method : " + re.getMessage());
		}

	}

	protected Element buildParameterDescription(Document doc, Map param_spec) {

		String input_name = (String) param_spec.get("name");
		String input_type = (String) param_spec.get("type");

		Element parameterElement = doc.createElement(FetaModelXSD.PARAMETER); // Create
																				// element

		Element parameterNameElement = doc
				.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																// element
		parameterNameElement.appendChild(doc.createTextNode(input_name));

		parameterElement.appendChild(parameterNameElement);

		Element parameterTransportTypeElement = doc
				.createElement(FetaModelXSD.TRANSPORT_DATATYPE);
		parameterTransportTypeElement.appendChild(doc
				.createTextNode(input_type));

		parameterElement.appendChild(parameterTransportTypeElement);

		if (param_spec.containsKey("semantic_type")) {
			Element semanticTypeElement = doc
					.createElement(FetaModelXSD.SEMANTIC_TYPE);
			semanticTypeElement.appendChild(doc
					.createTextNode((String) param_spec.get("semantic_type")));
			parameterElement.appendChild(semanticTypeElement);
		}
		if (param_spec.containsKey("default")) {
			Element defaultValueElement = doc
					.createElement(FetaModelXSD.DEFAULT_VALUE);
			defaultValueElement.appendChild(doc
					.createTextNode((String) param_spec.get("default")));
			parameterElement.appendChild(defaultValueElement);
		}

		// build a detailed description for the service
		String paramDesc = "";
		if (this.paramsDescriptions.containsKey(input_name)) {
			paramDesc = paramDesc + this.paramsDescriptions.get(input_name);
		}

		if (param_spec.containsKey("allowed_values")) {
			String[] allowedValues = (String[]) param_spec
					.get("allowed_values");
			paramDesc = paramDesc
					+ " The allowed values for this parameter are: ";
			for (int i = 0; i < allowedValues.length; i++) {
				paramDesc = paramDesc + (String) allowedValues[i] + ", ";
			}

		}
		// build a detailed description for the service

		Element parameterDescElement = doc
				.createElement(FetaModelXSD.PARAMETER_DESC);
		parameterDescElement.appendChild(doc.createTextNode(paramDesc));
		parameterElement.appendChild(parameterDescElement);

		return parameterElement;
	}

	public Map convert(String[] services) throws FetaImportException {
		Map conversionResults = new HashMap();

		try {

			for (int s = 0; s < services.length; s++) {

				// Generate a document for each SOAPLAB Service

				Document doc = new DocumentImpl();

				Element el_descriptions = doc
						.createElement(FetaModelXSD.SERVICE_DESCS); // Create
																	// top level
																	// element
				Element el_description = doc
						.createElement(FetaModelXSD.SERVICE_DESC); // Create
																	// element
				el_descriptions.appendChild(el_description);

				String serviceDescription = getDescription(new URL(sourceURL
						+ services[s]));
				Element el_serviceDescText = doc
						.createElement(FetaModelXSD.SERV_DESC_TEXT);
				el_serviceDescText.appendChild(doc
						.createTextNode(serviceDescription));
				el_description.appendChild(el_serviceDescText);

				// Create element that holds enumerated value of service type
				Element el_serviceType = doc
						.createElement(FetaModelXSD.SERVICE_TYPE);
				el_serviceType.appendChild(doc
						.createTextNode(ServiceType.SOAPLAB.toString()));
				el_description.appendChild(el_serviceType);

				Element el_serviceName = doc
						.createElement(FetaModelXSD.SERVICE_NAME);
				el_serviceName.appendChild(doc.createTextNode(services[s]));
				el_description.appendChild(el_serviceName);
				// System.out.println(services[s]);

				Element el_serviceLocation = doc
						.createElement(FetaModelXSD.LOCATION_URL);
				el_serviceLocation.appendChild(doc.createTextNode(sourceURL
						+ services[s]));
				el_description.appendChild(el_serviceLocation);

				Element el_serviceInterface = doc
						.createElement(FetaModelXSD.INTERFACE_WSDL);
				el_serviceInterface.appendChild(doc.createTextNode(sourceURL
						+ services[s] + "?wsdl"));
				el_description.appendChild(el_serviceInterface);

				Element el_organisation = doc
						.createElement(FetaModelXSD.ORGANISATION);
				Element el_organisationName = doc
						.createElement(FetaModelXSD.ORGANISATION_NAME);
				el_organisationName.appendChild(doc
						.createTextNode("European Bioinformatics Institute"));
				el_organisation.appendChild(el_organisationName);
				el_description.appendChild(el_organisation);

				Element el_operations = doc
						.createElement(FetaModelXSD.OPERATIONS);
				el_description.appendChild(el_operations);

				Element el_operation = doc
						.createElement(FetaModelXSD.SERVICE_OPERATION);
				el_operations.appendChild(el_operation);

				Element el_operationName = doc
						.createElement(FetaModelXSD.OPERATION_NAME);
				el_operationName.appendChild(doc.createTextNode(services[s]));
				el_operation.appendChild(el_operationName);

				if (fraggy != null) {
					Element el_operationSpec = doc
							.createElement(FetaModelXSD.OPERATION_SPEC); // Create
																			// element
					el_operationSpec.appendChild(doc.createTextNode(fraggy
							.getFragmentAsString()));
					el_operation.appendChild(el_operationSpec);
				}

				String operationDescription = serviceDescription
						+ getDetailedDescription(new URL(sourceURL
								+ services[s]));
				Element el_operationDesc = doc
						.createElement(FetaModelXSD.OPER_DESC_TEXT);
				el_operationDesc.appendChild(doc
						.createTextNode(operationDescription));
				el_operation.appendChild(el_operationDesc);

				Element el_operationInputs = doc
						.createElement(FetaModelXSD.OPERATION_INPUTS);
				Element el_operationOutputs = doc
						.createElement(FetaModelXSD.OPERATION_OUTPUTS);

				Vector serviceInputs = generateInputParameters(doc, new URL(
						sourceURL + services[s]));

				Element input;
				for (Iterator j = serviceInputs.iterator(); j.hasNext();) {
					input = (Element) j.next();
					el_operationInputs.appendChild(input);

				}
				Vector serviceOutputs = generateOutputParameters(doc, new URL(
						sourceURL + services[s]));
				Element output;
				for (Iterator j = serviceOutputs.iterator(); j.hasNext();) {
					output = (Element) j.next();
					el_operationOutputs.appendChild(output);

				}

				el_operation.appendChild(el_operationInputs);
				el_operation.appendChild(el_operationOutputs);

				doc.appendChild(el_descriptions);
				conversionResults.put(services[s].replaceAll(".", "-"), doc);

			}

		}

		catch (Exception exp) {
			exp.printStackTrace();
		}
		return conversionResults;
	}

	public static void main(String[] args) throws Exception {
		// arg 0 SOAPLAB SERVER URL
		// args 1 output path for files
		SoaplabToSkeletonConverter myConv;

		if (args.length >= 1) {

			try {
				java.net.URL wsdlURL = new java.net.URL(args[0]);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
				System.exit(1);
			}
			// new
			// SoaplabToSkeletonConverter("http://industry.ebi.ac.uk/soap/soaplab/");
			myConv = (args[0].endsWith("/") ? new SoaplabToSkeletonConverter(
					args[0]) : new SoaplabToSkeletonConverter(args[0] + "/"));
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
				.println("*****TO USE THE SOAPLAB IMPORTER****************************");
		System.out
				.println("*****Arg 1: A Valid URL for the SOAPLAB Installation********");
		System.out
				.println("*****Arg 2: (Optional) OutputPath for Generated Skeletons***");
		System.out
				.println("************************************************************");
	}

}
