/**
 * 
 */
package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import junit.framework.TestCase;

/**
 * @author Tom
 *
 */
public class LocalRepositoryTest extends TestCase {

	static String rLocation = "C:\\repository\\";
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.Repository(File)'
	 */
	public void testRepository() {
		File f = new File(rLocation);
		f.delete();
		new LocalRepository(f);
	}

	
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.addArtifact(Artifact)'
	 */
	public void testAddArtifact() throws MalformedURLException {
		File f = new File(rLocation);
		f.delete();
		LocalRepository r = new LocalRepository(f);
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(new BasicArtifact("batik","batik-swing","1.6"));
	}

	public void testRepositoryWithExistingContents() throws MalformedURLException {
		File f = new File(rLocation);
		f.delete();
		LocalRepository r = new LocalRepository(f);
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(new BasicArtifact("batik","batik-swing","1.6"));
		LocalRepository r2 = new LocalRepository(f);
		assertTrue(r.getArtifacts().containsAll(r2.getArtifacts()));
	}
	
	/*
	 * Test method for 'net.sf.taverna.raven.repository.Repository.update()'
	 */
	public void testUpdate() throws MalformedURLException {
		File f = new File(rLocation);
		f.delete();
		LocalRepository r = new LocalRepository(f);
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
		File f = new File(rLocation);
		f.delete();
		LocalRepository r = new LocalRepository(f);
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		Artifact a = new BasicArtifact("org.xbean","xbean-kernel","2.1");
		r.addArtifact(a);
		r.update();
		assertTrue(r.getArtifacts().contains(a));
		assertTrue(r.getStatus(a).equals(ArtifactStatus.Ready));
	}
	
	public void testUpdateWithPackageOnlyPom() throws MalformedURLException {
		File f = new File(rLocation);
		f.delete();
		LocalRepository r = new LocalRepository(f);
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
		File f = new File(rLocation);
		LocalRepository r = new LocalRepository(f);
		Artifact a = new BasicArtifact("batik","batik-rasterizer","1.6");
		r.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		r.addArtifact(a);
		r.update();
		ClassLoader cl = r.getLoader(a, null);
		cl.loadClass("org.apache.batik.apps.rasterizer.Main");
	}

}
