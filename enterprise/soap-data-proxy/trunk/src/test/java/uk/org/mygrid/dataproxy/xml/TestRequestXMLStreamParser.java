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
 * Filename           $RCSfile: TestRequestXMLStreamParser.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 14:20:48 $
 *               by   $Author: sowen70 $
 * Created on 15 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.dataproxy.xml.impl.RequestXMLStreamParserImpl;

public class TestRequestXMLStreamParser {

	private InterceptingXMLStreamParser parser = null;
	private ByteArrayOutputStream outputStream = null;
	
	@Before
	public void setUp() throws Exception {
		parser=new RequestXMLStreamParserImpl();
		outputStream = new ByteArrayOutputStream();
		parser.setOutputStream(outputStream);
	}
	
	@Test
	public void testRewriteData() throws Exception {
		String xml="<somexml><header>header</header><data>1</data><data>2</data></somexml>";
		
		StringReaderFactory readerFactory=new StringReaderFactory();
		readerFactory.addStringData("1", "data1");
		readerFactory.addStringData("2", "data2");
		
		parser.addContentInterceptor(new DummyContentInterceptor(readerFactory));
				
		parser.read(new ByteArrayInputStream(xml.getBytes()));
				
		String finalXML = outputStream.toString();
		
		assertEquals("<somexml><header>header</header><data>data1</data><data>data2</data></somexml>",finalXML);						
	}
	
	@Test
	public void testRewriteData2() throws Exception {
		String xml="<somexml><data>ref:1982223</data><data>ref:2392348</data><footer>footer</footer></somexml>";
		
		StringReaderFactory readerFactory=new StringReaderFactory();
		readerFactory.addStringData("ref:1982223", "11111111");
		readerFactory.addStringData("ref:2392348", "22222222");
				
		parser.addContentInterceptor(new DummyContentInterceptor(readerFactory));
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outputStream.toString();
		
		assertEquals("<somexml><data>11111111</data><data>22222222</data><footer>footer</footer></somexml>",finalXML);						
	}
	
	@Test
	public void testTrimLeadingSpacesOrNewlines() throws Exception {
		String xml="<somexml><data>    ref:1982223</data><data>\nref:2392348</data><data>\n\rref:2392348</data><footer>footer</footer></somexml>";
		
		StringReaderFactory readerFactory=new StringReaderFactory();
		readerFactory.addStringData("ref:1982223", "11111111");
		readerFactory.addStringData("ref:2392348", "22222222");
				
		parser.addContentInterceptor(new DummyContentInterceptor(readerFactory));
		parser.read(new ByteArrayInputStream(xml.getBytes()));		
				
		String finalXML = outputStream.toString();
		
		assertEquals("<somexml><data>11111111</data><data>22222222</data><data>22222222</data><footer>footer</footer></somexml>",finalXML);						
	}
	
}
