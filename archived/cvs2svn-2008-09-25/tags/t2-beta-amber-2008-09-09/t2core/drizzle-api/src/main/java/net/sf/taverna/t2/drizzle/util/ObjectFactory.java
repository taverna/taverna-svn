/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.EclipseRepository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.SpiRegistry;

/**
 * @author alanrw
 *
 */
public final class ObjectFactory {

	private static Map<Class<?>,SpiRegistry> registryMap = new HashMap<Class<?>,SpiRegistry>();
	
	@SuppressWarnings("unchecked")
	public static <C> C getInstance(Class<C> objectClass) {
		if (objectClass == null) {
			throw new NullPointerException ("objectClass cannot be null"); //$NON-NLS-1$
		}
		C result = null;
		SpiRegistry registry = registryMap.get(objectClass);
		if (registry == null) {
			registry = new SpiRegistry (getRepository(), objectClass.getName(),
					getClassLoader());
			registryMap.put(objectClass, registry);
		}
		List<Class> classes = registry.getClasses();
		if (classes.size() > 0) {
			try {
				result = (C) classes.get(0).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private static Repository getRepository() {
		if (getClassLoader() instanceof LocalArtifactClassLoader) {
			return ((LocalArtifactClassLoader) getClassLoader())
					.getRepository();
		}
		// Do the ugly hack in one place
		System.setProperty("raven.eclipse", "true");  //$NON-NLS-1$//$NON-NLS-2$
		return new EclipseRepository();
	}

	private static ClassLoader getClassLoader() {
		return ObjectFactory.class.getClassLoader();
	}

}
