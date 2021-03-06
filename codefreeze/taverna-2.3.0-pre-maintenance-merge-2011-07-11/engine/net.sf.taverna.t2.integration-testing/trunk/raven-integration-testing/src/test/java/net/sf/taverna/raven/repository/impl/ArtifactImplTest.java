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
package net.sf.taverna.raven.repository.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.sf.taverna.raven.LoaderTest;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ArtifactImplTest {

	File dir = null;
	LocalRepository r = null;
	URL testRepos = null;

	@Before
	public void setUp() throws IOException {
		dir = LocalRepositoryTest.createTempDirectory();
		r = new LocalRepository(dir);
		// To get "repository" under test-classes
		
		String repLoc = "/raventest-repository.jar";
		URL repUrl = getClass().getResource(repLoc);
		assertNotNull("Could not find repository " + repUrl, repUrl);
		testRepos = new URL("jar:" + repUrl.toExternalForm() + "!/");
		
//		testRepos = this.getClass().getResource(
//				"/net/sf/taverna/raven/repository/raventest/..");
		System.setProperty("raven.profile",
						"http://www.mygrid.org.uk/taverna/updates/1.5.2/taverna-1.5.2.0-profile.xml");
	}

	@After
	public void tearDown() {
		r = null;
		try {
			if (false) deleteDirectory(dir);
		} catch (IOException e) {
			//
		}
	}

	@SuppressWarnings("null")
	@Ignore
	@Test
	public void testBocaPropertiesInterpolation() throws MalformedURLException,
			InterruptedException, ArtifactStateException, FileNotFoundException {
		r.addRemoteRepository(new URL(
				"http://repository.aduna-software.org/maven2"));
		r.addRemoteRepository(new URL(
				"http://people.apache.org/repo/m2-incubating-repository/"));
		r.addRemoteRepository(new URL(LoaderTest.MAVEN_MYGRID_REPOSITORY));
		r
				.addRemoteRepository(new URL(
						LoaderTest.MAVEN_MYGRID_REPO1_REPOSITORY));
		r.addRemoteRepository(testRepos);
		BasicArtifact bocaTest = new BasicArtifact("raventest", "bocatest",
				"2.4");
		r.addArtifact(bocaTest);
		r.update();
		List<Artifact> artifacts = r.getArtifacts();
		ArtifactImpl artifactImpl = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(bocaTest)) {
				artifactImpl = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + bocaTest, artifactImpl);
		List<ArtifactImpl> dependencies = artifactImpl.getDependencies();
		assertEquals(1, dependencies.size());
		ArtifactImpl bocaServer = dependencies.get(0);
		assertEquals("boca-server", bocaServer.getArtifactId());
		assertEquals("uk.org.mygrid.resources", bocaServer.getGroupId());
		assertEquals("2.4", bocaServer.getVersion());

		r.addArtifact(bocaServer);
		r.update();
		artifacts = r.getArtifacts();
		ArtifactImpl retrievedBocaServer = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(bocaServer)) {
				retrievedBocaServer = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + bocaServer, retrievedBocaServer);
		List<ArtifactImpl> bocaServerDependencies = retrievedBocaServer
				.getDependencies();
		assertTrue("No boca-server dependency found", bocaServerDependencies
				.size() > 0);
		assertEquals(13, bocaServerDependencies.size());
		ArtifactImpl activemq = null;
		for (ArtifactImpl bocaServerDependency : bocaServerDependencies) {
			if (bocaServerDependency.getGroupId().equals("org.apache.activemq")) {
				activemq = bocaServerDependency;
				break;
			}
		}
		assertNotNull(activemq);
	}

	@SuppressWarnings("null")
	@Test
	public void testDependenciesWithPropertiesInterpolation()
			throws MalformedURLException, InterruptedException,
			ArtifactStateException {
		r.addRemoteRepository(new URL(
						LoaderTest.MAVEN_MYGRID_REPO1_REPOSITORY));
		r.addRemoteRepository(testRepos);
		BasicArtifact exclusionTest = new BasicArtifact("raventest",
				"exclusiontest", "1.6.0");
		r.addArtifact(exclusionTest);
		r.update();
		List<Artifact> artifacts = r.getArtifacts();
		// Find the ArtifactImpl for maven-reporting-api
		ArtifactImpl artifactImpl = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(exclusionTest)) {
				artifactImpl = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + exclusionTest, artifactImpl);
		List<ArtifactImpl> dependencies = artifactImpl.getDependencies();
		assertEquals(1, dependencies.size());
		ArtifactImpl dom4j = dependencies.get(0);
		assertEquals("dom4j", dom4j.getArtifactId());
		assertEquals("dom4j", dom4j.getGroupId());
		assertEquals("1.5", dom4j.getVersion());

		r.addArtifact(dom4j);
		r.update();
		artifacts = r.getArtifacts();
		ArtifactImpl retrievedDom4j = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(dom4j)) {
				retrievedDom4j = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + dom4j, retrievedDom4j);
		List<ArtifactImpl> dom4jDependencies = retrievedDom4j.getDependencies();
		assertEquals(5, dom4jDependencies.size());
	}

	@Test
	@SuppressWarnings("null")
	public void testExclusionDependencies() throws MalformedURLException,
			InterruptedException, ArtifactStateException {
		r
				.addRemoteRepository(new URL(
						LoaderTest.MAVEN_MYGRID_REPO1_REPOSITORY));
		r.addRemoteRepository(testRepos);
		BasicArtifact exclusionTest = new BasicArtifact("raventest",
				"exclusiontest", "1.5.1");
		r.addArtifact(exclusionTest);
		r.update();
		List<Artifact> artifacts = r.getArtifacts();
		// Find the ArtifactImpl for maven-reporting-api
		ArtifactImpl artifactImpl = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(exclusionTest)) {
				artifactImpl = (ArtifactImpl) artifact;
			} else {
				ArtifactImpl resolved = (ArtifactImpl) artifact;
				assertNotNull("Should have inherited exclusions: " + resolved,
						resolved.exclusions);
				assertEquals(2, resolved.exclusions.size());
			}
		}
		assertNotNull("Could not find " + exclusionTest, artifactImpl);
		for (ArtifactImpl dependency : artifactImpl.getDependencies()) {
			if (dependency.getGroupId().equals("pull-parser")
					&& dependency.getArtifactId().equals("pull-parser")) {
				fail("Did not exclude pull-parser:pull-parser");
			}
			if (dependency.getGroupId().equals("javax.xml")
					&& dependency.getArtifactId().equals("jsr173")) {
				fail("Did not exclude javax.xml:jsr173");
			}
		}
	}

	@SuppressWarnings("null")
	@Test
	public void testPropertiesInterpolation() throws MalformedURLException,
			InterruptedException, ArtifactStateException, FileNotFoundException {
		r.addRemoteRepository(new URL(
						LoaderTest.MAVEN_MYGRID_REPO1_REPOSITORY));
		r.addRemoteRepository(testRepos);
		BasicArtifact propertiesTest = new BasicArtifact("raventest",
				"propertiestest", "1.6.0");
		r.addArtifact(propertiesTest);
		r.update();
		List<Artifact> artifacts = r.getArtifacts();
		ArtifactImpl artifactImpl = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(propertiesTest)) {
				artifactImpl = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + propertiesTest, artifactImpl);
		List<ArtifactImpl> dependencies = artifactImpl.getDependencies();
		assertEquals(1, dependencies.size());
		ArtifactImpl geronimoSpecs = dependencies.get(0);
		assertEquals("geronimo-j2ee-management_1.0_spec", geronimoSpecs
				.getArtifactId());
		assertEquals("org.apache.geronimo.specs", geronimoSpecs.getGroupId());
		assertEquals("1.0.1", geronimoSpecs.getVersion());

		r.addArtifact(geronimoSpecs);
		r.update();
		artifacts = r.getArtifacts();
		ArtifactImpl retrievedGeronimoSpecs = null;
		for (Artifact artifact : artifacts) {
			if (artifact.equals(geronimoSpecs)) {
				retrievedGeronimoSpecs = (ArtifactImpl) artifact;
				break;
			}
		}
		assertNotNull("Could not find " + geronimoSpecs, retrievedGeronimoSpecs);
		List<ArtifactImpl> geronimoDependencies = retrievedGeronimoSpecs
				.getDependencies();
		assertTrue("No geronimo dependency found",
				geronimoDependencies.size() > 0);
	}

	@Test
	public void testTestRepos() throws IOException {
		URL pom = new URL(testRepos,
				"raventest/exclusiontest/1.5.1/exclusiontest-1.5.1.pom");
		String pomContent = IOUtils.toString(pom.openStream(), "utf8");
		assertTrue(pomContent.contains("<exclusions>"));
		assertTrue(pomContent.contains("jsr173"));
	}
}
