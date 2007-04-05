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
 * Filename           $RCSfile: AbstractXMLStreamParser.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:34:16 $
 *               by   $Author: sowen70 $
 * Created on 15 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.org.mygrid.dataproxy.xml.ElementDefinition;
import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.InterceptingXMLStreamParser;

public abstract class AbstractXMLStreamParser extends XMLWriter implements InterceptingXMLStreamParser {
	private Map<ElementDefinition,TagInterceptor> interceptors = new HashMap<ElementDefinition,TagInterceptor>();
	
	private static Logger logger = Logger
			.getLogger(AbstractXMLStreamParser.class);	
	
	@Override
	public void setOutputStream(OutputStream out) throws UnsupportedEncodingException {		
		super.setOutputStream(out);
		super.getOutputFormat().setSuppressDeclaration(true);
	}
	
	public void read(InputStream stream) throws SAXException, IOException {
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(this);				
		reader.parse(new InputSource(stream));		
	}
	
	public void addTagInterceptor(TagInterceptor interceptor) {
		interceptors.put(interceptor.getTargetElementDef(),interceptor);
		logger.debug("Interceptor added class="+interceptor.getClass()+" for elementDef: "+interceptor.getTargetElementDef());
	}
	
	protected TagInterceptor getInterceptorForElement(String element, String uri, String path, String operation) {
		ElementDefinition def = new ElementDefinition(element,uri,path,operation);
		for (TagInterceptor interceptor : interceptors.values()) {
			if (interceptor.getTargetElementDef().matches(def)) {				
				return interceptor; 				
			}
		}
		return null;
	}
		
}
