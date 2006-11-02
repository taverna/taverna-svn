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
 * Filename           $RCSfile: LocalWorkerRegistry.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-02 15:36:56 $
 *               by   $Author: sowen70 $
 * Created on 1 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.java;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * SPI discovery class for finding classes that implement LocalWorker
 * 
 * @author Stuart Owen
 */

public class LocalWorkerRegistry extends TavernaSPIRegistry<LocalWorker> {
	
	private static LocalWorkerRegistry instance = new LocalWorkerRegistry();
	private static Map<String,ClassLoader> classloaderMap=new HashMap<String,ClassLoader>();	

	private LocalWorkerRegistry() {
		super(LocalWorker.class);
	}

	/**
	 * Return a static instance of the registry loaded with all available
	 * instances of the ProcessorActionSPI
	 */
	public static synchronized LocalWorkerRegistry instance() {
		
		return instance;
	}
	
	public List<LocalWorker> getLocalWorkers() {
		List<LocalWorker> result = findComponents();
		mapClassloaders(result);
		return result;
	}
	
	/**
	 * adds the local worker classnames to a map, mapping to the classloader.
	 * this is so that the correct classloader can be found when trying to load
	 * that local worker from a scufl xml document.
	 * @param workers
	 */
	private void mapClassloaders(List<LocalWorker> workers) {
		for (LocalWorker worker : workers) {
			classloaderMap.put(worker.getClass().getName(),worker.getClass().getClassLoader());
		}
	}
	
	public static Class findClassForName(String classname) throws ClassNotFoundException{
		ClassLoader loader=classloaderMap.get(classname);
		if (loader==null) { //try the classloader for the current context as a last resort	
			loader=Thread.currentThread().getContextClassLoader();
		}
		return loader.loadClass(classname);
	}	
}
