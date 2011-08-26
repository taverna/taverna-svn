/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import junit.framework.TestCase;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * @author Tom
 *
 */
public class LocalRepositoryTest extends TestCase {

	File dir = null;
	LocalRepository r = null;
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.Repository(File)'
	 */
	public void testRepository() {
		dir.delete();
		new LocalRepository(dir);
	}
	
	public void setUp() throws IOException {
		dir = createTempDirectory();
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

	public void testCreateEmpty() {
		// Should create directory if it is not existing
		dir.delete();
		assertFalse(dir.isDirectory());
		LocalRepository rep = new LocalRepository(dir);
		assertTrue(dir.isDirectory());
	}


	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.addArtifact(Artifact)'
	 */
	public void testAddArtifact() throws MalformedURLException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		BasicArtifact batik = new BasicArtifact("batik","batik-swing","1.6");
		r.addArtifact(batik);
		File batikDir = new File(dir, "batik/batik-swing");
		assertTrue(batikDir.isDirectory());
		// Assumes batik-swing don't have any dependencies
		assertEquals(1, r.getArtifacts().size());
	}

	public void testRepositoryWithExistingContents() throws MalformedURLException {	
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(new BasicArtifact("batik","batik-swing","1.6"));
		r.update();
		LocalRepository r2 = new LocalRepository(dir);
		assertTrue(r.getArtifacts().containsAll(r2.getArtifacts()));
	}
	
	public void testCleanEmpty() {
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
	
	
//	public void testCleanUnknown() throws IOException {
//		File wrongFile = new File(dir, "wrongfile");
//		wrongFile.createNewFile();
//		assertTrue(wrongFile.isFile());
//		r.clean(false, false);
//		// Should not touch it, it's unknown
//		assertTrue(wrongFile.exists());
//		// unless we ask to remove unknowns
//		r.clean(false, true);
//		assertFalse(wrongFile.exists());
//	}
//	
//	public void testCleanNonEmpty() throws MalformedURLException {
//		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
//		BasicArtifact batik = new BasicArtifact("batik","batik-swing","1.6");
//		r.addArtifact(batik);
//		File batikDir = new File(dir, "batik/batik-swing");
//		r.clean(false, false);
//		// Did not touch our batik-swing
//		assertTrue(batikDir.isDirectory());
//	}
	
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.update()'
	 */
	public void testUpdate() throws MalformedURLException {
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
	public void testUpdateWithDots() throws MalformedURLException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		Artifact a = new BasicArtifact("org.xbean","xbean-kernel","2.1");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.Ready));
	}
	
	public void testUpdateWithPackageOnlyPom() throws MalformedURLException {
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		Artifact a = new BasicArtifact("org.codehaus.xfire","xfire-parent","1.0");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.PomNonJar));
	}
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.getLoader(Artifact, ClassLoader)'
	 */
	public void testGetLoader() throws MalformedURLException, ClassNotFoundException, ArtifactNotFoundException, ArtifactStateException {
		Artifact a = new BasicArtifact("batik","batik-rasterizer","1.6");
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(a);
		r.update();
		ClassLoader cl = r.getLoader(a, null);
		cl.loadClass("org.apache.batik.apps.rasterizer.Main");
	}

}
