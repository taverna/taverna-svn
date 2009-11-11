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
package net.sf.taverna.raven.prelauncher;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestClassLocation {

	private static final String CLASS_NAME = "net.sf.taverna.raven.launcher.Launchable";
	private static final String JAR = "launcher-api-1.7-SNAPSHOT.jar";
	private URL classes = getClass().getResource("classes/");
	private URL classesWithSpaces = getClass().getResource(
			"classes with spaces/");
	private URL jar = getClass().getResource("jar/" + JAR);
	private URL jarWithSpaces = (getClass().getResource("jar with spaces/"
			+ JAR));

	@Test
	public void allDiffer() throws Exception {
		Set<Class<?>> foundClasses = new HashSet<Class<?>>();
		foundClasses.add(loadClassFromURL(jar));
		foundClasses.add(loadClassFromURL(jarWithSpaces));
		foundClasses.add(loadClassFromURL(classes));
		foundClasses.add(loadClassFromURL(classesWithSpaces));
		assertEquals(4, foundClasses.size());

		try {
			foundClasses.add(getClass().getClassLoader().loadClass(CLASS_NAME));
			// If it exists in our classloader as well (ie. when running in
			// Eclipse), make sure none of the above found that class instead
			// of the fresh one
			assertEquals(5, foundClasses.size());
		} catch (ClassNotFoundException ex) {
			// OK, didn't exist in our class loader
		}
	}

	@Test(expected = ClassNotFoundException.class)
	public void cantFindClass() throws Exception {
		loadClassFromURL(getClass().getResource("."));
	}

	@Test
	public void classLocationAnonymousClass() throws Exception {
		Class<? extends Object> anonymousClass = new Object() {
		}.getClass();
		assertEquals(myLocation(), ClassLocation
				.getClassLocationURI(anonymousClass));
	}

	@Test
	public void classLocationClasses() throws Exception {
		assertEquals(classes.toURI(), ClassLocation.getClassLocationFile(
				loadClassFromURL(classes)).toURI());
	}

	@Test
	public void classLocationClassesWithSpaces() throws Exception {
		assertEquals(classesWithSpaces.toURI(), ClassLocation
				.getClassLocationFile(loadClassFromURL(classesWithSpaces))
				.toURI());
	}

	@Test
	public void classLocationDirClasses() throws Exception {
		assertEquals(getClass().getResource("classes/").toURI(), (ClassLocation
				.getClassLocationDir(loadClassFromURL(classes))).toURI());
	}

	@Test
	public void classLocationDirClassesWithSpaces() throws Exception {
		assertEquals(
				getClass().getResource("classes with spaces/").toURI(),
				(ClassLocation
						.getClassLocationDir(loadClassFromURL(classesWithSpaces)))
						.toURI());
	}

	@Test
	public void classLocationDirJar() throws Exception {
		assertEquals(getClass().getResource("jar/").toURI(), (ClassLocation
				.getClassLocationDir(loadClassFromURL(jar))).toURI());
	}

	@Test
	public void classLocationDirJarWithSpaces() throws Exception {
		assertEquals(getClass().getResource("jar with spaces/").toURI(),
				(ClassLocation
						.getClassLocationDir(loadClassFromURL(jarWithSpaces)))
						.toURI());
	}

	@Test
	public void classLocationInnerClass() throws Exception {
		assertEquals(myLocation(), ClassLocation
				.getClassLocationURI(InnerClass.class));
	}

	@Test
	public void classLocationJar() throws Exception {
		assertEquals(jar.toURI(), ClassLocation.getClassLocationFile(
				loadClassFromURL(jar)).toURI());
	}

	@Test
	public void classLocationJarWithSpaces() throws Exception {
		assertEquals(jarWithSpaces.toURI(), ClassLocation.getClassLocationFile(
				loadClassFromURL(jarWithSpaces)).toURI());
	}

	@Test
	public void classLocationMe() throws Exception {
		assertEquals(myLocation(), ClassLocation
				.getClassLocationURI(getClass()));
	}

	private URI myLocation() throws URISyntaxException {
		String canonical = getClass().getCanonicalName();
		String classPath = "/" + canonical.replace(".", "/") + ".class";
		URI classURI = getClass().getResource(classPath).toURI();
		int dirCount = canonical.split("[.]").length;
		URI locationURI = classURI;
		for (int i=1; i<dirCount ; i++) { 
			locationURI = locationURI.resolve("..");
		}
		return locationURI;
	}

	@Test
	public void classLocationURIClasses() throws Exception {
		assertEquals(classes.toURI(), (ClassLocation
				.getClassLocationURI(loadClassFromURL(classes))));
	}

	@Test
	public void classLocationURIClassesWithSpaces() throws Exception {
		assertEquals(classesWithSpaces.toURI(), (ClassLocation
				.getClassLocationURI(loadClassFromURL(classesWithSpaces))));
	}

	@Test
	public void classLocationURIJar() throws Exception {
		assertEquals(jar.toURI(), (ClassLocation
				.getClassLocationURI(loadClassFromURL(jar))));
	}

	@Test
	public void classLocationURIJarWithSpaces() throws Exception {
		assertEquals(jarWithSpaces.toURI(), (ClassLocation
				.getClassLocationURI(loadClassFromURL(jarWithSpaces))));
	}

	@Test
	public void loadClassClasses() throws Exception {
		loadClassFromURL(classes);
	}

	@Test
	public void loadClassClassesWithSpaces() throws Exception {
		loadClassFromURL(classesWithSpaces);
	}

	@Test
	public void loadClassJar() throws Exception {
		loadClassFromURL(jar);
	}

	@Test
	public void loadClassJarWithSpaces() throws Exception {
		loadClassFromURL(jarWithSpaces);
	}

	@Test
	public void notNull() throws Exception {
		assertNotNull(classes);
		assertNotNull(classesWithSpaces);
		assertNotNull(jar);
		assertNotNull(jarWithSpaces);
	}

	private Class<?> loadClassFromURL(URL url) throws ClassNotFoundException {
		return new URLClassLoader(new URL[] { url }, null)
				.loadClass(CLASS_NAME);
	}

	public class InnerClass {
	}

}
