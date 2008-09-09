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
package net.sf.taverna.raven.appconfig;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


import org.junit.After;
import org.junit.Before;

/**
 * Abstract test superclass that restores context class loader and system
 * properties.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractPropThreadTest {

	private Properties oldSysProperties;
	private ClassLoader oldContextLoader;

	public AbstractPropThreadTest() {
		super();
	}

	protected List<URL> makeClassPath(String resourceName) {
		URL resourceCP = getClass().getResource(resourceName);
		ClassLoader ourClassLoader = getClass()
				.getClassLoader();
		assertTrue("Our classloader was not a URLClassLoader, can't run test",
				ourClassLoader instanceof URLClassLoader);
		URL[] origURLs = ((URLClassLoader) ourClassLoader).getURLs();
		List<URL> urls = new ArrayList<URL>();
		urls.add(resourceCP); // first
		urls.addAll(Arrays.asList(origURLs));
		return urls;
	}

	@Before
	public void saveContextClassLoader() {
		oldContextLoader = Thread.currentThread().getContextClassLoader();
	}

	@After
	public void restoreContextClassLoader() {
		Thread.currentThread().setContextClassLoader(oldContextLoader);
	}

	@Before
	public synchronized void saveSysProperties() {
		oldSysProperties = new Properties();
		oldSysProperties.putAll(System.getProperties());
	}

	@After
	public synchronized void restoreSysProperties() {
		if (oldSysProperties != null) {
			Properties propsCopy = new Properties();
			propsCopy.putAll(System.getProperties());
			
			System.getProperties().putAll(oldSysProperties);
			for (Object key : propsCopy.keySet()) {
				if (! oldSysProperties.containsKey(key)) {
					// Should not be there
					System.getProperties().remove(key);
				}
			}
			oldSysProperties = null;
		}
	}

}
