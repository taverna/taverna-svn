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
	}

	
	public void testExclusionDependencies() throws MalformedURLException, InterruptedException, ArtifactStateException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addRemoteRepository(testRepos);
		BasicArtifact mavenReporting = new BasicArtifact("raventest",
				"exclusiontest","1.5.1");
		r.addArtifact(mavenReporting);
		r.update();
		List<Artifact> artifacts = r.getArtifacts();
		// Find the ArtifactImpl for maven-reporting-api
		ArtifactImpl artifactImpl = null;
		for (Artifact artifact : artifacts) {
			System.out.println(artifact);
			if (artifact.equals(mavenReporting)) {
				artifactImpl = (ArtifactImpl) artifact;
			}
		}
		assertNotNull("Could not find " + mavenReporting, artifactImpl);
		for (ArtifactImpl dependency : artifactImpl.getDependencies()) {
			if (dependency.getGroupId().equals("pull-parser") && 
					dependency.getArtifactId().equals("pull-parser")) {
				fail("Did not exclude pull-parser:pull-parser");
			}
			System.out.println("  " + dependency);
		}

	}
}
