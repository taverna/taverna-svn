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
package net.sf.taverna.raven;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.RepositoryListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoaderTest {

	public static final String MAVEN_MYGRID_REPOSITORY = "http://www.mygrid.org.uk/maven/repository/";
	public static final String MAVEN_MYGRID_SNAPSHOT_REPOSITORY = "http://www.mygrid.org.uk/maven/snapshot-repository/";
	public static final String MAVEN_MYGRID_REPO1_REPOSITORY = "http://www.mygrid.org.uk/maven/repo1/repository/";
	public static final String MAVEN_UNIONTRANSIT_REPOSITORY = "http://www.uniontransit.com/apache/maven-repository/";

	public static final String repositoryLocation = MAVEN_MYGRID_REPOSITORY;

	File dir;

	@Before
	public void createTempDirectory() {
		try {
			dir = File.createTempFile("raven", "");
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			e.printStackTrace();
			dir = null;
			return;
		}
		// But we want a directory!
		dir.delete();
		assertTrue(dir.mkdir());
	}

	@After
	public void deleteTempDirectory() throws IOException {
		System.gc();
		System.gc();
		try {
			deleteDirectory(dir);
		} catch (Exception ex) {
			System.err.println("Could not delete directory " + dir + " - probably locking problem");
		}
	}

	// fixme: name this back as testDynamic once this is deployed
	// fixme: this is an integration test as it contacts a repo that relies on
	// the code having already been successfully built and deployed - consider
	// splitting into a unit test as above and a seperate integration test
	/**
	 * Test dynamic (reflection based) access to a remote raven repository
	 * without using any reference to the raven API
	 * 
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */

	@SuppressWarnings("unchecked")
	@Test
	public void shouldTestDynamic() throws MalformedURLException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, InterruptedException {

		// Create a remote classloader referencing the raven jar within a
		// repository

		String artifactLocation = "uk/org/mygrid/taverna/raven/raven/1.5.1/raven-1.5.1.jar";
		ClassLoader c = new URLClassLoader(new URL[] { new URL(
				repositoryLocation + artifactLocation) }, null);

		// Reference to the Loader class within net.sf.taverna.raven
		Class loaderClass = c.loadClass("net.sf.taverna.raven.Loader");
		// Find the single static method provided by the loader
		Method m = loaderClass.getDeclaredMethod("doRavenMagic", String.class,
				File.class, URL[].class, String.class, String.class,
				String.class, String.class, URL.class, int.class);

		// Parameters for the Raven loader call
		String ravenVersion = "1.5.1";
		URL[] remoteRepositories = new URL[] { new URL(repositoryLocation),
				new URL(MAVEN_MYGRID_REPO1_REPOSITORY) };
		String groupID = "uk.org.mygrid.taverna";
		String artifactID = "taverna-workbench";
		String version = "1.5.1";
		int minimumDisplayTime = 10 * 1000; // Ten seconds
		String targetClassName = "org.embl.ebi.escience.scuflui.workbench.Workbench";

		// Construct array for dynamic invocation
		// fixme: this will need changing when a mvn deploy is run

		// Call method via reflection, 'null' target as this is a static method
		Class workbenchClass = (Class) m.invoke(null, ravenVersion, dir,
				remoteRepositories, groupID, artifactID, version,
				targetClassName, null, // splashScreenImage,
				minimumDisplayTime);

		// Verify that the class is loaded and that the classloader is being
		// driven off Raven's artifact system
		assertEquals("org.embl.ebi.escience.scuflui.workbench.Workbench",
				workbenchClass.getName());
		String classLoaderName = workbenchClass.getClassLoader().toString()
				.split(" ")[0];
		assertEquals("loader{uk.org.mygrid.taverna:taverna-workbench:1.5.1}",
				classLoaderName);

		// Could invoke the workbench's main method here as follows :
		//
		// Method workbenchMain =
		// workbenchClass.getDeclaredMethod("main",String[].class);
		// workbenchMain.invoke(null,new Object[]{new String[0]});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDynamicNonGui() throws ClassNotFoundException,
			NoSuchMethodException, MalformedURLException,
			IllegalAccessException, InvocationTargetException,
			ArtifactNotFoundException, ArtifactStateException {

		// Create a remote classloader referencing the raven jar within a
		// repository
		String artifactLocation = "uk/org/mygrid/taverna/raven/raven/1.5.1/raven-1.5.1.jar";
		ClassLoader c = new URLClassLoader(new URL[] { new URL(
				repositoryLocation + artifactLocation) }, null);

		// Reference to the Loader class within net.sf.taverna.raven
		c.loadClass("net.sf.taverna.raven.Loader");
		// Find the single static method provided by the loader

		// Parameters for the Raven loader call
		URL[] remoteRepositories = new URL[] { new URL(repositoryLocation),
				new URL(MAVEN_MYGRID_REPO1_REPOSITORY) };
		String groupID = "uk.org.mygrid.taverna";
		String artifactID = "taverna-workbench";
		String version = "1.5.1";
		String targetClassName = "org.embl.ebi.escience.scuflui.workbench.Workbench";

		RepositoryListener listener = new RepositoryListener() {
			public void statusChanged(Artifact a, ArtifactStatus oldStatus,
					ArtifactStatus newStatus) {
				if (newStatus != null) {
					/*
					 * System.out.println( "change in status: " + a + " changed
					 * from " + oldStatus + " to " + newStatus);
					 */
				}
			}
		};

		// Call method via reflection, 'null' target as this is a static method
		Class workbenchClass = Loader.doRavenMagic(dir, remoteRepositories,
				groupID, artifactID, version, targetClassName, listener);

		// Verify that the class is loaded and that the classloader is being
		// driven off Raven's artifact system
		assertEquals("org.embl.ebi.escience.scuflui.workbench.Workbench",
				workbenchClass.getName());

		String classLoaderName = workbenchClass.getClassLoader().toString()
				.split(" ")[0];
		assertEquals("loader{uk.org.mygrid.taverna:taverna-workbench:1.5.1}",
				classLoaderName);

		// Could invoke the workbench's main method here as follows :
		//
		// Method workbenchMain =
		// workbenchClass.getDeclaredMethod("main",String[].class);
		// workbenchMain.invoke(null,new Object[]{new String[0]});
	}

	/**
	 * Test whether we can launch the workbench from a local raven jar
	 * 
	 * @throws MalformedURLException
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	@Test
	public void testNonDynamic() throws MalformedURLException,
			ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		// UIManager.setLookAndFeel(UIManager
		// .getSystemLookAndFeelClassName());
		Loader.doRavenMagic("1.5.1", dir, new URL[] {
				new URL(repositoryLocation),
				new URL(MAVEN_MYGRID_REPO1_REPOSITORY) },
				"uk.org.mygrid.taverna", "taverna-workbench", "1.5.1",
				"org.embl.ebi.escience.scuflui.workbench.Workbench", null,
				10000);
		// System.out.println(workbenchClass.toString());
		// System.out.println(workbenchClass.getClassLoader().toString());
		/**
		 * System.out.println("\n\nRepository state dump : \n"); LocalRepository
		 * r = LocalRepository.getRepository(dir); for (Artifact a :
		 * r.getArtifacts()) { ArtifactImpl ai = (ArtifactImpl)a;
		 * System.out.println(ai.toString()+" ## "+r.getStatus(ai).toString());
		 * for (ArtifactImpl dep : ai.getDependencies()) { System.out.println("
		 * "+dep.toString()+" -- "+r.getStatus(dep).toString()); } }
		 */
	}

}
