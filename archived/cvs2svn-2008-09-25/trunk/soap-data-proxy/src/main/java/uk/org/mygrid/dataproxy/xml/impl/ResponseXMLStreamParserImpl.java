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
 * Filename           $RCSfile: ResponseXMLStreamParserImpl.java,v $
 * Revision           $Revision: 1.14 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 09:43:36 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import uk.org.mygrid.dataproxy.xml.InterceptingXMLStreamParser;
import uk.org.mygrid.dataproxy.xml.InterceptorWriter;
import uk.org.mygrid.dataproxy.xml.ResponseTagInterceptor;
import uk.org.mygrid.dataproxy.xml.TagInterceptor;

/**
 * A parser based upon the SOAP response, which will redirect the content of any elements that matches
 * a TagInterceptor, redirecting the data to the writer provided by that interceptor.
 * 
 * @author Stuart Owen
 */

public class ResponseXMLStreamParserImpl extends AbstractXMLStreamParser implements InterceptingXMLStreamParser {	
	
	private static Logger logger = Logger.getLogger(ResponseXMLStreamParserImpl.class);
	
	private int activeTagCount = 0;
	private String activeTag = null;
	private InterceptorWriter activeWriter = null;
	private List<String> tagHistory = new ArrayList<String>();			

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
		
		if (tagHistory.get(tagHistory.size()-1).equals(localName)) {
			tagHistory.remove(tagHistory.size()-1);
		}
		else {
			logger.warn("Last tag in list does not match end element");
		}
		
		if (activeWriter!=null) {
			
			if (activeTag.equals(localName)) {
				activeTagCount--;
			}
			if (activeTagCount==0) {
				writeReplacementEndElement(uri, localName, qName);
				try {
					activeWriter.close();
				}
				catch(IOException e) {
					logger.error("Error closing active writer ",e);
				}
				activeWriter=null;
				activeTag=null;
			}
			else {
				writeEndTagToActiveWriter(qName); //write inner tags, but not the tag of the element being stripped
			}
		}
		else {				
			super.endElement(uri, localName, qName);
		}
	}

	private void writeReplacementEndElement(String uri, String localName, String qName) throws SAXException {		
		if (logger.isDebugEnabled()) logger.debug("Encountered final tag for active Tag: "+localName);
		super.endElement(uri, localName, qName);
	}

	private void writeEndTagToActiveWriter(String qName) {
		try {
			activeWriter.write("</"+qName+">");
		} catch (IOException e) {
			logger.error("Error writing end Element to Interceptor writer",e);
		}
	}	
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {				
		tagHistory.add(localName);
		
		if (activeTag==null) {			
			if (checkForNewStartElement(uri, localName, qName)) {
				writeReplacementStartElement(uri, localName, qName);
			}
		}		
		
		if (activeTag!=null) {			
			if (activeTagCount>0) writeStartTagToActiveWriter(localName, qName, atts);
			
			if (activeTag.equals(localName)) {				
				activeTagCount++;
			}						
		}
		else {
			super.startElement(uri, localName, qName, atts);
		}
	}

	private void writeStartTagToActiveWriter(String localName, String qName, Attributes atts) {
						
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
	}

	private void writeReplacementStartElement(String uri, String localName, String qName) throws SAXException {
		AttributesImpl attributes = new AttributesImpl();
		
		super.startElement(uri,localName,qName,attributes);
		String destinationName=activeWriter.getDestinationReference();		
		super.characters(destinationName.toCharArray(), 0, destinationName.length());		
	}

	private boolean checkForNewStartElement(String uri, String localName, String qName) throws SAXException {
		String path = currentPath();
		//TODO: in the future pass operation rather than *, though currently the path will indicate the operation
		TagInterceptor interceptor = getTagInterceptorForElement(localName,uri,path,"*");
		if (interceptor!=null && interceptor instanceof ResponseTagInterceptor) {		
			if (logger.isDebugEnabled()) logger.debug("Found matching start tag for :"+localName);
			activeTag=localName;
			try {
				activeWriter=((ResponseTagInterceptor)interceptor).getWriterFactory().newWriter();				
			}
			catch(Exception e) {
				logger.error("Unable to retrieve a new Writer ",e);
				throw new SAXException(e);
			}													
		}
		return interceptor!=null;
	}
	
	private String currentPath() {
		String result="*";		
		for (String s : tagHistory) {
			result+="/"+s;			
		}
		return result;
	}	

	@Override
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning:"+e.getMessage());
		super.warning(e);
	}		
}
