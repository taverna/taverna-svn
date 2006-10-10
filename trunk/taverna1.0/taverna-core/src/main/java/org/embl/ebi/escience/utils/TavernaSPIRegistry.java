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
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 * Last modified on   $Date$
 *               by   $Author$
 * Created on 30-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;

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

	private static Map<Class, InstanceRegistry> spiMap =
		Collections.synchronizedMap(new HashMap<Class, InstanceRegistry>());
	private static Repository REPOSITORY = null;
	
	public static void setRepository(Repository theRepository) {
		REPOSITORY = theRepository;
	}
	
	public TavernaSPIRegistry(Class<T> spiClass) {
		this.spiClass = spiClass;
		if (spiMap.containsKey(spiClass) == false) {
			SpiRegistry registry = new SpiRegistry(REPOSITORY, spiClass.getName(), null);
			registry.addRegistryListener(new RegistryListener() {
				public void spiRegistryUpdated(SpiRegistry registry) {
					System.out.println("Registry updated <"+
							TavernaSPIRegistry.this.spiClass.getName()+"> : ");
					for (Class<T> theClass : registry.getClasses()) {
						System.out.println("  "+theClass.getName());
						System.out.println("    - "+theClass.getClassLoader().toString());
					}
				}
			});
			spiMap.put(spiClass, new InstanceRegistry<T>(registry, new Object[0]));
		}
	}
	
	/**
	 * Finds the components registered as an SPI against class <T>, and searches the specified classloader
	 * Checks cache first, and only does a refetch if the cache is empty.
	 * The cache can be cleared using flushCache. 
	 */
	protected synchronized List<T> findComponents() {
		InstanceRegistry<T> ir = spiMap.get(spiClass);
		return ir.getInstances();
	}
	
}
