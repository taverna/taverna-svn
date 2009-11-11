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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.LogInterface;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test support for various uses of Maven properties such as ${project.version}.
 * <p>
 * Based on real examples dumped into the test classpath 
 * at /net/sf/taverna/raven/repository/cxf-repository.
 * 
 * @author Stian Soiland-Reyes
 *
 */


public class PomPropertiesTest {

	public static File createTempDirectory() throws IOException {
		File tempFile;
		try {
			tempFile = File.createTempFile("raven", "test");
			// But we want a directory!
		} catch (IOException e) {
			System.err.println("Could not create temporary directory");
			throw e;
		}
		tempFile.delete();
		assert tempFile.mkdir();
		return tempFile;
	}

	@BeforeClass
	public static void setRavenConsoleLogging() {
		Log.setImplementation(new ConsoleLog());
		ConsoleLog.level = LogInterface.Priority.DEBUG;
	}

	private LocalRepository repository;

	private File repositoryDir;

	private URL repositoryURL;

	@After
	public void deleteRepositoryDir() {
		repository = null;
		if (repositoryDir == null) {
			return;
		}
		try {
			FileUtils.deleteDirectory(repositoryDir);
		} catch (IOException e) {
			// OK
		}
		repositoryDir = null;
	}

	@Before
	public void findRepository() throws IOException {
		// Fake repository in classpath
		String repLoc = "/cxf-repository.jar";
		URL repUrl = getClass().getResource(repLoc);
		assertNotNull("Could not find repository " + repUrl, repUrl);
		repositoryURL = new URL("jar:" + repUrl.toExternalForm() + "!/");

		repositoryDir = createTempDirectory().getAbsoluteFile();
		System.out.println(repositoryDir);
		assertNotNull("Repository dir was null", repositoryDir);
		// reset static members that screw up/fake testing
		LocalRepository.loaderMap.clear();
		LocalRepository.repositoryCache.clear();
		repository = new LocalRepository(repositoryDir);

		// Use fake repository instead for increased
		repository.addRemoteRepository(repositoryURL);

		// Real repositories - should not be needed unless
		// repositoryURL is missing

		/*
		 * repository.addRemoteRepository(new URL(
		 * "http://people.apache.org/repo/m2-snapshot-repository/"));
		 * repository.addRemoteRepository(new URL(
		 * "http://people.apache.org/repo/m2-incubating-repository/"));
		 * repository.addRemoteRepository(new URL(
		 * "http://metagenome.ncl.ac.uk/fluxions/repo-snapshot/"));
		 * repository.addRemoteRepository(new URL(
		 * "http://metagenome.ncl.ac.uk/fluxions/repo/"));
		 * repository.addRemoteRepository(new URL(
		 * "http://maven2.mirrors.skynet.be/pub/maven2/"));
		 * repository.addRemoteRepository(new URL(
		 * "http://mirrors.ibiblio.org/pub/mirrors/maven2/"));
		 */

	}

	@Test
	public void isValidRepository() throws Exception {
		URL pomURL = new URL(repositoryURL,
				"org/apache/cxf/cxf/2.1-SNAPSHOT/cxf-2.1-SNAPSHOT.pom");
		pomURL.openStream().close();
	}

	@Test
	public void loadInstantSoap() throws Exception {
		BasicArtifact cxfRtBindingsSoap = new BasicArtifact("org.apache.cxf",
				"cxf-rt-bindings-soap", "2.1-SNAPSHOT");
		repository.addArtifact(cxfRtBindingsSoap);
		repository.update();
		assertEquals("Could not get cxf-rt-bindings-soap",
				ArtifactStatus.Ready, repository.getStatus(cxfRtBindingsSoap));
	}

	@Test
	public void loadStaxThroughAxiom() throws ArtifactStateException {
		BasicArtifact axiomImpl = new BasicArtifact(
				"org.apache.ws.commons.axiom", "axiom-impl", "1.2.7");
		repository.addArtifact(axiomImpl);
		repository.update();
		List<Artifact> artifacts = repository.getArtifacts();
		// Hack to extract the ArtifactImpl
		@SuppressWarnings("unused")
		ArtifactImpl a = (ArtifactImpl) artifacts.get(artifacts
				.indexOf(axiomImpl));
		/*
		 * for (ArtifactImpl dep : a.getDependencies()) { System.out.println(dep + " " +
		 * repository.getStatus(dep)); }
		 */

		assertEquals("Could not get " + axiomImpl, ArtifactStatus.Ready,
				repository.getStatus(axiomImpl));

	}
}
