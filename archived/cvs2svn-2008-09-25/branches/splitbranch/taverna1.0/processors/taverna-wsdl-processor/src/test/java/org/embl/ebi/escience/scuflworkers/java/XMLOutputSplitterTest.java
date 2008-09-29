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
 * Filename           $RCSfile: XMLOutputSplitterTest.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-07 13:57:18 $
 *               by   $Author: sowen70 $
 * Created on 16-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.java;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XMLOutputSplitterTest extends TestCase {

	public void testSplitter() throws Exception {
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eInfo");
		splitter.setUpOutputs(processor.getOutputPorts()[1]);

		assertEquals("wrong number of outputs", 3, splitter.outputNames().length);
		assertEquals("wrong number of outputs types", 3, splitter.outputTypes().length);

		assertEquals("wrong name", "ERROR", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/plain'", splitter.outputTypes()[0]);

		assertEquals("wrong name", "DbList", splitter.outputNames()[1]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[1]);

		assertEquals("wrong name", "DbInfo", splitter.outputNames()[2]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[2]);

		assertEquals("wrong number of inputs", 1, splitter.inputNames().length);
		assertEquals("wrong name", "input", splitter.inputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.inputTypes()[0]);

		Map inputMap = new HashMap();
		inputMap
				.put(
						"input",
						new DataThing(
								"<eInfoResult><ERROR>error text</ERROR><DbList><list><item /></list></DbList><DbInfo><info>some info</info></DbInfo></eInfoResult>"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("ERROR");
		assertNotNull("ERROR missing from output Map", outputThing);
		assertEquals("wrong type for ERROR", String.class, outputThing.getDataObject().getClass());
		String outputString = outputThing.getDataObject().toString();
		assertEquals("output ERROR is incorrect", "error text", outputString);

		outputThing = (DataThing) outputMap.get("DbList");
		assertNotNull("DbList missing from output Map", outputThing);
		assertEquals("wrong type for DbList", String.class, outputThing.getDataObject().getClass());
		outputString = outputThing.getDataObject().toString();
		assertEquals("output DbList is incorrect", "<DbList><list><item /></list></DbList>", outputString);

		outputThing = (DataThing) outputMap.get("DbInfo");
		assertNotNull("DbInfo missing from output Map", outputThing);
		assertEquals("wrong type for DbInfo", String.class, outputThing.getDataObject().getClass());
		outputString = outputThing.getDataObject().toString();
		assertEquals("output DbInfo is incorrect", "<DbInfo><info>some info</info></DbInfo>", outputString);
	}

	public void testProvideXML() throws Exception {
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		ScuflModel model = new ScuflModel();
		WSDLBasedProcessor processor = new WSDLBasedProcessor(model, "testProc",
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl", "run_eInfo");
		splitter.setUpOutputs(processor.getOutputPorts()[1]);
		Element element = splitter.provideXML();
		String xml = new XMLOutputter().outputString(element);

		assertEquals(eInfoProcessorXML(), xml);
	}

	public void testConsumeXML() throws Exception {
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(eInfoProcessorXML())).getRootElement());

		assertEquals("wrong number of outputs", 3, splitter.outputNames().length);
		assertEquals("wrong number of outputs types", 3, splitter.outputTypes().length);

		assertEquals("wrong name", "ERROR", splitter.outputNames()[0]);
		assertEquals("wrong type", "'text/plain'", splitter.outputTypes()[0]);

		assertEquals("wrong name", "DbList", splitter.outputNames()[1]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[1]);

		assertEquals("wrong name", "DbInfo", splitter.outputNames()[2]);
		assertEquals("wrong type", "'text/xml'", splitter.outputTypes()[2]);

		assertEquals("wrong number of inputs", 1, splitter.inputNames().length);
		assertEquals("wrong name", "input", splitter.inputNames()[0]);
		assertEquals("wrong type", "'text/xml'", splitter.inputTypes()[0]);

		Map inputMap = new HashMap();
		inputMap
				.put(
						"input",
						new DataThing(
								"<eInfoResult><ERROR>error text</ERROR><DbList><list><item /></list></DbList><DbInfo><info>some info</info></DbInfo></eInfoResult>"));

		Map outputMap = splitter.execute(inputMap);

		DataThing outputThing = (DataThing) outputMap.get("ERROR");
		assertNotNull("ERROR missing from output Map", outputThing);
		assertEquals("wrong type for ERROR", String.class, outputThing.getDataObject().getClass());
		String outputString = outputThing.getDataObject().toString();
		assertEquals("output ERROR is incorrect", "error text", outputString);

		outputThing = (DataThing) outputMap.get("DbList");
		assertNotNull("DbList missing from output Map", outputThing);
		assertEquals("wrong type for DbList", String.class, outputThing.getDataObject().getClass());
		outputString = outputThing.getDataObject().toString();
		assertEquals("output DbList is incorrect", "<DbList><list><item /></list></DbList>", outputString);

		outputThing = (DataThing) outputMap.get("DbInfo");
		assertNotNull("DbInfo missing from output Map", outputThing);
		assertEquals("wrong type for DbInfo", String.class, outputThing.getDataObject().getClass());
		outputString = outputThing.getDataObject().toString();
		assertEquals("output DbInfo is incorrect", "<DbInfo><info>some info</info></DbInfo>", outputString);
	}

	public void testArraysBasic() throws Exception {
		String splitterXML = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:arraytype optional=\"true\" unbounded=\"true\" typename=\"string\" name=\"DbName\"><s:elementtype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"\" /></s:elementtype></s:arraytype></s:extensions>";
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(splitterXML)).getRootElement());

		assertEquals(splitter.outputTypes()[0], "l('text/plain')");
		assertEquals(splitter.outputNames()[0], "DbName");

		Map inputMap = new HashMap();
		inputMap.put("input", new DataThing(
				"<DbName><item>item 1</item><item>item 2</item><item>item 3</item></DbName>"));

		Map outputMap = splitter.execute(inputMap);

		assertNotNull("no output found for DbName", outputMap.get("DbName"));
		DataThing thingy = (DataThing) outputMap.get("DbName");

		assertEquals("Expecting ArrayList as datatype", ArrayList.class, thingy.getDataObject().getClass());
		ArrayList elements = (ArrayList) thingy.getDataObject();

		assertEquals("3 elements were expected", 3, elements.size());
		assertEquals("unexpected value", "item 1", elements.get(0));
		assertEquals("unexpected value", "item 2", elements.get(1));
		assertEquals("unexpected value", "item 3", elements.get(2));
	}

	public void testArraysComplex() throws Exception {
		String splitterXML = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:arraytype optional=\"true\" unbounded=\"true\" typename=\"string\" name=\"People\"><s:elementtype><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"\" ><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Name\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"Age\" /></s:elements></s:complextype></s:elementtype></s:arraytype></s:extensions>";
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(splitterXML)).getRootElement());

		assertEquals(splitter.outputTypes()[0], "l('text/xml')");
		assertEquals(splitter.outputNames()[0], "People");

		Map inputMap = new HashMap();
		String dataXML = "<People><item><Name>bob</Name><Age>37</Age></item><item><Name>mary</Name><Age>57</Age></item></People>";
		inputMap.put("input", new DataThing(dataXML));

		Map outputMap = splitter.execute(inputMap);

		assertNotNull("no output found for DbName", outputMap.get("People"));
		DataThing thingy = (DataThing) outputMap.get("People");

		assertEquals("Expecting ArrayList as datatype", ArrayList.class, thingy.getDataObject().getClass());
		ArrayList elements = (ArrayList) thingy.getDataObject();

		assertEquals("2 elements were expected", 2, elements.size());
		assertEquals("unexpected value", "<item><Name>bob</Name><Age>37</Age></item>", elements.get(0));
		assertEquals("unexpected value", "<item><Name>mary</Name><Age>57</Age></item>", elements.get(1));
	}

	public void testEmptyOutputs() throws Exception {
		// missing optional outputs are populated with an empty string
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(eInfoProcessorXML())).getRootElement());

		Map inputMap = new HashMap();
		inputMap.put("input", new DataThing("<eInfoResult><DbInfo><info>some info</info></DbInfo></eInfoResult>"));

		Map outputMap = splitter.execute(inputMap);
		assertNotNull(outputMap.get("DbList"));
		DataThing thingy = (DataThing) outputMap.get("DbList");
		assertEquals("<DbList />", thingy.getDataObject().toString());

		assertNotNull(outputMap.get("ERROR"));
		thingy = (DataThing) outputMap.get("ERROR");
		assertEquals("", thingy.getDataObject().toString());
	}

	public void testEmptyOutputsForArray() throws Exception {
		String procXML = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"eInfoResult\" name=\"parameters\"><s:elements><s:arraytype optional=\"true\" unbounded=\"true\" typename=\"arrayofstring\" name=\"AnArray\"><s:elementtype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"\" /></s:elementtype></s:arraytype></s:elements></s:complextype></s:extensions>";
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.consumeXML(new SAXBuilder().build(new StringReader(procXML)).getRootElement());

		assertEquals("wrong number of outputs", 1, splitter.outputNames().length);
		assertEquals("wrong output name", "AnArray", splitter.outputNames()[0]);
		assertEquals("wrong output type, should be array", "l('text/plain')", splitter.outputTypes()[0]);
		Map inputMap = new HashMap();
		inputMap.put("input", new DataThing("<eInfoResult />"));

		Map outputMap = splitter.execute(inputMap);

		assertNotNull(outputMap.get("AnArray"));
		DataThing thingy = (DataThing) outputMap.get("AnArray");
		assertTrue(thingy.getDataObject() instanceof List);

		List list = (List) thingy.getDataObject();
		assertEquals(0, list.size());

	}

	private String eInfoProcessorXML() {
		return "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"eInfoResult\" name=\"parameters\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}eInfoResult\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"ERROR\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:complextype optional=\"true\" unbounded=\"false\" typename=\"DbListType\" name=\"DbList\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}DbListType\"><s:elements><s:arraytype optional=\"true\" unbounded=\"true\" typename=\"string\" name=\"DbName\" qname=\"{http://www.w3.org/2001/XMLSchema}string[0,unbounded]\"><s:elementtype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elementtype></s:arraytype></s:elements></s:complextype><s:complextype optional=\"true\" unbounded=\"false\" typename=\"DbInfoType\" name=\"DbInfo\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}DbInfoType\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"DbName\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"MenuName\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Description\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Count\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"LastUpdate\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:arraytype optional=\"false\" unbounded=\"false\" typename=\"FieldListType\" name=\"FieldList\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}FieldListType\"><s:elementtype><s:complextype optional=\"false\" unbounded=\"false\" typename=\"FieldType\" name=\"\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}FieldType\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Name\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Description\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"TermCount\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"IsDate\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"IsNumerical\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"SingleToken\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Hierarchy\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elements></s:complextype></s:elementtype></s:arraytype><s:arraytype optional=\"true\" unbounded=\"false\" typename=\"LinkListType\" name=\"LinkList\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}LinkListType\"><s:elementtype><s:complextype optional=\"false\" unbounded=\"false\" typename=\"LinkType\" name=\"\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/einfo}LinkType\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Name\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Menu\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"Description\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"DbTo\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elements></s:complextype></s:elementtype></s:arraytype></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
	}

}
