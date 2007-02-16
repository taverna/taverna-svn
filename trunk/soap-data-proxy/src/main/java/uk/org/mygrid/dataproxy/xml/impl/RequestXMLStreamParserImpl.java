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
 * Filename           $RCSfile: RequestXMLStreamParserImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-16 16:13:58 $
 *               by   $Author: sowen70 $
 * Created on 15 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.xml.InterceptorReader;
import uk.org.mygrid.dataproxy.xml.ReaderFactory;
import uk.org.mygrid.dataproxy.xml.RequestTagInterceptor;
import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;

public class RequestXMLStreamParserImpl extends AbstractXMLStreamParser implements XMLStreamParser {

	private static Logger logger = Logger
			.getLogger(RequestXMLStreamParserImpl.class);
	
	private ReaderFactory activeReaderFactory = null;
	private String reference;
		
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (activeReaderFactory!=null) {
			reference+=String.valueOf(ch, start, length);
		}
		else {
			super.characters(ch, start, length);
		}
				
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (activeReaderFactory!=null) {
			insertDataFromReference();
			activeReaderFactory=null;
			reference=null;
		}
		super.endElement(uri, localName, qName);
	}
	
	private void insertDataFromReference() throws SAXException{
		InterceptorReader reader;
		try {			
			reader = activeReaderFactory.getReaderForReference(reference);
			if (logger.isDebugEnabled()) logger.debug("Found reader for reference:"+reference);
		}
		catch(Exception e) {
			logger.error("Unable to create reader for reference '"+reference+"'");
			throw new SAXException("Unable to create reader for reference '"+reference+"'",e);
		}
		char [] buffer = new char[255];
		int len=0;
		try {
			while ((len = reader.read(buffer))!= -1) {
				logger.info("Data read:"+String.valueOf(buffer,0,len));
				super.characters(buffer, 0, len);
			}
		}
		catch(IOException e) {
			logger.error("IOException reading from data stream");
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
		TagInterceptor interceptor = getInterceptorForElement(localName, uri);	
		if (logger.isDebugEnabled()) logger.debug("Met start element: "+localName+", namespaceuri="+uri+", qName="+qName);
		if (interceptor!=null && interceptor instanceof RequestTagInterceptor) {
			if (logger.isDebugEnabled()) logger.debug("Interceptor found for start element: "+localName+", namespaceuri="+uri+", qName="+qName);
			activeReaderFactory = ((RequestTagInterceptor)interceptor).getReaderFactory();
			reference="";
		}		
		super.startElement(uri, localName, qName, attr);		
	}	
}
