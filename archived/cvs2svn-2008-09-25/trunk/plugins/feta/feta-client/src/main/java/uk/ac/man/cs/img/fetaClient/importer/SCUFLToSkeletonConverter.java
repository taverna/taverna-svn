/*
 * Created on Nov 07, 2004
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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.xerces.dom.DocumentImpl;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.DnDFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WORKFLOWFragment;
import uk.ac.man.cs.img.fetaClient.util.DOMUtil;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

public class SCUFLToSkeletonConverter {

	private String scuflURL;

	private ScuflModel workflowModel = null;

	private DnDFragment fraggy;

	public SCUFLToSkeletonConverter(String scuflURL, ScuflModel model) {

		try {
			this.scuflURL = scuflURL;
			if (model == null)
				model = new ScuflModel();
			XScuflParser.populate(new URL(scuflURL).openStream(), model, null);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}

	public SCUFLToSkeletonConverter(InputStream scuflStream, ScuflModel model) {

		try {
			this.scuflURL = null;
			XScuflParser.populate(scuflStream, model, null);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}

	public SCUFLToSkeletonConverter(WORKFLOWFragment specElement,
			ScuflModel model) {

		this(specElement.getWorkflowLoc(), model);
		fraggy = specElement;
	}

	public Map convert() throws FetaImportException {
		try {

			Port[] outputs = this.workflowModel.getWorkflowSinkPorts();
			Port[] inputs = this.workflowModel.getWorkflowSourcePorts();

			Map conversionResults = new HashMap();

			Document doc = new DocumentImpl();

			Element el_descriptions = doc
					.createElement(FetaModelXSD.SERVICE_DESCS);

			Element el_description = doc
					.createElement(FetaModelXSD.SERVICE_DESC);

			String workflowName = this.workflowModel.getDescription()
					.getTitle();
			Element el_serviceName = doc
					.createElement(FetaModelXSD.SERVICE_NAME);
			el_serviceName.appendChild(doc.createTextNode(workflowName));

			String workflowDesc = this.workflowModel.getDescription().getText();
			Element el_description_text = doc
					.createElement(FetaModelXSD.SERV_DESC_TEXT);
			el_description_text.appendChild(doc.createTextNode(workflowDesc));

			Element el_serviceLoc = doc
					.createElement(FetaModelXSD.INTERFACE_WSDL);
			// Create element that holds the interface description document
			// location
			el_serviceLoc.appendChild(doc.createTextNode(this.scuflURL));

			Element el_serviceType = doc
					.createElement(FetaModelXSD.SERVICE_TYPE);
			// Create element
			el_serviceType.appendChild(doc.createTextNode(ServiceType.WORKFLOW
					.toString()));

			el_description.appendChild(el_serviceName);
			el_description.appendChild(el_serviceLoc);
			el_description.appendChild(el_description_text);
			el_description.appendChild(el_serviceType);

			el_descriptions.appendChild(el_description);

			doc.appendChild(el_descriptions);

			Element el_operations = doc.createElement(FetaModelXSD.OPERATIONS); // Create
																				// element
			el_description.appendChild(el_operations);

			Element el_operation = doc
					.createElement(FetaModelXSD.SERVICE_OPERATION); // Create
																	// element
			el_operations.appendChild(el_operation);

			Element el_operationName = doc
					.createElement(FetaModelXSD.OPERATION_NAME); // Create
																	// element
			el_operationName.appendChild(doc.createTextNode(workflowName));
			el_operation.appendChild(el_operationName);

			if (fraggy != null) {
				Element el_operationSpec = doc
						.createElement(FetaModelXSD.OPERATION_SPEC); // Create
																		// element
				el_operationSpec.appendChild(doc.createTextNode(fraggy
						.getFragmentAsString()));
				el_operation.appendChild(el_operationSpec);
			}

			Element el_operationInputs = doc
					.createElement(FetaModelXSD.OPERATION_INPUTS);
			Element el_operationOutputs = doc
					.createElement(FetaModelXSD.OPERATION_OUTPUTS);

			Vector serviceInputs = generateParameters(doc, inputs);

			Element input;
			for (Iterator j = serviceInputs.iterator(); j.hasNext();) {
				input = (Element) j.next();
				el_operationInputs.appendChild(input);

			}
			Vector serviceOutputs = generateParameters(doc, outputs);
			Element output;
			for (Iterator j = serviceOutputs.iterator(); j.hasNext();) {
				output = (Element) j.next();
				el_operationOutputs.appendChild(output);

			}

			el_operation.appendChild(el_operationInputs);
			el_operation.appendChild(el_operationOutputs);

			conversionResults.put(workflowName, doc);

			return conversionResults;
		}// try

		catch (Exception exp) {
			exp.printStackTrace();
			throw new FetaImportException("Exception occured: "
					+ exp.getMessage());

		}// catch

	}

	public Vector generateParameters(Document doc, Port[] ports) {

		Element paramElement;
		Vector wfPorts = new Vector();
		for (int i = 0; i < ports.length; i++) {
			paramElement = buildParameterDescription(doc, ports[i]);
			wfPorts.add(paramElement);
		}
		return wfPorts;
	}

	protected Element buildParameterDescription(Document doc, Port port) {

		String paramName = port.toString();
		String syntacticType = port.getSyntacticType();
		String semanticType = port.getMetadata().getSemanticType();
		String description = port.getMetadata().getDescription();
		// this is all we can get from the taverna's Port object to be used in
		// the Feta description

		Element parameterElement = doc.createElement(FetaModelXSD.PARAMETER); // Create
																				// element

		Element parameterNameElement = doc
				.createElement(FetaModelXSD.PARAMETER_NAME); // Create
																// element
		parameterNameElement.appendChild(doc
				.createTextNode((paramName == null ? " " : paramName)));

		parameterElement.appendChild(parameterNameElement);

		if (syntacticType != null) {
			Element parameterTransportTypeElement = doc
					.createElement(FetaModelXSD.TRANSPORT_DATATYPE);
			parameterTransportTypeElement.appendChild(doc
					.createTextNode(syntacticType));
			parameterElement.appendChild(parameterTransportTypeElement);
		}

		if (semanticType != null) {
			Element semanticTypeElement = doc
					.createElement(FetaModelXSD.SEMANTIC_TYPE);
			semanticTypeElement.appendChild(doc.createTextNode(semanticType));
			parameterElement.appendChild(semanticTypeElement);
		}
		if (description != null) {
			Element descElement = doc
					.createElement(FetaModelXSD.PARAMETER_DESC);
			descElement.appendChild(doc.createTextNode(description));
			parameterElement.appendChild(descElement);
		}

		return parameterElement;
	}

	public static void main(String[] args) throws Exception {

		// args 0 scufl url
		// args 1 output path for files

		SCUFLToSkeletonConverter myConv;

		if (args.length >= 1) {

			try {
				java.net.URL wsdlURL = new java.net.URL(args[0]);

			} catch (Exception e) {
				e.printStackTrace();
				printUsage();
				System.exit(1);
			}

			myConv = new SCUFLToSkeletonConverter(args[0], null);
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
			System.exit(0);
		} else {
			printUsage();
			System.exit(1);
		}

	}

	private static void printUsage() {

		System.out
				.println("***********************************************************");
		System.out
				.println("*****TO USE THE SCUFL IMPORTER*****************************");
		System.out
				.println("*****Arg 1: A Valid URL for the SCUFL**********************");
		System.out
				.println("*****Arg 2: (Optional) OutputPath for Generated Skeleton***");
		System.out
				.println("***********************************************************");
	}

}
