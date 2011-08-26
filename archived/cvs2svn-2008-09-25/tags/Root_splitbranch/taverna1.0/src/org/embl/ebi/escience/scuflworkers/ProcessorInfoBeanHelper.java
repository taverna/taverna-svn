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
 * Filename           $RCSfile: ProcessorInfoBeanHelper.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 09:54:03 $
 *               by   $Author: sowen70 $
 * Created on 22-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * Generic helper class to aid in retreiving processor information from
 * taverna.properties
 * 
 * @author Stuart Owen
 * 
 */

public class ProcessorInfoBeanHelper implements ProcessorInfoBean {

	private static Logger logger = Logger.getLogger(ProcessorInfoBeanHelper.class);

	private String propertyBase = "";

	private String tag = "";

	private static Properties tavernaProperties;

	static {
		ClassLoader loader = ProcessorInfoBeanHelper.class.getClassLoader();
		if (loader == null) {
			loader = Thread.currentThread().getContextClassLoader();
		}
		try {
			Enumeration en = loader.getResources("taverna.properties");
			tavernaProperties = new Properties();
			while (en.hasMoreElements()) {
				URL resourceURL = (URL) en.nextElement();
				logger.info("Loading resources from : " + resourceURL.toString());
				tavernaProperties.load(resourceURL.openStream());
			}
		} catch (IOException e) {
			logger.error("Error loading taverna.properties.", e);
		}
	}

	public ProcessorInfoBeanHelper(String tagname) {
		tag = tagname;
		propertyBase = "taverna.processor." + tagname;
	}

	public String processorClassname() {
		String result = tavernaProperties.getProperty(propertyBase + ".class");
		if (result!=null) result=result.trim();
		return result;
	}

	public String xmlHandlerClassname() {
		String result= tavernaProperties.getProperty(propertyBase + ".xml");
		if (result!=null) result=result.trim();
		return result;
	}

	public String colour() {
		String result=tavernaProperties.getProperty(propertyBase + ".colour");
		if (result!=null) result=result.trim();
		return result;
	}

	public ImageIcon icon() {
		String icon = tavernaProperties.getProperty(propertyBase + ".icon");		
		ImageIcon result = null;
		if (icon != null) {
			icon=icon.trim();
			ClassLoader loader = ProcessorInfoBeanHelper.class.getClassLoader();
			if (loader == null) {
				loader = Thread.currentThread().getContextClassLoader();
			}
			result = new ImageIcon(loader.getResource(icon));
		}
		return result;
	}

	public String taskClassname() {
		String result= tavernaProperties.getProperty(propertyBase + ".taskclass");
		if (result!=null) result=result.trim();
		return result;
	}

	public String editorClassname() {
		String result= tavernaProperties.getProperty(propertyBase + ".editor");
		if (result!=null) result=result.trim();
		return result;
	}

	public String scavengerClassname() {
		String result= tavernaProperties.getProperty(propertyBase + ".scavenger");
		if (result!=null) result=result.trim();
		return result;
	}

	public String tag() {
		return tag;
	}
}
