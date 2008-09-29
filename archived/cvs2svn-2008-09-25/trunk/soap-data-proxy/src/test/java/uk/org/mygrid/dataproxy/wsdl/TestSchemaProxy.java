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
 * Filename           $RCSfile: TestSchemaProxy.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 13:53:16 $
 *               by   $Author: sowen70 $
 * Created on 20 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import uk.org.mygrid.dataproxy.configuration.impl.NewWSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.impl.SchemaProxyImpl;

public class TestSchemaProxy {

	@SuppressWarnings("unchecked")
	@Test
	public void testRewritesIncludes() throws Exception
	{		
		NewWSDLConfig config = new NewWSDLConfig();
		config.setAddress("http://www.cs.man.ac.uk/~sowen/proxytests/wsdls/eutils/eutils.wsdl");
		config.setWSDLID("1");
		
		SchemaProxy proxy = new SchemaProxyImpl(config,"efetch.xsd");
		
		Document doc = new SAXReader().read(proxy.getStream());
		List<Element>include = doc.getRootElement().elements("include");
		
		Element taxon = include.get(0);		
		assertEquals("included schema should have been rewritten to use proxy",taxon.attributeValue("schemaLocation"),"schema?wsdlid=1&xsd=efetch_taxon.xsd");
		
		Element pubmed = include.get(3);
		assertEquals("included schema should have been rewritten to use proxy",pubmed.attributeValue("schemaLocation"),"schema?wsdlid=1&xsd=efetch_pubmed.xsd");		
	}
}
