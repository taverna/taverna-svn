/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view.test;

import java.net.URL;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;

/**
 * Attempts to load data into a ScuflModel from the same source that the
 * XScuflParserTest uses, then print out the XScufl text from the XScuflView. In
 * an ideal world these would be the same, subject to XML parsing (esp.
 * whitespace).
 * 
 * @author Tom Oinn
 */
public class XScuflViewTest {

	public static void main(String args[]) throws Exception {

		ScuflModel model = new ScuflModel();
		XScuflView view = new XScuflView(model);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL location = loader
				.getResource("org/embl/ebi/escience/scufl/parser/test/XScufl_example.xml");
		System.out.println("Loading definition from : " + location.toString());
		XScuflParser.populate(location.openStream(), model, null);
		System.out.println(view.getXMLText());

	}

}
