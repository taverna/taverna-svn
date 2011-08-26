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
 * Revision           $Revision: 1.6.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-02 12:05:36 $
 *               by   $Author: stain $
 * Created on 1 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.java;

import java.util.List;

import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * SPI discovery class for finding classes that implement LocalWorker
 * 
 * @author Stuart Owen
 */

public class LocalWorkerRegistry extends TavernaSPIRegistry<LocalWorker> {
	
	private static LocalWorkerRegistry instance = new LocalWorkerRegistry();	

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
		return result;
	}
	
	public Class findClassForName(String classname) throws ClassNotFoundException{				
		for (Class c: getSpiRegistry().getClasses()) {
			if (c.getName().equalsIgnoreCase(classname)) {
				return c;
			}
		}		
		throw new ClassNotFoundException();
	}	
}
