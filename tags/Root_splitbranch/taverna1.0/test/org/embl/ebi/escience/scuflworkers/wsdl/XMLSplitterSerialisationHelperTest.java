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
 * Filename           $RCSfile: XMLSplitterSerialisationHelperTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 11:15:44 $
 *               by   $Author: sowen70 $
 * Created on 24-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.io.StringReader;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.embl.ebi.escience.scuflworkers.wsdl.XMLSplitterSerialisationHelper;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.ComplexTypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XMLSplitterSerialisationHelperTest extends TestCase {

	public void testCyclicToElement() throws Exception {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("typename");
		a.setQname(new QName("{namespace}typename"));

		ComplexTypeDescriptor b = new ComplexTypeDescriptor();
		b.setName("b");
		b.setType("typename2");
		b.setQname(new QName("{namespace}typename2"));

		a.getElements().add(b);

		b.getElements().add(a);

		Element el = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(a);

		String xml = new XMLOutputter().outputString(el);

		assertEquals(
				"unexpected xml",
				"<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename2\" name=\"b\" qname=\"{namespace}typename2\"><s:elements><s:complextype id=\"{namespace}typename\" optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>",
				xml);
		
	}

	public void testCyclicToElement2() throws Exception {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("typename");
		a.setQname(new QName("{namespace}typename"));

		a.getElements().add(a);

		Element el = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(a);

		String xml = new XMLOutputter().outputString(el);

		assertEquals(
				"unexpected xml",
				"<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype id=\"{namespace}typename\" optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" /></s:elements></s:complextype></s:extensions>",
				xml);		
	}

	public void testCyclicFromElement() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype id=\"{namespace}typename\" /></s:elements></s:complextype></s:extensions>";
		Element el = new SAXBuilder().build(new StringReader(xml)).getRootElement();

		TypeDescriptor a = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(el);

		assertTrue("wrong type", a instanceof ComplexTypeDescriptor);
		assertEquals("wrong name", "a", a.getName());

		List a_elements = ((ComplexTypeDescriptor) a).getElements();

		assertEquals("should be only 1 element", 1, a_elements.size());

		TypeDescriptor b = (TypeDescriptor) a_elements.get(0);

		assertTrue("wrong type", b instanceof ComplexTypeDescriptor);

		List b_elements = ((ComplexTypeDescriptor) b).getElements();

		assertEquals("should be only 1 element", 1, b_elements.size());

		assertEquals("b should contain a reference to a", a.toString(), b_elements.get(0).toString());
	}

}
