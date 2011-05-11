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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.raven.appconfig.ApplicationConfig;

import org.junit.Before;
import org.junit.Test;

public class TestApplicationConfig extends AbstractPropThreadTest {

	private static final String APP_CONFIG_NAME = ApplicationConfig.class.getCanonicalName();

	private ApplicationConfig config;

	private URLClassLoader classLoader1;

	private URLClassLoader classLoader3;

	private URLClassLoader classLoader2;

	@Test
	public void getApplicationInheritedTitleSysProps() {
		assertEquals(ApplicationConfig.APP_TITLE, "raven.launcher.app.title");
		System.getProperties().put("raven.launcher.app.name", "appname");
		assertEquals("Title was not correct", "appname", config
				.getTitle());
	}

	@Test
	public void getUnknownName() throws Exception {
		assertTrue("Unexpected name", config.getName().startsWith("unknownApplication-"));
		assertEquals("Unexpected length of name", config.getName().length(), ("unknownApplication-" + UUID.randomUUID().toString()).length());
	}

	@Test
	public void getNameSysProps() {
		assertEquals(ApplicationConfig.APP_NAME, "raven.launcher.app.name");
		System.getProperties().put(ApplicationConfig.APP_NAME, "appname");
		assertEquals("Name was not correct", "appname", config
				.getName());
	}

	@Before
	public void makeClassLoader1() throws ClassNotFoundException {
		List<URL> urls = makeClassPath("TestApplicationConfig-classpath1/");
		classLoader1 = new URLClassLoader(urls.toArray(new URL[0]), null);
		String confName = "conf/" + ApplicationConfig.PROPERTIES;
		assertNotNull("Could not find " + confName
				+ classLoader1.getResource(confName));

		confName = ApplicationConfig.PROPERTIES;
		assertNull("Unexpectedly found " + confName, classLoader1
				.getResource(confName));
	}

	@Before
	public void makeClassLoader2() {
		List<URL> urls = makeClassPath("TestApplicationConfig-classpath2/");
		classLoader2 = new URLClassLoader(urls.toArray(new URL[0]), null);
		String confName = "conf/" + ApplicationConfig.PROPERTIES;
		assertNull("Unexpectedly found " + confName, classLoader2
				.getResource(confName));

		confName = ApplicationConfig.PROPERTIES;
		assertNotNull("Could not find " + confName, classLoader2
				.getResource(confName));
	}

	@Before
	public void makeClassLoader3() {
		List<URL> urls = makeClassPath("TestApplicationConfig-classpath3/");
		classLoader3 = new URLClassLoader(urls.toArray(new URL[0]), null);
		String confName = "conf/" + ApplicationConfig.PROPERTIES;
		assertNotNull("Could not find " + confName, classLoader3
				.getResource(confName));

		confName = ApplicationConfig.PROPERTIES;
		assertNotNull("Could not find " + confName, classLoader3
				.getResource(confName));
	}

	@Test
	public void getNameThreadCntxCL1() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Name was not correct", "classpath1-app", config
				.getName());
	}

	@Test
	public void getTitleThreadCntxCL1() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Title was not correct", "The application of classpath1",
				config.getTitle());
	}

	@Test
	public void getNameThreadCntxCL2() {
		Thread.currentThread().setContextClassLoader(classLoader2);
		assertEquals("Name was not correct", "classpath2-app", config
				.getName());
	}

	@Test
	public void getTitleThreadCntxCL2() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Title was not correct", "The application of classpath1",
				config.getTitle());
	}

	@Test
	public void getNameThreadCntxCL3() {
		Thread.currentThread().setContextClassLoader(classLoader3);
		assertEquals("Name was not correct", "classpath3-app", config
				.getName());
	}

	@Test
	public void getTitleThreadCntxCL3() {
		Thread.currentThread().setContextClassLoader(classLoader3);
		assertEquals("Title was not correct", "classpath3-app", config
				.getTitle());
		// Should be inherited, not picked up from /raven-launcher.properties
	}

	@Test
	public void getNameClassPathCL1() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader1
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getName", (Class[]) null);
		assertEquals("Name was not correct", "classpath1-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getTitleClassPathCL1()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader1
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getTitle", (Class[]) null);
		assertEquals("Name was not correct", "The application of classpath1",
				getAppNameMethod.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getNameClassPathCL2() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader2
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader2, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getName", (Class[]) null);
		assertEquals("Name was not correct", "classpath2-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getTitleClassPathCL2()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader2
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader2, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getTitle", (Class[]) null);
		assertEquals("Name was not correct", "The application of classpath2",
				getAppNameMethod.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getNameClassPathCL3() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader3
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader3, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getName", (Class[]) null);
		assertEquals("Name was not correct", "classpath3-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getTitleClassPathCL3()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader3
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader3, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getTitle", (Class[]) null);
		assertEquals("Name was not correct", "classpath3-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	/**
	 * Make sure that the context class loader (CL2) is always preferred, even if it
	 * only has /raven-launcher.properties and the loading class loader (CL1) has
	 * /conf/raven-launcher-properties.
	 * 
	 */
	@Test
	public void getNameClassPathCL1ctxCL2()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Thread.currentThread().setContextClassLoader(classLoader2);
		Class<?> appConfigClass = classLoader1
				.loadClass(APP_CONFIG_NAME);
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getName", (Class[]) null);
		assertEquals("Name was not correct", "classpath2-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getUnknownTitle() throws Exception {	
		assertEquals("Title of unknown application didn't match name", 
				config.getName(), config.getTitle());		
	}

	@Before
	public void makeApplicationConfig() {
		// Always use a fresh instance for testing
		config = new ApplicationConfig();
	}



}
