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
 * @author Stian Soiland
 *
 */
public class LocalRepositoryTest {

	File dir = null;
	LocalRepository r = null;
	URL mavenMirror;
	
	public LocalRepositoryTest() throws MalformedURLException {
		super();
		mavenMirror = new URL("http://maven.sateh.com/repository/");
	}
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.Repository(File)'
	 */
	@Test
	public void testRepository() {
		dir.delete();
		new LocalRepository(dir);
	}
	
	@Before
	public void setUp() throws IOException {
		dir = createTempDirectory().getAbsoluteFile();
		// reset static members that screw up/fake testing
		LocalRepository.loaderMap.clear();
		LocalRepository.repositoryCache.clear();
		r = new LocalRepository(dir);
		System.setProperty("raven.profile", "http://www.mygrid.org.uk/taverna/updates/1.5/taverna-1.5.1.0-profile.xml");
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

	@Test
	public void createEmpty() {
		// Should create directory if it is not existing
		dir.delete();
		assertFalse(dir.isDirectory());
		new LocalRepository(dir);
		assertTrue(dir.isDirectory());
	}


	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.addArtifact(Artifact)'
	 */
	@Test
	public void addArtifact() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		BasicArtifact batik = new BasicArtifact("batik","batik-swing","1.6");
		File batikDir = new File(dir, "batik/batik-swing/1.6");
		assertFalse(batikDir.isDirectory());
		r.addArtifact(batik);
		r.update();
		assertTrue(batikDir.isDirectory());
		// Assumes batik-swing don't have any dependencies
		assertEquals(1, r.getArtifacts().size());
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
		String shouldStartWith = "jar:" + junitJar.toURL();
		assertTrue("Did not start with " + shouldStartWith + ": "
			+ junitResource, junitResource.toString().startsWith(
			shouldStartWith));
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
		File m2Repo =
			new File(System.getProperty("user.home"), ".m2/repository");
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

		r.addRemoteRepository(m2Repo.toURL());
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
		String shouldStartWith = "jar:" + junitRepo.toURL();
		assertTrue("Did not start with " + shouldStartWith + ": "
			+ junitResource, junitResource.toString().startsWith(
			shouldStartWith));

	}

	@Test
	public void repositoryWithExistingContents() throws MalformedURLException {	
		r.addRemoteRepository(mavenMirror);
		r.addArtifact(new BasicArtifact("batik","batik-swing","1.6"));
		r.update();
		LocalRepository r2 = new LocalRepository(dir);
		assertTrue(r.getArtifacts().containsAll(r2.getArtifacts()));
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
	
	
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.update()'
	 */
	@Test
	public void update() throws MalformedURLException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(new BasicArtifact("batik","batik-rasterizer","1.6"));
		r.update();
	}
	
	/*
	 * Test method for error from groupID with '.' in
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 */
	@Test
	public void updateWithDots() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		Artifact a = new BasicArtifact("org.xbean","xbean-kernel","2.1");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.Ready));
	}
	
	@Test
	public void updateWithPackageOnlyPom() throws MalformedURLException {
		r.addRemoteRepository(mavenMirror);
		Artifact a = new BasicArtifact("org.codehaus.xfire","xfire-parent","1.0");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.PomNonJar));
	}
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.getLoader(Artifact, ClassLoader)'
	 */
	@Test
	public void getLoader() throws MalformedURLException, ClassNotFoundException, ArtifactNotFoundException, ArtifactStateException {
		Artifact a = new BasicArtifact("batik","batik-rasterizer","1.6");
		r.addRemoteRepository(mavenMirror);
		r.addArtifact(a);
		r.update();
		ClassLoader cl = r.getLoader(a, null);
		cl.loadClass("org.apache.batik.apps.rasterizer.Main");
	}

}
