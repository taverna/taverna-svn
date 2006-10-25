package net.sf.taverna.raven.repository.impl;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;

import junit.framework.TestCase;


public class ArtifactImplTest extends TestCase {
	
	File dir = null;
	LocalRepository r = null;
	
	
	public void setUp() throws IOException {
		dir = LocalRepositoryTest.createTempDirectory();
		r = new LocalRepository(dir);
	}

	public void tearDown() {
		r = null;
		try {
			deleteDirectory(dir);
		} catch (IOException e) {
			//
		}
	}

	
	public void testExclusionDependencies() throws MalformedURLException, InterruptedException, ArtifactStateException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		BasicArtifact mavenReporting = new BasicArtifact("org.apache.maven.reporting",
				"maven-reporting-api","2.0");
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
			System.out.println("  " + dependency);
		}

	}
}
