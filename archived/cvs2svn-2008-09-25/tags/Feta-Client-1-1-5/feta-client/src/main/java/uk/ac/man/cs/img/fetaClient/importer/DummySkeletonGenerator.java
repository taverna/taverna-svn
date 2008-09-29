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

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.man.cs.img.fetaClient.util.DOMUtil;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;

public class DummySkeletonGenerator {

	private String escapedSpecElementString;

	public DummySkeletonGenerator(String escapedSpecStr) {

		escapedSpecElementString = escapedSpecStr;
	}

	public DummySkeletonGenerator() {

		escapedSpecElementString = null;
	}

	public org.w3c.dom.Document generate() {

		try {

			Document doc = new DocumentImpl();

			Element el_descriptions = doc
					.createElement(FetaModelXSD.SERVICE_DESCS);
			doc.appendChild(el_descriptions);

			String serviceName = "Service name";
			String serviceDesc = "Service description";

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

			Element el_serviceDescText = doc
					.createElement(FetaModelXSD.SERV_DESC_TEXT);
			// Create element that will hold description of service
			el_serviceDescText.appendChild(doc.createTextNode(serviceDesc));

			el_description.appendChild(el_serviceName);
			el_description.appendChild(el_serviceDescText);

			Element el_operations = doc.createElement(FetaModelXSD.OPERATIONS); // Create
																				// element
			el_description.appendChild(el_operations);

			Element el_operation = doc
					.createElement(FetaModelXSD.SERVICE_OPERATION); // Create
																	// element
			el_operations.appendChild(el_operation);

			String operationName = "Operation name";
			Element el_operationName = doc
					.createElement(FetaModelXSD.OPERATION_NAME); // Create
																	// element
			el_operationName.appendChild(doc.createTextNode(operationName));
			el_operation.appendChild(el_operationName);

			if (escapedSpecElementString != null) {

				Element el_operationSpec = doc.createElement("operationSpec"); // Create
																				// element
				el_operationSpec.appendChild(doc
						.createTextNode(escapedSpecElementString));
				el_operation.appendChild(el_operationSpec);

			}

			el_operations.appendChild(el_operation);

			el_descriptions.appendChild(el_description);
			DOMUtil.writeOut(doc, "c:\\", "denemeeeee");
			return doc;
		}// try

		catch (Exception exp) {
			exp.printStackTrace();

			return null;

		}// catch

	}

}
