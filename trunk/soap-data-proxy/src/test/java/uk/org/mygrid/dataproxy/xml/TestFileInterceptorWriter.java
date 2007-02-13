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
 * Filename           $RCSfile: TestFileInterceptorWriter.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-13 15:38:51 $
 *               by   $Author: sowen70 $
 * Created on 9 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorWriterFactory;
import uk.org.mygrid.dataproxy.xml.impl.TagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.XMLStreamParserImpl;

public class TestFileInterceptorWriter {

	private static Logger logger = Logger
			.getLogger(TestFileInterceptorWriter.class);
	
	private File tmpDir;
	private ByteArrayOutputStream outStream;
	private XMLStreamParser parser;
	
	@Before
	public void setUp() throws Exception {
		try {
			tmpDir = File.createTempFile("dataproxy-test", "");
			// But we want a directory!
		} catch (IOException e) {
			logger.error("Couldn't create tmp dir",e);	
		}
		tmpDir.delete();
		tmpDir.mkdir();
		outStream = new ByteArrayOutputStream();
		parser=new XMLStreamParserImpl();
		parser.setOutputStream(outStream);
	}
	
	@After
	public void deleteTemp() {
		for (File file : tmpDir.listFiles()) file.delete();
		tmpDir.delete();
	}
	
	@Test
	public void testWritesToFile() throws Exception {
		String xml="<section><title>Title</title><data>some data</data></section>";
		TagInterceptor interceptor = new TagInterceptorImpl("data","data-replaced", new FileInterceptorWriterFactory(tmpDir.toURL()));
		
		parser.addTagInterceptor(interceptor);
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		Document doc = new SAXReader().read(new ByteArrayInputStream(outStream.toByteArray()));
		
		Element el = doc.getRootElement().element("data-replaced");
		String strURL=el.getTextTrim();
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(strURL).openStream()));
		String result="";
		String line=null;
		while ((line=reader.readLine())!=null) {
			result+=line;
		}
		
		assertEquals("<data>some data</data>",result);
	}
	
	@Test
	public void testIncrementsDestinationCorrectly() throws Exception {
		String xml="<section><data>one</data><data>two</data><data>three</data></section>";
		TagInterceptor interceptor = new TagInterceptorImpl("data","data-replaced", new FileInterceptorWriterFactory(tmpDir.toURL()));
		
		parser.addTagInterceptor(interceptor);
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		Document doc = new SAXReader().read(new ByteArrayInputStream(outStream.toByteArray()));
		
		List<Element> elements = doc.getRootElement().elements("data-replaced");
		assertEquals("should be 3 elements",3,elements.size());
		
		int c=1;
		for (Element el : elements) {
			String url=el.getTextTrim();
			assertTrue("should end with "+c,url.endsWith(String.valueOf(c)));
			c++;
		}
		
	}
	
}
