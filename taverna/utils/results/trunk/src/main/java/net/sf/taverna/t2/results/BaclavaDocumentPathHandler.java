/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Map;

import net.sf.taverna.t2.baclava.DataThing;
import net.sf.taverna.t2.baclava.factory.DataThingFactory;
import net.sf.taverna.t2.baclava.factory.DataThingXMLFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Handles the loading and saving of DataBundle data as Baclava documents
 *
 * @author Stuart Owen
 * @author David Withers
 */
public class BaclavaDocumentPathHandler {

	private Map<String, Path> chosenReferences;

	private static Namespace namespace = Namespace.getNamespace("b",
			"http://org.embl.ebi.escience/baclava/0.1alpha");

	/**
	 * Reads a baclava document from an InputStream and returns a map of DataThings mapped to the portName
	 *
	 * @throws IOException, JDOMException
	 */
	public Map<String, DataThing> readData(InputStream inputStream)
			throws IOException, JDOMException {

		SAXBuilder builder = new SAXBuilder();
		Document inputDoc;
		inputDoc = builder.build(inputStream);

		return DataThingXMLFactory.parseDataDocument(inputDoc);
	}

	/**
	 * Saves the result data to an XML Baclava file.
	 *
	 * @throws IOException
	 */
	public void saveData(File file) throws IOException {
		// Build the string containing the XML document from the result map
		Document doc = getDataDocument();
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		PrintWriter out = new PrintWriter(new FileWriter(file));
		xo.output(doc, out);
	}

	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS
	 * containing the port's results.
	 */
	public Document getDataDocument() {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		// Build the DataThing map from the chosenReferences
		// First convert map of references to objects into a map of real result
		// objects
		for (String portName : getChosenReferences().keySet()) {
			DataThing thing = DataThingFactory.bake(getObjectForName(portName));
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", portName);
			dataThingElement.addContent(thing.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}

	protected Object getObjectForName(String name) {
		Object result = null;
		if (getChosenReferences().containsKey(name)) {
			try {
				result = ResultsUtils.convertPathToObject(getChosenReferences().get(name));
			} catch (IOException e) {
			}
		}
		if (result == null) {
			result = "null";
		}
		return result;
	}

	private Map<String, Path> getChosenReferences() {
		return chosenReferences;
	}

	public void setChosenReferences(Map<String, Path> chosenReferences) {
		this.chosenReferences = chosenReferences;
	}
}
