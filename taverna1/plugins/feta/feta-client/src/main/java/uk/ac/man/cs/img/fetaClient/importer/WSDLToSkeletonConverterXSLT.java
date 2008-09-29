/*
 * Created on Jan 26, 2004
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class WSDLToSkeletonConverterXSLT {

	private String xmlSourceURL;

	private String xslURL;

	private String xmlOutputPath;

	public WSDLToSkeletonConverterXSLT(String XMLSourceURL, String XSLURL,
			String XMLOutputPath) {

		xmlSourceURL = XMLSourceURL;
		xslURL = XSLURL;
		xmlOutputPath = XMLOutputPath;
	}

	/* Convert the WSDL file to a skeleton XML file using an XSLT Script */

	public void convert() throws TransformerException,
			TransformerConfigurationException, FileNotFoundException,
			IOException {
		{

			TransformerFactory tFactory = TransformerFactory.newInstance();

			Transformer transformer = tFactory.newTransformer(new StreamSource(
					this.xslURL));

			// Use the Transformer to apply the associated Templates object to
			// an XML document
			// and write the output to an XML document
			transformer.transform(new StreamSource(xmlSourceURL),
					new StreamResult(new FileOutputStream(xmlOutputPath)));

			System.out.println("************* The result is in "
					+ xmlOutputPath + "**************");
		}
	}

}
