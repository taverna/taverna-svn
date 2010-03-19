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
package net.sf.taverna.raven.repository.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.raven.LoaderTest;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
public class LocalRepositoryTest {

	public static File createTempDirectory() throws IOException {
		File tempFile;
		try {
			tempFile = File.createTempFile("raven", "");
			// But we want a directory!
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			throw e;
		}
		tempFile.delete();
		assert tempFile.mkdir();
		return tempFile;
	}
	File dir = null;
	LocalRepository r = null;

	URL mavenMirror;

	public LocalRepositoryTest() throws MalformedURLException {
		super();
		mavenMirror = new URL(LoaderTest.MAVEN_MYGRID_REPO1_REPOSITORY);
	}

	/*
	 * Test method for
	 * 'net.sf.taverna.raven.repository.Repository.addArtifact(Artifact)'
	 */
	@Test
	public void addArtifact() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		BasicArtifact batik = new BasicArtifact("batik", "batik-swing", "1.6");
		File batikDir = new File(dir, "batik/batik-swing/1.6");
		assertFalse(batikDir.isDirectory());
		r.addArtifact(batik);
		r.update();
		assertTrue(batikDir.isDirectory());
		// Assumes batik-swing don't have any dependencies
		assertEquals(1, r.getArtifacts().size());
	}

	@Test
	public void cleanEmpty() {
		File emptyDir = new File(dir, "some/artifact/1.1");
		emptyDir.mkdirs();
		assertTrue(emptyDir.isDirectory());
		r.clean();
		// some/artifact/1.1
		assertFalse(emptyDir.isDirectory());
		// some/artifact
		assertFalse(emptyDir.getParentFile().isDirectory());
		// some
		assertFalse(emptyDir.getParentFile().getParentFile().isDirectory());
		assertTrue(dir.isDirectory());
	}

	@Test
	public void createEmpty() {
		// Should create directory if it is not existing
		dir.delete();
		assertFalse(dir.isDirectory());
		new LocalRepository(dir);
		assertTrue(dir.isDirectory());
	}

	/**
	 * Should not copy from file:/// repositories. Assumes
	 * $HOME/.m2/repositories have junit/junit/4.0, as needed for these tests.
	 * Test will abort if this is not so.
	 * 
	 * @throws ArtifactStateException
	 * @throws ArtifactNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Test
	public void didntMadeLocalJarFile() throws ArtifactNotFoundException,
			ArtifactStateException, ClassNotFoundException, IOException {
		File m2Repo = new File(System.getProperty("user.home"),
				".m2/repository");
		if (!m2Repo.isDirectory()) {
			System.err.println("Warning: Could not test using " + m2Repo);
			return;
		}
		String junitPath = "junit/junit/4.0/junit-4.0.jar";
		File junitRepo = new File(m2Repo, junitPath);
		if (!junitRepo.isFile()) {
			System.err.println("Warning: Could not test using " + junitRepo);
			return;
		}

		r.addRemoteRepository(m2Repo.toURI().toURL());
		BasicArtifact junit = new BasicArtifact("junit", "junit", "4.0");
		File junitJar = new File(dir, junitPath);
		assertFalse("Already existed " + junitJar, junitJar.isFile());
		r.addArtifact(junit);
		r.update();
		assertFalse("Should not have created " + junitJar, junitJar.isFile());

		// instead, it should use junitRepo directly
		junitJar = junitJar.getCanonicalFile();
		ClassLoader loader = r.getLoader(junit, null);
		assertNotNull("ClassLoader was null ", loader);
		URL junitResource = loader.getResource("junit");
		String shouldStartWith = "jar:" + junitRepo.toURI().toURL();
		assertTrue("Did not start with " + shouldStartWith + ": "
				+ junitResource, junitResource.toString().startsWith(
				shouldStartWith));

	}

	/*
	 * Test method for
	 * 'net.sf.taverna.raven.repository.Repository.getLoader(Artifact,
	 * ClassLoader)'
	 */
	@Test
	public void getLoader() throws MalformedURLException,
			ClassNotFoundException, ArtifactNotFoundException,
			ArtifactStateException {
		Artifact a = new BasicArtifact("batik", "batik-rasterizer", "1.6");
		r.addRemoteRepository(mavenMirror);
		r.addArtifact(a);
		r.update();
		ClassLoader cl = r.getLoader(a, null);
		cl.loadClass("org.apache.batik.apps.rasterizer.Main");
	}

	@Test
	public void madeLocalJarFile() throws ArtifactNotFoundException,
			ArtifactStateException, IOException {
		r.addRemoteRepository(mavenMirror);
		BasicArtifact junit = new BasicArtifact("junit", "junit", "4.0");
		File junitJar = new File(dir, "junit/junit/4.0/junit-4.0.jar");
		assertFalse("Already existed " + junitJar, junitJar.isFile());
		r.addArtifact(junit);
		r.update();
		assertTrue("Did not create " + junitJar, junitJar.isFile());
		// To avoid /private/tmp vs /tmp etc.
		junitJar = junitJar.getCanonicalFile();
		ClassLoader loader = r.getLoader(junit, null);
		assertNotNull("ClassLoader was null ", loader);
		URL junitResource = loader.getResource("junit");
		String shouldStartWith = "jar:" + junitJar.toURI().toURL();
		assertTrue("Did not start with " + shouldStartWith + ": "
				+ junitResource, junitResource.toString().startsWith(
				shouldStartWith));
	}

	@Test
	public void repositoryWithExistingContents() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		r.addArtifact(new BasicArtifact("batik", "batik-swing", "1.6"));
		r.update();
		LocalRepository r2 = new LocalRepository(dir);
		assertTrue(r.getArtifacts().containsAll(r2.getArtifacts()));
	}

	@Before
	public void setUp() throws IOException {
		dir = createTempDirectory().getAbsoluteFile();
		// reset static members that screw up/fake testing
		LocalRepository.loaderMap.clear();
		LocalRepository.repositoryCache.clear();
		r = new LocalRepository(dir);
		System
				.setProperty("raven.profile",
						"http://www.mygrid.org.uk/taverna/updates/1.5/taverna-1.5.1.0-profile.xml");
	}

	@After
	public void tearDown() {
		r = null;
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			// OK
		}
	}

	@Test
	public void testPrependRemoteRepository() throws Exception {
		assertEquals(0, r.getRemoteRepositories().size());
		r.prependRemoteRepository(new URL("http://www.google.com"));
		assertEquals(1, r.getRemoteRepositories().size());
		r.prependRemoteRepository(new URL("http://www.yahoo.com"));
		assertEquals(2, r.getRemoteRepositories().size());
		r.prependRemoteRepository(new URL("http://www.bbc.co.uk"));
		assertEquals(3, r.getRemoteRepositories().size());
		r.prependRemoteRepository(new URL("http://www.mygrid.org.uk"));
		assertEquals(4, r.getRemoteRepositories().size());
		int i = 0;
		String[] expectedURLS = new String[] { "http://www.mygrid.org.uk",
				"http://www.bbc.co.uk", "http://www.yahoo.com",
				"http://www.google.com" };
		for (URL url : r.getRemoteRepositories()) {
			assertEquals(expectedURLS[i], url.toExternalForm());
			i++;
		}
	}

	/*
	 * Test method for
	 * 'net.sf.taverna.raven.repository.Repository.Repository(File)'
	 */
	@Test
	public void testRepository() {
		dir.delete();
		new LocalRepository(dir);
	}

	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.update()'
	 */
	@Test
	public void update() throws MalformedURLException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(new BasicArtifact("batik", "batik-rasterizer", "1.6"));
		r.update();
	}

	/*
	 * Test method for error from groupID with '.' in @throws
	 * MalformedURLException @throws ClassNotFoundException @throws
	 * ArtifactNotFoundException @throws ArtifactStateException
	 */
	@Test
	public void updateWithDots() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		Artifact a = new BasicArtifact("org.xbean", "xbean-kernel", "2.1");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.Ready));
	}

	@Test
	public void updateWithPackageOnlyPom() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		Artifact a = new BasicArtifact("org.codehaus.xfire", "xfire-parent",
				"1.0");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.PomNonJar));
	}
}
