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
package uk.ac.man.cs.img.fetaClient.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class DOMUtil {

	public static void writeOut(Document doc, String outputPath,
			String serviceNiceName) {
		try {
			OutputFormat format = new OutputFormat(doc,
					OutputFormat.Defaults.Encoding, true); // Serialize DOM
			FileWriter fw = new FileWriter(new File(outputPath, serviceNiceName
					+ ".xml"));
			XMLSerializer serial = new XMLSerializer(fw, format);
			serial.asDOMSerializer(); // As a DOM Serializer
			serial.serialize(doc.getDocumentElement());
		} catch (IOException ioe) {
			System.out.println("Problem writing XML file" + ioe.getMessage());
		}
	}

	public static XMLSerializer getSerializerForDoc(Document doc, Writer wr) {
		try {
			OutputFormat format = new OutputFormat(doc,
					OutputFormat.Defaults.Encoding, true); // Serialize DOM
			XMLSerializer serial = new XMLSerializer(wr, format);
			serial.asDOMSerializer(); // As a DOM Serializer
			return serial;
		} catch (IOException ioe) {
			System.out
					.println("Problem creating a serializer for the given document and writer"
							+ ioe.getMessage());
			return null;
		}
	}

}
