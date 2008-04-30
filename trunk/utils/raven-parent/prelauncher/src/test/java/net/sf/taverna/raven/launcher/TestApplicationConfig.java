package net.sf.taverna.raven.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestApplicationConfig {

	private ApplicationConfig config;

	private Properties oldSysProperties;

	private URLClassLoader classLoader1;

	private ClassLoader oldContextLoader;

	private URLClassLoader classLoader3;

	private URLClassLoader classLoader2;

	@Test
	public void getApplicationInheritedTitleSysProps() {
		assertEquals(ApplicationConfig.APP_TITLE, "raven.launcher.app.title");
		System.getProperties().put("raven.launcher.app.name", "appname");
		assertEquals("Title was not correct", "appname", config
				.getApplicationTitle());
	}

	@Test(expected = IllegalStateException.class)
	public void getApplicationNameFails() throws Exception {
		config.getApplicationName();
	}

	@Test
	public void getApplicationNameSysProps() {
		assertEquals(ApplicationConfig.APP_NAME, "raven.launcher.app.name");
		System.getProperties().put(ApplicationConfig.APP_NAME, "appname");
		assertEquals("Name was not correct", "appname", config
				.getApplicationName());
	}

	private List<URL> makeClassPath(String resourceName) {
		URL resourceCP = getClass().getResource(resourceName);
		ClassLoader appConfigClassLoader = ApplicationConfig.class
				.getClassLoader();
		assertTrue("Our classloader was not a URLClassLoader, can't run test",
				appConfigClassLoader instanceof URLClassLoader);
		URL[] origURLs = ((URLClassLoader) appConfigClassLoader).getURLs();
		List<URL> urls = new ArrayList<URL>();
		urls.add(resourceCP); // first
		urls.addAll(Arrays.asList(origURLs));
		return urls;
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

	@Before
	public void saveContextClassLoader() {
		oldContextLoader = Thread.currentThread().getContextClassLoader();
	}

	@After
	public void restoreContextClassLoader() {
		Thread.currentThread().setContextClassLoader(oldContextLoader);
	}

	@Test
	public void getApplicationNameThreadCntxCL1() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Name was not correct", "classpath1-app", config
				.getApplicationName());
	}

	@Test
	public void getApplicationTitleThreadCntxCL1() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Title was not correct", "The application of classpath1",
				config.getApplicationTitle());
	}

	@Test
	public void getApplicationNameThreadCntxCL2() {
		Thread.currentThread().setContextClassLoader(classLoader2);
		assertEquals("Name was not correct", "classpath2-app", config
				.getApplicationName());
	}

	@Test
	public void getApplicationTitleThreadCntxCL2() {
		Thread.currentThread().setContextClassLoader(classLoader1);
		assertEquals("Title was not correct", "The application of classpath1",
				config.getApplicationTitle());
	}

	@Test
	public void getApplicationNameThreadCntxCL3() {
		Thread.currentThread().setContextClassLoader(classLoader3);
		assertEquals("Name was not correct", "classpath3-app", config
				.getApplicationName());
	}

	@Test
	public void getApplicationTitleThreadCntxCL3() {
		Thread.currentThread().setContextClassLoader(classLoader3);
		assertEquals("Title was not correct", "classpath3-app", config
				.getApplicationTitle());
		// Should be inherited, not picked up from /raven-launcher.properties
	}

	@Test
	public void getApplicationNameClassPathCL1() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader1
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationName", (Class[]) null);
		assertEquals("Name was not correct", "classpath1-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getApplicationTitleClassPathCL1()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader1
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationTitle", (Class[]) null);
		assertEquals("Name was not correct", "The application of classpath1",
				getAppNameMethod.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getApplicationNameClassPathCL2() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader2
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader2, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationName", (Class[]) null);
		assertEquals("Name was not correct", "classpath2-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getApplicationTitleClassPathCL2()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader2
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader2, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationTitle", (Class[]) null);
		assertEquals("Name was not correct", "The application of classpath2",
				getAppNameMethod.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getApplicationNameClassPathCL3() throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader3
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader3, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationName", (Class[]) null);
		assertEquals("Name was not correct", "classpath3-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test
	public void getApplicationTitleClassPathCL3()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Class<?> appConfigClass = classLoader3
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader3, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationTitle", (Class[]) null);
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
	public void getApplicationNameClassPathCL1ctxCL2()
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		Thread.currentThread().setContextClassLoader(classLoader2);
		Class<?> appConfigClass = classLoader1
				.loadClass("net.sf.taverna.raven.launcher.ApplicationConfig");
		assertEquals(classLoader1, appConfigClass.getClassLoader());
		Method getInstanceMethod = appConfigClass.getMethod("getInstance",
				(Class[]) null);
		Object appConfig = getInstanceMethod.invoke((Object) null,
				(Object[]) null);
		Method getAppNameMethod = appConfigClass.getMethod(
				"getApplicationName", (Class[]) null);
		assertEquals("Name was not correct", "classpath2-app", getAppNameMethod
				.invoke(appConfig, (Object[]) null));
	}

	@Test(expected = IllegalStateException.class)
	public void getApplicationTitleFails() throws Exception {
		config.getApplicationTitle();
		System.out.println(System.getProperties());
	}

	@Before
	public void makeApplicationConfig() {
		// Always use a fresh instance for testing
		config = new ApplicationConfig();
	}

	@Before
	public synchronized void removeSysProperties() {
		oldSysProperties = new Properties();
		oldSysProperties.putAll(System.getProperties());
		clearRavenProps();
	}

	@After
	public synchronized void restoreSysProperties() {
		if (oldSysProperties != null) {
			clearRavenProps();
			System.getProperties().putAll(oldSysProperties);
			oldSysProperties = null;
		}
	}

	protected void clearRavenProps() {
		Properties propsCopy = new Properties();
		propsCopy.putAll(System.getProperties());
		for (Object key : propsCopy.keySet()) {
			if (((String) key).startsWith(ApplicationConfig.PREFIX)) {
				System.getProperties().remove(key);
			}
		}
	}

}
