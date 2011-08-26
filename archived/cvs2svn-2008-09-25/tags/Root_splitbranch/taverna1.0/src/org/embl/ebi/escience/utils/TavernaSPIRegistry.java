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
 * Filename           $RCSfile: TavernaSPIRegistry.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-06-30 11:31:49 $
 *               by   $Author: sowen70 $
 * Created on 30-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.tools.Service;
import org.apache.log4j.Logger;

/**
 * Base class of the Registry classes in Taverna. Uses the SPI pattern to discover classes
 * of the type defined by spiClass in the constructor.
 * 
 * @author sowen
 *
 */

public class TavernaSPIRegistry<T> {

	private Class<T> spiClass;
	private static Logger logger = Logger.getLogger(TavernaSPIRegistry.class);
	private static Map<Class,List> cache = Collections.synchronizedMap(new HashMap<Class,List>());
	
	
	public TavernaSPIRegistry(Class<T> spiClass) {
		this.spiClass = spiClass;
		if (cache.get(spiClass)==null)
		{
			cache.put(spiClass,new ArrayList<T>());
		}
	}
	
	/**
	 * Finds the components registered as an SPI against class <T>, and searches the specified classloader
	 * Checks cache first, and only does a refetch if the cache is empty.
	 * The cache can be cleared using flushCache. 
	 */
	protected synchronized List<T> findComponents(ClassLoader classloader)
	{
		List<T> result = cache.get(spiClass);
		if (result.size()==0)
		{
			ClassLoaders loaders = new ClassLoaders();
			loaders.put(classloader);
			SPInterface spi = new SPInterface(spiClass);
			Enumeration en = Service.providers(spi,loaders);
			while (en.hasMoreElements()) {
				T el=(T)en.nextElement();
				result.add(el);	
				logger.info("Found plugin:"+el.getClass().getSimpleName());
			}			
		}
		return result;
	}
	
	
	/**
	 * Finds the components registered as an SPI against class <T>, searching the current classloader
	 * Checks cache first, and only does a refetch if the cache is empty.
	 * The cache can be cleared using flushCache. 
	 */
	protected synchronized List<T> findComponents()
	{				
		return findComponents(getClassLoader());
	}
	
	protected ClassLoader getClassLoader()
	{
		ClassLoader loader = TavernaSPIRegistry.class.getClassLoader();
		if (loader == null) {
			loader = Thread.currentThread().getContextClassLoader();
		}
		
		return loader;	
	}
	
	/**
	 * flushes the cached components based on the provided spiClass
	 * @param spiClass
	 */
	public static void flushCache(Class spiClass)
	{
		List list=cache.get(spiClass);
		if (list!=null)
		{
			list.clear();
		}
	}

}
