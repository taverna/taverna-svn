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
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 15 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.xml.EmbeddedReferenceInterceptor;
import uk.org.mygrid.dataproxy.xml.InterceptingXMLStreamParser;
import uk.org.mygrid.dataproxy.xml.InterceptorReader;
import uk.org.mygrid.dataproxy.xml.ReaderFactory;

/**
 * A parser based upon the SOAP request, which will de-reference any content that matches
 * a EmbeddedReferenceInterceptor
 * 
 * @author Stuart Owen
 */

public class RequestXMLStreamParserImpl extends AbstractXMLStreamParser implements InterceptingXMLStreamParser {

	private static Logger logger = Logger
			.getLogger(RequestXMLStreamParserImpl.class);
			
		
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (length<1024) { // an upper limit for checking for content interceptor
			String content = String.valueOf(ch,start,length);
			EmbeddedReferenceInterceptor interceptor = getContentInterceptor(content);
			if (interceptor !=null && interceptor instanceof EmbeddedReferenceInterceptor) {			
				writeInterceptedContent(content, interceptor);												
			}
			else {
				super.characters(ch, start, length);
			}
		}
		else {
			logger.info("content length too big to check for ContentInterceptor, length was:"+length);
			super.characters(ch, start, length);
		}
	}

	private void writeInterceptedContent(String content, EmbeddedReferenceInterceptor interceptor) throws SAXException {
		ReaderFactory factory = ((EmbeddedReferenceInterceptor)interceptor).getReaderFactory();				
		InterceptorReader reader = factory.getReaderForContent(content.trim());
		
		int len = 0;
		char[] buffer=new char[255];
		setEscapeText(false);
		
		try {
			
			while ((len = reader.read(buffer))!=-1) {
				super.characters(buffer, 0, len);
			}
			reader.close();
			
		} catch (IOException e) {
			logger.error("Error occurred reading data from reader",e);
			throw new SAXException("Unable to read from InterceptorReader for content '"+content+"'",e);
		}
		
		setEscapeText(true);		
	}			
}
