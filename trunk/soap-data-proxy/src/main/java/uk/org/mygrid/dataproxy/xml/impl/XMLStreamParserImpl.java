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
 * Filename           $RCSfile: XMLStreamParserImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-12 17:01:45 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.org.mygrid.dataproxy.xml.InterceptorWriter;
import uk.org.mygrid.dataproxy.xml.TagInterceptor;
import uk.org.mygrid.dataproxy.xml.XMLStreamParser;

public class XMLStreamParserImpl extends XMLWriter implements XMLStreamParser {	
	
	private static Logger logger = Logger.getLogger(XMLStreamParserImpl.class);

	private Map<String,TagInterceptor> interceptors = new HashMap<String,TagInterceptor>();
	private int activeTagCount = 0;
	private String activeTag = null;
	private InterceptorWriter activeWriter = null;
	
	
	@Override
	public void setOutputStream(OutputStream out) throws UnsupportedEncodingException {		
		super.setOutputStream(out);
		super.getOutputFormat().setSuppressDeclaration(true);
	}



	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (activeWriter != null ) {
			try {
				activeWriter.write(ch, start, length);
			} catch (IOException e) {
				logger.error("Error writing to active writer",e);
			}			
		}		
		else {
			super.characters(ch, start, length);			
		}
	}


	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (activeWriter!=null) {
			try {
				activeWriter.write("</"+qName+">");
			} catch (IOException e) {
				logger.error("Error writing end Element to Interceptor writer",e);
			}
			if (activeTag.equals(localName)) {
				activeTagCount--;
			}
			if (activeTagCount==0) {
				TagInterceptor interceptor=interceptors.get(localName);
				super.endElement(uri, interceptor.getReplacementTag(), qName.replaceAll(localName, interceptor.getReplacementTag()));
				try {
					activeWriter.close();
				}
				catch(IOException e) {
					logger.error("Error closing active writer ",e);
				}
				activeWriter=null;
				activeTag=null;
			}
		}
		else {
			super.endElement(uri, localName, qName);
		}
	}

	


	@Override
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning:"+e.getMessage());
		super.warning(e);
	}





	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {		
		TagInterceptor interceptor = null;
		if (activeTag==null) {
			interceptor = interceptors.get(localName);
			if (interceptor!=null) {				
				activeTag=localName;
				try {
					activeWriter=interceptor.getWriterFactory().newWriter();
				}
				catch(Exception e) {
					logger.error("Unable to retrieve a new Writer ",e);
					throw new SAXException(e);
				}
			}
		}
		
		if (activeTag!=null) {
			if (activeTag.equals(localName)) {
				activeTagCount++;
			}
			
			String attributesString="";
			for (int i=0;i<atts.getLength();i++) {
				attributesString+=atts.getQName(i)+"=\""+atts.getValue(i)+"\" ";
			}
			if (attributesString.length()>0) attributesString=" "+attributesString.trim();
			
			try {
				activeWriter.write("<"+qName+attributesString+">");				
			} catch (IOException e) {
				logger.error("Error writing to Interceptors writer:",e);
			}
			if (interceptor!=null) {
				super.startElement(uri,interceptor.getReplacementTag(),qName.replaceAll(localName, interceptor.getReplacementTag()),new AttributesImpl());
				String destinationName=activeWriter.getDestinationName();
				super.characters(destinationName.toCharArray(), 0, destinationName.length());
			}
		}
		else {
			super.startElement(uri, localName, qName, atts);
		}								
	}		
	
	public void read(InputStream stream) throws SAXException, IOException {
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(this);				
		reader.parse(new InputSource(stream));
	}

	public void addTagInterceptor(TagInterceptor interceptor) {
		interceptors.put(interceptor.getTargetTag(),interceptor);			
	}

}
