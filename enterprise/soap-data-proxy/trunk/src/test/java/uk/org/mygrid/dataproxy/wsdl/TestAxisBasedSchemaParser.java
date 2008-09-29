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
 * Filename           $RCSfile: TestAxisBasedSchemaParser.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.wsdl.impl.AxisBasedSchemaParserImpl;

public class TestAxisBasedSchemaParser {

	WSDLParser parser;
	@Before
	public void setUp() {
		parser=new AxisBasedSchemaParserImpl();
	}	
	
	@Test
	public void testOperations() throws Exception {
		String wsdl="http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/gominer/GMService.wsdl";
		List<Element> operations = parser.parseOperations(wsdl);		
		assertEquals("There should be 1 operation found",operations.size(), 1);
		Element el = operations.get(0);
		assertEquals("Root should be operation","operation",el.getName());
		Element name = el.element("name");
		assertNotNull("There should be child element called name",name);
		assertEquals("Operation name should be 'getReport'","getReport",name.getTextTrim());
		Element elements = el.element("elements");
		assertNotNull("There should be child element called elements",name);
		assertEquals("There should be only 1 element within the elements tag",1,elements.elements().size());
		Element element = (Element)elements.elements().get(0);
		
		assertEquals("There should be only 1 element within the element tag",1,elements.elements().size());
		Element getReportResponse = (Element)element.elements().get(0);	
		
		
		assertEquals("The response type for getReport should be getReportResponse","getReportResponse",getReportResponse.getName());
		assertEquals("name attribute should be parameters","parameters",getReportResponse.attribute("name").getText());
		assertEquals("Incorrect namespace","http://webservice.gominer.lmp.nci.nih.gov",getReportResponse.getNamespaceURI());
	}
	
	@Test
	public void testExpand() throws Exception {
		String wsdl="http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/gominer/GMService.wsdl";
		List<Element> operations = parser.parseOperations(wsdl);
		Element el = operations.get(0);
		Element getReportResponse = el.element("elements").element("element").element("getReportResponse");
		assertEquals("There should be no children before expansion",0,getReportResponse.elements().size());
		Element expanded = parser.expandType(wsdl, getReportResponse);
		assertEquals("There should be 1 child after expansion",1,expanded.elements().size());
		
		Element child = (Element)expanded.elements().get(0);
		
		assertEquals("Expanded type should be string","string",child.getName());
		assertEquals("Name should be out","out",child.attribute("name").getText());
		assertEquals("Incorrect namespace","http://webservice.gominer.lmp.nci.nih.gov",child.getNamespaceURI());	
	}
	
	@Test (expected=WSDLParsingException.class)
	public void testRepeatedExpand() throws Exception {
		String wsdl="http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/gominer/GMService.wsdl";
		List<Element> operations = parser.parseOperations(wsdl);
		Element el = operations.get(0);
		Element getReportResponse = el.element("elements").element("element").element("getReportResponse");
		assertEquals("There should be no children before expansion",0,getReportResponse.elements().size());
		Element expanded = parser.expandType(wsdl, getReportResponse);
		assertEquals("There should be 1 child after expansion",1,expanded.elements().size());
		
		expanded = parser.expandType(wsdl, expanded);						
	}
	
	@Test
	public void testForImports() throws Exception {
		String wsdl = "http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/eutils/eutils_lite.wsdl";
		List<Element> operations = parser.parseOperations(wsdl);
		assertEquals("There should be 12 operations",12,operations.size());
		
		Element eInfo=operations.get(2);
		Element name = eInfo.element("name");
		assertEquals("Operation should be run_eInfo","run_eInfo",name.getTextTrim());
		assertEquals("There should be 1 response type",1,eInfo.element("elements").element("element").elements().size());
		Element eInfoResult = (Element)eInfo.element("elements").element("element").elements().get(0);
		
		assertEquals("Incorrect type","eInfoResult",eInfoResult.getName());
		assertEquals("Incorrect name","parameters",eInfoResult.attribute("name").getText());
		assertEquals("Incorrect namespace","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo",eInfoResult.getNamespaceURI());
		
		Element expanded = parser.expandType(wsdl, eInfoResult);
		
		assertEquals("There should be 3 inner elements after expansion",3,expanded.elements().size());
		
		Element inner = expanded.element("string");
		assertNotNull("There should be an element string",inner);
		assertEquals("Incorrect tagname","string",inner.getName());
		assertEquals("Incorrect name","ERROR",inner.attribute("name").getText());
		assertEquals("Incorrect namespace","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo",inner.getNamespaceURI());		
		
		inner = expanded.element("DbListType");
		assertNotNull("There should be an element DbListType",inner);
		assertEquals("Incorrect tagname","DbListType",inner.getName());
		assertEquals("Incorrect name","DbList",inner.attribute("name").getText());
		assertEquals("Incorrect namespace","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo",inner.getNamespaceURI());
		
		inner = expanded.element("DbInfoType");
		assertNotNull("There should be an element DbInfoType",inner);
		assertEquals("Incorrect tagname","DbInfoType",inner.getName());
		assertEquals("Incorrect name","DbInfo",inner.attribute("name").getText());
		assertEquals("Incorrect namespace","http://www.ncbi.nlm.nih.gov/soap/eutils/einfo",inner.getNamespaceURI());
		
		expanded = parser.expandType(wsdl, inner);
		assertEquals("Expanded DbInfoType should have 7 children",7,expanded.elements().size());
		
		assertEquals("There should be 5 elements of type string",5,expanded.elements("string").size());
		assertNotNull("There should be an element for type FieldListType",expanded.element("FieldListType"));
		assertNotNull("There should be an element for type LinkListType",expanded.element("LinkListType"));		
	}		
}
