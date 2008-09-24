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
 * Filename           $RCSfile: TestDataURLInterceptor.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 14:20:48 $
 *               by   $Author: sowen70 $
 * Created on 16 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.xml.impl.DataURLInterceptor;
import uk.org.mygrid.dataproxy.xml.impl.RequestXMLStreamParserImpl;

public class TestDataURLInterceptor {
	
	private static Logger logger = Logger
			.getLogger(TestDataURLInterceptor.class);
	
	private File tmpDir,wsdlDir,dataDir,data,xmldata;
	private ByteArrayOutputStream outStream;
	private InterceptingXMLStreamParser parser;

	@Before
	public void setUp() throws Exception{
		try {
			tmpDir = File.createTempFile("dataproxy-test", "");
			// But we want a directory!
		} catch (IOException e) {
			logger.error("Couldn't create tmp dir",e);	
		}
		tmpDir.delete();
		tmpDir.mkdir();
		
		wsdlDir=new File(tmpDir,"73f13fba");
		wsdlDir.mkdir();
		
		dataDir=new File(wsdlDir,"1671a4df");
		dataDir.mkdir();
		
		data=new File(dataDir,"data1");
		BufferedWriter writer = new BufferedWriter(new FileWriter(data));
		writer.write("the meaning of life is 42");
		writer.close();	
		
		xmldata=new File(dataDir,"xmldata1");
		writer = new BufferedWriter(new FileWriter(xmldata));
		writer.write("<somedata>xxx</somedata>");
		writer.close();
		
		outStream = new ByteArrayOutputStream();
		parser=new RequestXMLStreamParserImpl();
		parser.setOutputStream(outStream);
	}
	
	@After
	public void tearDown() {
		data.delete();
		xmldata.delete();
		dataDir.delete();
		wsdlDir.delete();
		tmpDir.delete();
	}
	
	@Test
	public void testDereferencingOfURL() throws Exception {

		ProxyConfigFactory.getInstance().setContextPath("http://localhost/dataproxy/");
		ProxyConfigFactory.getInstance().setStoreBaseURL(tmpDir.toURL());
		String xml = "<somexml><header>header</header><data>http://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><footer>footer</footer></somexml>";
		
		parser.addContentInterceptor(new DataURLInterceptor());
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outStream.toString();
		
		assertEquals("Url wasn't correctly dereferenced - xml is probably escaped","<somexml><header>header</header><data>the meaning of life is 42</data><footer>footer</footer></somexml>",finalXML); 		
	}
	
	@Test
	public void testTrimLeadingSpaces() throws Exception {

		ProxyConfigFactory.getInstance().setContextPath("http://localhost/dataproxy/");
		ProxyConfigFactory.getInstance().setStoreBaseURL(tmpDir.toURL());
		String xml = "<somexml><header>header</header><data>   http://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><footer>footer</footer></somexml>";
		
		parser.addContentInterceptor(new DataURLInterceptor());
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outStream.toString();
		
		assertEquals("Url wasn't correctly dereferenced - xml is probably escaped","<somexml><header>header</header><data>the meaning of life is 42</data><footer>footer</footer></somexml>",finalXML); 		
	}
	
	@Test
	public void testMultipleDefinitions() throws Exception {

		ProxyConfigFactory.getInstance().setContextPath("http://localhost/dataproxy/");
		ProxyConfigFactory.getInstance().setStoreBaseURL(tmpDir.toURL());
		String xml = "<somexml><header>header</header><data>http://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><data>http://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><data>http://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><footer>footer</footer></somexml>";
		
		parser.addContentInterceptor(new DataURLInterceptor());
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outStream.toString();
		
		assertEquals("Url wasn't correctly dereferenced - xml is probably escaped","<somexml><header>header</header><data>the meaning of life is 42</data><data>the meaning of life is 42</data><data>the meaning of life is 42</data><footer>footer</footer></somexml>",finalXML); 		
	}
	
	@Test
	public void testTrimLeadingNewLine() throws Exception {

		ProxyConfigFactory.getInstance().setContextPath("http://localhost/dataproxy/");
		ProxyConfigFactory.getInstance().setStoreBaseURL(tmpDir.toURL());
		String xml = "<somexml><header>header</header><data>\n\nhttp://localhost/dataproxy/data?id=73f13fba-1671a4df-data1</data><footer>footer</footer></somexml>";
		
		parser.addContentInterceptor(new DataURLInterceptor());
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outStream.toString();
		
		assertEquals("Url wasn't correctly dereferenced - xml is probably escaped","<somexml><header>header</header><data>the meaning of life is 42</data><footer>footer</footer></somexml>",finalXML); 		
	}
	
	@Test
	public void testEmbeddedXML() throws Exception {
		ProxyConfigFactory.getInstance().setContextPath("http://localhost/dataproxy/");
		ProxyConfigFactory.getInstance().setStoreBaseURL(tmpDir.toURL());
		String xml = "<somexml><header>header</header><data>http://localhost/dataproxy/data?id=73f13fba-1671a4df-xmldata1</data><footer>footer</footer></somexml>";
		
		parser.addContentInterceptor(new DataURLInterceptor());
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outStream.toString();
		
		assertEquals("Url wasn't correctly dereferenced","<somexml><header>header</header><data><somedata>xxx</somedata></data><footer>footer</footer></somexml>",finalXML);
	}
}
