package uk.org.mygrid.dataproxy.xml;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.ResponseTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseXMLStreamParserImpl;

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
 * Filename           $RCSfile: TestResponseXMLStreamParser.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-17 15:02:40 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/

public class TestResponseXMLStreamParser {

	private InterceptingXMLStreamParser parser = null;
	ByteArrayOutputStream outputStream;
	
	@Before
	public void setUp() throws UnsupportedEncodingException {
		parser=new ResponseXMLStreamParserImpl();
		outputStream  = new ByteArrayOutputStream();		
		parser.setOutputStream(outputStream );
	}
	
	@Test
	public void simpleXML() throws Exception {
		String xml="<section><para>a paragraph</para><link href=\"a ref\">my site</link></section>";
				
		StringWriterFactory paraWriterFactory = new StringWriterFactory();
		StringWriterFactory linkWriterFactory = new StringWriterFactory();
		ResponseTagInterceptor paraInterceptor=new ResponseTagInterceptorImpl(new ElementDefinition("para","","*/section/para","*"),paraWriterFactory);
		ResponseTagInterceptor linkInterceptor=new ResponseTagInterceptorImpl(new ElementDefinition("link","","*/section/link","*"),linkWriterFactory);
		
		parser.addTagInterceptor(paraInterceptor);
		parser.addTagInterceptor(linkInterceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals("Wrong number of matches found for para element",1,paraWriterFactory.getOutputsWritten().size());
		assertEquals("XML for para element did not match expected","a paragraph",paraWriterFactory.getOutputsWritten().get(0));
		
		assertEquals("Wrong number of matches found for link element",1,linkWriterFactory.getOutputsWritten().size());
		assertEquals("XML for link element did not match expected","my site",linkWriterFactory.getOutputsWritten().get(0));
		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<section><para>1</para><link>1</link></section>",finalXML);		
	}	
	
	@Test
	public void multipleElements() throws Exception {
		String xml = "<a><b>bbbbb</b><c>c</c><b>bbb</b></a>";
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/b","*"),bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(2,bWriterFactory.getOutputsWritten().size());
		assertEquals("bbbbb",bWriterFactory.getOutputsWritten().get(0));
		assertEquals("bbb",bWriterFactory.getOutputsWritten().get(1));
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b>1</b><c>c</c><b>2</b></a>",finalXML);
	}
	
	@Test 
	public void nested() throws Exception {
		String xml = "<a><b><c>c</c><b>bbbbb</b></b><c>c</c></a>";
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/b","*"),bWriterFactory);		
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<c>c</c><b>bbbbb</b>",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b>1</b><c>c</c></a>",finalXML);
	}
	
	@Test 
	public void nestedWithinActiveTag() throws Exception {
		String xml="<a><b><c>ccc</c></b></a>";
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		StringWriterFactory cWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptorB=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/b","*"),bWriterFactory);		
		ResponseTagInterceptor interceptorC=new ResponseTagInterceptorImpl(new ElementDefinition("c","","*/c","*"),cWriterFactory);
		
		parser.addTagInterceptor(interceptorB);
		parser.addTagInterceptor(interceptorC);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("<c>ccc</c>",bWriterFactory.getOutputsWritten().get(0));
		
		assertEquals("nothing should have been intercepted for the c tag",0,cWriterFactory.getOutputsWritten().size());
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b>1</b></a>",finalXML);
	}
	
	@Test
	public void testNamespace() throws Exception {
		String xml = "<ns1:a xmlns:ns1=\"a\"><ns1:b>bbb</ns1:b></ns1:a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","a","*/a/b","*"),bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("bbb",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		
				
		assertEquals("<ns1:a xmlns:ns1=\"a\"><ns1:b>1</ns1:b></ns1:a>",finalXML);
	}	
	
	@Test
	public void testNamespace2() throws Exception {
		String xml = "<ns1:a xmlns:ns1=\"a\"><b>bbb</b></ns1:a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/a/b","*"),bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("bbb",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
				
		assertEquals("<ns1:a xmlns:ns1=\"a\"><b>1</b></ns1:a>",finalXML);
	}
	
	@Test 
	public void testNoContent() throws Exception {
		String xml = "<a><b/></a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/b","*"),bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b>1</b></a>",finalXML);
	}		
	
	@Test 
	public void testNoContent2() throws Exception {
		String xml = "<a><b></b></a>";
		
		StringWriterFactory bWriterFactory=new StringWriterFactory();
		ResponseTagInterceptor interceptor=new ResponseTagInterceptorImpl(new ElementDefinition("b","","*/a/b","*"),bWriterFactory);
		
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		assertEquals(1,bWriterFactory.getOutputsWritten().size());
		assertEquals("",bWriterFactory.getOutputsWritten().get(0));		
		
		String finalXML=outputStream.toString();
		
		assertEquals("<a><b>1</b></a>",finalXML);
	}	
}


