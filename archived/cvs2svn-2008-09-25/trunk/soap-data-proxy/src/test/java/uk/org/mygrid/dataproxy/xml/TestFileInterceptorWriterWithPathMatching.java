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
 * Filename           $RCSfile: TestFileInterceptorWriterWithPathMatching.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-21 19:53:34 $
 *               by   $Author: sowen70 $
 * Created on 16 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.ResponseTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.ResponseXMLStreamParserImpl;

public class TestFileInterceptorWriterWithPathMatching {

	private static Logger logger = Logger
			.getLogger(TestFileInterceptorWriter.class);

	private File tmpDir;

	private ByteArrayOutputStream outStream;

	private InterceptingXMLStreamParser parser;

	@Before
	public void setUp() throws Exception {
		try {
			tmpDir = File.createTempFile("dataproxy-test", "");
			// But we want a directory!
		} catch (IOException e) {
			logger.error("Couldn't create tmp dir", e);
		}
		tmpDir.delete();
		tmpDir.mkdir();
		outStream = new ByteArrayOutputStream();
		parser = new ResponseXMLStreamParserImpl();
		parser.setOutputStream(outStream);
	}

	@After
	public void deleteTemp() {
		for (File file : tmpDir.listFiles())
			file.delete();
		tmpDir.delete();
	}
	
	@Test
	public void testMatchingPath() throws Exception {
		String xml="<section><title>Title</title><data>some data</data></section>";
		ResponseTagInterceptor interceptor = new ResponseTagInterceptorImpl(new ElementDefinition("data","","*/section/data","*"), new FileInterceptorWriterFactory(tmpDir.toURL(),"http://localhost/data","data"));
		
		parser.addTagInterceptor(interceptor);
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		Document doc = new SAXReader().read(new ByteArrayInputStream(outStream.toByteArray()));
		
		Element el = doc.getRootElement().element("data");
		String data=el.getTextTrim();						
		
		assertFalse("some data".equals(data));
	}
	
	@Test
	public void testMatchingWildcardName() throws Exception {
		String xml="<section><title>Title</title><data>some data</data></section>";
		ResponseTagInterceptor interceptor = new ResponseTagInterceptorImpl(new ElementDefinition("*","","*/section/data","*"), new FileInterceptorWriterFactory(tmpDir.toURL(),"http://localhost/data","data"));
		
		parser.addTagInterceptor(interceptor);
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		Document doc = new SAXReader().read(new ByteArrayInputStream(outStream.toByteArray()));
		
		Element el = doc.getRootElement().element("data");
		String data=el.getTextTrim();						
		
		assertFalse("some data".equals(data));
	}
	
	@Test
	public void testNonMatchingPath() throws Exception {
		String xml="<section><title>Title</title><data>some data</data></section>";
		ResponseTagInterceptor interceptor = new ResponseTagInterceptorImpl(new ElementDefinition("data","","*/anelement/data","*"), new FileInterceptorWriterFactory(tmpDir.toURL(),"http://localhost/data","data"));
		
		parser.addTagInterceptor(interceptor);
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		Document doc = new SAXReader().read(new ByteArrayInputStream(outStream.toByteArray()));
		
		Element el = doc.getRootElement().element("data");
		String data=el.getTextTrim();						
		
		assertEquals("some data",data);
	}
	

}
