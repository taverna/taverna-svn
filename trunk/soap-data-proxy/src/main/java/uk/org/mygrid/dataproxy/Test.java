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
 * Filename           $RCSfile: Test.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-09 16:38:44 $
 *               by   $Author: sowen70 $
 * Created on 8 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy;

import java.io.ByteArrayInputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String xml="<section><para><biggy>sdfljksdfjsdf sdf sdf sdf sdf sdf sd fsd f sfsdfsdfsdf sdf sf sdf sf sf sdf sdf sdf sdf sf sdf sdfsdf sdf sdf sf sf sdfsdfsdfjsfsjdhfskdfhsdfh sd sdkjfhsdkfhsdkfjhsd fsdkfjhsdkfjhsdfjkhsdfkjhsd fskjjfhskdjfhsdkjfhsdkfjhsdfkhs dfksjdhfkshfsdkfhskdjfhsdkfhsdkjfhskdfh sdfjkhsdkfhsdkfhskdfhsdkhskdhfskdjhfskfhsdkjfhsd fskdfhsdkfhsdkfhsdkfjhsdfkhsdfkhsfkjhsfkhskjfhsd fksjdhfskhfskjfhsdkjfhskdjfhskjfhsdkhfsdkfjhsdkfhskdhfsdkjfhksdfh skjdhfskdfhskdjfhskfhskdjfhskdfhsdkfhskdfhsdkfh skdjfhskfhsdfhsjkfhskfhskjfhskdjfhskfhskfhskdjfhskfh sdkfhsdkfhskdfhskdfhskjdfhskfhskdjfhskdfhskdjfhsdkfhsdkjfh ksjdfhsdkjfhsdkfhsdjkhfskdfhsdkfhskfhskdfhskdfhskdfhskdjfhsjkfhksdhfksdhfksdjhfskhfsdjkhf ksdjhfksdjhfsdkfh ksdhfskfhskjdhfksdfhskdfhskjdfhsdkhfskdjfhskdfh skdhfskdjfhskfsdjkfhsdkfjhskdfhksd fskdhfskfhskdfhskjdhfskhf first para</biggy></para><bob>inside bob</bob><fred><para>2nd para</para><para>3rd para</para><para><para>nested</para></para></fred></section>";
		SAXReader reader = new SAXReader();		
		
		reader.setXMLFilter(new XMLFilterImpl() {
			
			int inTag=0;
			String tag="para";
			

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				if (inTag>0) {
				//System.out.println("characters="+ch+", start="+start+", length="+length);
					System.out.print("[");
					for (int i=0;i<length;i++) {
						System.out.print(ch[i+start]);
					}
					System.out.print("]");
				//System.out.println("");
				}
				super.characters(ch, start, length);
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
				if (localName.equals(tag)) inTag++;
				if (inTag>0) System.out.print("<"+localName+">");
				super.startElement(uri,localName,qName,atts);								
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (inTag>0) System.out.print("</"+localName+">");
				if (localName.equals(tag)) inTag--;				
				super.endElement(uri, localName, qName);
			}
			
			

			
		});
		
		
		
		Document doc = reader.read(new ByteArrayInputStream(xml.getBytes()));
		
		write(doc.getRootElement());		
	}
	
	private static void write (Element element) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
		    XMLWriter writer = new XMLWriter( System.out, format );
		    //writer.write( element );
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	

}
