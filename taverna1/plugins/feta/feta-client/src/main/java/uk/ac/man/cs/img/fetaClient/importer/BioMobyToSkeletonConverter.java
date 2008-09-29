/*
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.xerces.dom.DocumentImpl;
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.BIOMOBYFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.DnDFragment;
import uk.ac.man.cs.img.fetaClient.util.DOMUtil;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

public class BioMobyToSkeletonConverter implements IImporter {

	private String mobyCentralURL;

	private String serviceName;

	private String authorityName;

	private DnDFragment fraggy;

	public BioMobyToSkeletonConverter(String MobyCentralURL) {

		this.mobyCentralURL = MobyCentralURL;
		this.serviceName = null;
		this.authorityName = null;

	}

	public BioMobyToSkeletonConverter(String MobyCentralURL,
			String AuthorityName) {
		this.mobyCentralURL = MobyCentralURL;
		this.serviceName = null;
		this.authorityName = AuthorityName;
	}

	public BioMobyToSkeletonConverter(String MobyCentralURL,
			String AuthorityName, String ServiceName) {
		this.mobyCentralURL = MobyCentralURL;
		this.serviceName = ServiceName;
		this.authorityName = AuthorityName;
	}

	public BioMobyToSkeletonConverter(BIOMOBYFragment specElement) {
		fraggy = specElement;
		this.mobyCentralURL = specElement.getMobyCENTRALLoc();
		this.serviceName = specElement.getServiceName();
		this.authorityName = specElement.getAuthorityName();
	}

	public Map convert() throws FetaImportException {
		Map conversionResults = new HashMap();
		try {

			Central worker = new CentralImpl(mobyCentralURL);
			Map names = worker.getServiceNames();

			Hashtable byAuthority = new Hashtable();
			for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String serviceName = (String) entry.getKey();
				String authorityName = (String) entry.getValue();
				Vector services;
				if (byAuthority.containsKey(authorityName))
					services = (Vector) byAuthority.get(authorityName);
				else
					services = new Vector();
				services.addElement(serviceName);
				byAuthority.put(authorityName, services);
			}

			for (Enumeration en = byAuthority.keys(); en.hasMoreElements();) {
				String AuthorityName = (String) en.nextElement();
				if (authorityName != null) {
					if (!AuthorityName.equalsIgnoreCase(authorityName))
						continue;
				}

				Vector v = (Vector) byAuthority.get(AuthorityName);

				for (Enumeration en2 = v.elements(); en2.hasMoreElements();) {

					Document doc = new DocumentImpl();
					Element el_descriptions = doc
							.createElement(FetaModelXSD.SERVICE_DESCS);
					doc.appendChild(el_descriptions);

					String ServiceName = (String) en2.nextElement();
					// System.out.println("Authority name" +
					// AuthorityName+"Service Name"+serviceName );

					MobyService pattern = new MobyService(ServiceName);
					pattern.setAuthority(AuthorityName);
					MobyService[] services = worker.findService(pattern);

					if (services == null || services.length == 0) {
						continue;
					} else if ((serviceName != null)
							&& (!serviceName.equalsIgnoreCase(ServiceName))) {
						continue;
					} else {

						// Element Declarations for the Service
						// Generate a SERVICE ELEMENT for each service
						// decription in the WSDL file
						Element el_description = doc
								.createElement(FetaModelXSD.SERVICE_DESC);
						// Create element

						Element el_serviceName = doc
								.createElement(FetaModelXSD.SERVICE_NAME);
						// Create elementthat holds name which is the local part
						// of the qualified name
						el_serviceName.appendChild(doc
								.createTextNode(ServiceName));

						Element el_serviceLoc = doc
								.createElement(FetaModelXSD.LOCATION_URL);
						// Create element that holds location URL
						el_serviceLoc.appendChild(doc
								.createTextNode(services[0].getURL()));

						Element el_serviceIntWSDL = doc
								.createElement(FetaModelXSD.INTERFACE_WSDL);
						// Create element that holds the WSDL location
						el_serviceIntWSDL.appendChild(doc
								.createTextNode(mobyCentralURL));

						Element el_serviceType = doc
								.createElement(FetaModelXSD.SERVICE_TYPE); // Create
																			// element
						el_serviceType
								.appendChild(doc
										.createTextNode(ServiceType.BIOMOBY
												.toString()));
						// Create element that holds enumerated value of service
						// type

						el_description.appendChild(el_serviceName);
						el_description.appendChild(el_serviceLoc);
						el_description.appendChild(el_serviceIntWSDL);

						el_description.appendChild(el_serviceType);

						el_descriptions.appendChild(el_description);

						// Element Declarations for operations of the Service

						Element el_operations = doc
								.createElement(FetaModelXSD.OPERATIONS); // Create
																			// element
						el_description.appendChild(el_operations);

						Element el_operation = doc
								.createElement(FetaModelXSD.SERVICE_OPERATION); // Create
																				// element
						el_operations.appendChild(el_operation);

						Element el_operationName = doc
								.createElement(FetaModelXSD.OPERATION_NAME); // Create
																				// element
						el_operationName.appendChild(doc
								.createTextNode(ServiceName));
						el_operation.appendChild(el_operationName);

						if (fraggy != null) {
							Element el_operationSpec = doc
									.createElement(FetaModelXSD.OPERATION_SPEC); // Create
																					// element
							el_operationSpec.appendChild(doc
									.createTextNode(fraggy
											.getFragmentAsString()));
							el_operation.appendChild(el_operationSpec);
						}

						Element el_serviceDescText = doc
								.createElement(FetaModelXSD.SERV_DESC_TEXT); // Create
																				// element
						el_serviceDescText.appendChild(doc
								.createTextNode(services[0].getDescription()));

						el_operation.appendChild(generateInputs(doc,
								services[0]));
						el_operation.appendChild(generateOutputs(doc,
								services[0]));
						el_description.appendChild(el_serviceDescText);

					}

					conversionResults.put(AuthorityName + "-" + ServiceName,
							doc);
				}
			}

			return conversionResults;
		}// try

		catch (Exception exp) {
			throw new FetaImportException("Exception occured: "
					+ exp.getMessage());

		}// catch

	}

	public Element generateInputs(Document doc, MobyService mobyService) {
		Element el_operationInputs = doc
				.createElement(FetaModelXSD.OPERATION_INPUTS); // Create
																// element

		MobyData[] inputs = mobyService.getPrimaryInputs();
		for (int i = 0; i < inputs.length; i++) {
			MobyData inp = inputs[i];
			Element el_parameter = doc.createElement(FetaModelXSD.PARAMETER); // Create
																				// element
			Element parameterName = doc
					.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																	// element
			parameterName.appendChild(doc.createTextNode(inp.getName()));
			el_parameter.appendChild(parameterName);
			el_operationInputs.appendChild(el_parameter);
		}

		MobyData[] inputs2 = mobyService.getSecondaryInputs();
		for (int j = 0; j < inputs2.length; j++) {
			MobyData inp = inputs2[j];
			Element el_parameter = doc.createElement(FetaModelXSD.PARAMETER); // Create
																				// element
			Element parameterName = doc
					.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																	// element
			parameterName.appendChild(doc.createTextNode(inp.getName()));
			el_parameter.appendChild(parameterName);
			el_operationInputs.appendChild(el_parameter);
		}

		return el_operationInputs;
	}

	public Element generateOutputs(Document doc, MobyService mobyService) {
		// outputs
		Element el_operationOutputs = doc
				.createElement(FetaModelXSD.OPERATION_OUTPUTS); // Create
																// element
		MobyData[] Outputs = mobyService.getPrimaryOutputs();
		for (int i = 0; i < Outputs.length; i++) {
			MobyData inp = Outputs[i];
			Element el_parameter = doc.createElement(FetaModelXSD.PARAMETER); // Create
																				// element
			Element parameterName = doc
					.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																	// element
			parameterName.appendChild(doc.createTextNode(inp.getName()));
			el_parameter.appendChild(parameterName);
			el_operationOutputs.appendChild(el_parameter);
		}

		return el_operationOutputs;
	}

	public static void main(String[] args) throws Exception {
		// arg 0 moby central location
		// arg 1 output path for files
		// "http://mobycentral.cbr.nrc.ca/cgi-bin/MOBY05/mobycentral.pl"
		BioMobyToSkeletonConverter myConv;
		if (args.length >= 1) {

			try {
				java.net.URL mobyURL = new java.net.URL(args[0]);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
				System.exit(1);
			}

			myConv = new BioMobyToSkeletonConverter(args[0]);
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
				.println("*************************************************************");
		System.out
				.println("*****TO USE THE BIMOBY IMPORTER******************************");
		System.out
				.println("*****Arg 1: A Valid URL for the MOBYCentral Registry ********");
		System.out
				.println("*****Arg 2: (Optional) OutputPath for Generated Skeletons****");
		System.out
				.println("*************************************************************");
	}
}
