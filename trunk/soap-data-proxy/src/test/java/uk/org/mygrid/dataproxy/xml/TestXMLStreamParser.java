package uk.org.mygrid.dataproxy.xml;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.TagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.XMLStreamParserImpl;

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
 * Filename           $RCSfile: TestXMLStreamParser.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-12 17:01:45 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/

public class TestXMLStreamParser {

	private XMLStreamParser parser = null;
	ByteArrayOutputStream outputStream;
	
	@Before
	public void setUp() throws UnsupportedEncodingException {
		parser=new XMLStreamParserImpl();
		outputStream  = new ByteArrayOutputStream();		
		parser.setOutputStream(outputStream );
	}
	
	@Test
	public void simpleXML() throws Exception {
		String xml="<section><para>a paragraph</para><link href=\"a ref\">my site</link></section>";
				
		StringWriterFactory paraWriterFactory = new StringWriterFactory();
		StringWriterFactory linkWriterFactory = new StringWriterFactory();
		TagInterceptor paraInterceptor=new TagInterceptorImpl("para","para-replaced",paraWriterFactory);
		TagInterceptor linkInterceptor=new TagInterceptorImpl("link","link-replaced",linkWriterFactory);
		
		parser.addTagInterceptor(paraInterceptor);
		parser.addTagInterceptor(linkInterceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals("Wrong number of matches found for para element",1,paraWriterFactory.getOutputsWritten().size());
		assertEquals("XML for para element did not match expected","<para>a paragraph</para>",paraWriterFactory.getOutputsWritten().get(0));
		
		assertEquals("Wrong number of matches found for link element",1,linkWriterFactory.getOutputsWritten().size());
		assertEquals("XML for link element did not match expected","<link href=\"a ref\">my site</link>",linkWriterFactory.getOutputsWritten().get(0));
		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<section><para-replaced>1</para-replaced><link-replaced>1</link-replaced></section>",finalXML);
		
	}
	
	@Test
	public void multipleElements() throws Exception {
		String xml = "<a><b>bbbbb</b><c>c</c><b>bbb</b></a>";
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		TagInterceptor interceptor=new TagInterceptorImpl("b","b-replaced",bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(2,bWriterFactory.getOutputsWritten().size());
		assertEquals("<b>bbbbb</b>",bWriterFactory.getOutputsWritten().get(0));
		assertEquals("<b>bbb</b>",bWriterFactory.getOutputsWritten().get(1));
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b-replaced>1</b-replaced><c>c</c><b-replaced>2</b-replaced></a>",finalXML);
	}
	
	@Test 
	public void testNested() throws Exception {
		String xml = "<a><b><c>c</c><b>bbbbb</b></b><c>c</c></a>";
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		TagInterceptor interceptor=new TagInterceptorImpl("b","b-replaced",bWriterFactory);		
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<b><c>c</c><b>bbbbb</b></b>",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b-replaced>1</b-replaced><c>c</c></a>",finalXML);
	}
	
	@Test
	public void testNamespace() throws Exception {
		String xml = "<ns1:a xmlns:ns1=\"a\"><ns1:b>bbb</ns1:b></ns1:a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		TagInterceptor interceptor=new TagInterceptorImpl("b","b-replaced",bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<ns1:b>bbb</ns1:b>",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
				
		assertEquals("<ns1:a xmlns:ns1=\"a\"><ns1:b-replaced>1</ns1:b-replaced></ns1:a>",finalXML);
	}
	
	@Test
	public void testNamespace2() throws Exception {
		String xml = "<ns1:a xmlns:ns1=\"a\"><b>bbb</b></ns1:a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		TagInterceptor interceptor=new TagInterceptorImpl("b","b-replaced",bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<b>bbb</b>",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
				
		assertEquals("<ns1:a xmlns:ns1=\"a\"><b-replaced>1</b-replaced></ns1:a>",finalXML);
	}
	
	@Test 
	public void testNoContent() throws Exception {
		String xml = "<a><b/></a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		TagInterceptor interceptor=new TagInterceptorImpl("b","b-replaced",bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<b></b>",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b-replaced>1</b-replaced></a>",finalXML);
	}		
}


