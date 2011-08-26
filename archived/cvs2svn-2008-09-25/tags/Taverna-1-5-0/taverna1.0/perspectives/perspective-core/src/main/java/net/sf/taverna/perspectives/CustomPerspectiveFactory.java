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
 * Filename           $RCSfile: CustomPerspectiveFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-10 12:06:31 $
 *               by   $Author: sowen70 $
 * Created on 9 Nov 2006
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class CustomPerspectiveFactory {
	
	private static Logger logger = Logger
			.getLogger(CustomPerspectiveFactory.class);
	
	private final static String XMLDOCNAME="user_perpectives.xml";
	
	private static CustomPerspectiveFactory instance = new CustomPerspectiveFactory();
	
	public static CustomPerspectiveFactory getInstance() {
		return instance;
	}
	
	public Set<CustomPerspective> getAll() throws IOException {
		Set<CustomPerspective> result=new HashSet<CustomPerspective>();
		File userdir=MyGridConfiguration.getUserDir("conf");
		File perspective=new File(userdir,XMLDOCNAME);
		if (perspective.exists()) {
			try {
				Document doc=new SAXBuilder().build(perspective);
				List children=doc.getRootElement().getChildren("layout");
				for (Iterator iterator = children.iterator() ; iterator.hasNext() ;) {
					Element child=(Element)iterator.next();
					result.add(new CustomPerspective(child));
				}
			} catch (JDOMException e) {
				logger.error("Error parsing xml in perspectives file",e);
			}
		}
		return result;
	}
	
	public void saveAll(Set<CustomPerspective> perspectives) throws FileNotFoundException, IOException {
		File userdir=MyGridConfiguration.getUserDir("conf");
		File perspectiveFile=new File(userdir,XMLDOCNAME);		
		Element topElement=new Element("perspectives");
		
		for (CustomPerspective p : perspectives) {
			Element layoutElement=new Element("layout");			
			layoutElement.setAttribute("name", p.getName());
			try {
				Element layoutContent=new SAXBuilder().build(p.getLayoutInputStream()).detachRootElement();
				layoutElement.addContent(layoutContent);
			} catch (JDOMException e) {
				logger.error("Error parsing layout xml",e);
			} catch (IOException e) {
				logger.error("Error reading stream for layout xml",e);
			}
			topElement.addContent(layoutElement);
		}
				
		XMLOutputter outputter=new XMLOutputter(Format.getPrettyFormat());
		outputter.output(topElement, new FileOutputStream(perspectiveFile));
	}
}
