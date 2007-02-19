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
 * Filename           $RCSfile: TestFileInterceptorReader.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-19 16:10:07 $
 *               by   $Author: sowen70 $
 * Created on 16 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.FileInterceptorReaderFactory;
import uk.org.mygrid.dataproxy.xml.impl.RequestTagInterceptorImpl;
import uk.org.mygrid.dataproxy.xml.impl.RequestXMLStreamParserImpl;

public class TestFileInterceptorReader {
	
	private static Logger logger = Logger
			.getLogger(TestFileInterceptorReader.class);

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
		parser=new RequestXMLStreamParserImpl();
		parser.setOutputStream(outStream);
		
	}
	
	@After
	public void clearUp() {
		for (File file : tmpDir.listFiles()) file.delete();
		tmpDir.delete();
	}
	
	@Test
	public void simpleTest() throws Exception {
		File dataFile = new File(tmpDir,"data1");
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dataFile));
		output.write("hello world".getBytes());
		output.close();
		
		String xml = "<somexml><data>"+dataFile.toURL().toExternalForm()+"</data></somexml>";
		TagInterceptor interceptor = new RequestTagInterceptorImpl(new ElementDef("data",""),new FileInterceptorReaderFactory());
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		String finalXML=new String(outStream.toByteArray());
		
		assertEquals("<somexml><data>hello world</data></somexml>", finalXML);
		
	}
	
	@Test
	public void insertingXML() throws Exception {
		File dataFile = new File(tmpDir,"data1");
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dataFile));
		output.write("<hello><world>blah blah blah</world></hello>".getBytes());
		output.close();
		
		String xml = "<somexml><data>"+dataFile.toURL().toExternalForm()+"</data></somexml>";
		TagInterceptor interceptor = new RequestTagInterceptorImpl(new ElementDef("data",""),new FileInterceptorReaderFactory());
		parser.addTagInterceptor(interceptor);
		
		parser.read(new ByteArrayInputStream(xml.getBytes()));
		
		String finalXML=new String(outStream.toByteArray());
		
		assertEquals("<somexml><data><hello><world>blah blah blah</world></hello></data></somexml>", finalXML);
	}
	
}
