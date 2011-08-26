package net.sf.taverna.raven.repository.impl;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;

import junit.framework.TestCase;


public class ArtifactImplTest extends TestCase {
	
	File dir = null;
	LocalRepository r = null;
	URL testRepos = null;
	
	public void setUp() throws IOException {
		dir = LocalRepositoryTest.createTempDirectory();
		r = new LocalRepository(dir);
		testRepos = this.getClass().getResource("/net/sf/taverna/raven/repository/");
	}

	public void tearDown() {
		r = null;
		try {
			deleteDirectory(dir);
		} catch (IOException e) {
			//
		}
	}
	
	public void testTestRepos() throws IOException {
		URL pom = new URL(testRepos, "raventest/exclusiontest/1.5.1/exclusiontest-1.5.1.pom");
		String pomContent = IOUtils.toString(pom.openStream(), "utf8");	
		assertTrue(pomContent.contains("<exclusions>"));
		assertTrue(pomContent.contains("jsr173"));
	}

	
	@SuppressWarnings("null")
	public void testExclusionDependencies() throws MalformedURLException, InterruptedException, ArtifactStateException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addRemoteRepository(testRepos);
		BasicArtifact exclusionTest = new BasicArtifact("raventest",
				"exclusiontest","1.5.1");
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
			if (dependency.getGroupId().equals("pull-parser") && 
					dependency.getArtifactId().equals("pull-parser")) {
				fail("Did not exclude pull-parser:pull-parser");
			}
			if (dependency.getGroupId().equals("javax.xml") && 
					dependency.getArtifactId().equals("jsr173")) {
				fail("Did not exclude javax.xml:jsr173");
			}
		}
	}
}
