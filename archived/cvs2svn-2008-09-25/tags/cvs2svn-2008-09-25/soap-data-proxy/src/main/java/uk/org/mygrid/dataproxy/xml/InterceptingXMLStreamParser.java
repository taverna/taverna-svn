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
 * Filename           $RCSfile: InterceptingXMLStreamParser.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:52 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An event driven XMLStreamParser that intercepts according to supplied ReferenceInterceptors or TagInterceptors. If an interceptor matches
 * then the stream is modified according to the behaviour of this interceptor.
 * @author Stuart Owen
 *
 */

public interface InterceptingXMLStreamParser {
			
	/**
	 * Sets the stream that the parsed XML stream will be forwarded to.
	 * @param stream
	 * @throws UnsupportedEncodingException
	 */
	public void setOutputStream(OutputStream stream) throws UnsupportedEncodingException;	
	
	/**
	 * Adds a TagInterceptor
	 * @param interceptor
	 */
	public void addTagInterceptor(TagInterceptor interceptor);
	
	/**
	 * Adds a EmbeddedReferenceInterceptor
	 * @param interceptor
	 */
	public void addContentInterceptor(EmbeddedReferenceInterceptor interceptor);
	
	/**
	 * Recieves an InputStream and starts to parse it, forwarding the modified stream (modified according to the supplied interceptors) to the 
	 * output stream provided by setOutputStream
	 * @param stream
	 * @throws SAXException
	 * @throws IOException
	 */
	public void read(InputStream stream)  throws SAXException, IOException;
	
	/**
	 * The callback method called when element content data is encountered
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	public void characters(char[] ch, int start, int length) throws SAXException;
	
	/**
	 * Callback method when an endtag is encountered
	 * @param uri - tag namespace uri
	 * @param localName - localname for the tag
	 * @param qName - the QName for the tag of the form {namespaceuri}localName
	 * @throws SAXException
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException;
	
	/**
	 * Callback method when a starttag is encountered
	 * @param uri - tag namespace uri
	 * @param localName - localname for the tag
	 * @param qName - the QName for the tag of the form {namespaceuri}localName
	 * @param attr - the attributes for the tag.
	 * @throws SAXException
	 */
	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException;
}
