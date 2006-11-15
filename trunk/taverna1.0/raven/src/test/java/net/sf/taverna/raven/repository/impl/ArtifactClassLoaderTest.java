package net.sf.taverna.raven.repository.impl;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import net.sf.taverna.raven.RavenException;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;

public class ArtifactClassLoaderTest extends TestCase {

	private File dir;
	private LocalRepository repository;
	BasicArtifact tavernaCore;
	BasicArtifact baclavaCore;

	public void setUp() throws IOException {
		dir = LocalRepositoryTest.createTempDirectory();
		repository = new LocalRepository(dir);
		repository.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		repository.addRemoteRepository(new URL("http://rpc268.cs.man.ac.uk/repository/"));
		tavernaCore = new BasicArtifact("uk.org.mygrid.taverna",
				"taverna-core","1.5-SNAPSHOT");
		baclavaCore = new BasicArtifact("uk.org.mygrid.taverna.baclava",
				"baclava-core","1.5-SNAPSHOT");
		repository.addArtifact(tavernaCore);
		repository.addArtifact(baclavaCore);
		repository.update();
	}
	
	public void tearDown() throws InterruptedException {
		baclavaCore = null;
		tavernaCore = null;
		repository = null;
		try {
			deleteDirectory(dir);
		} catch (IOException e) {
			//
		}
		System.gc();
		Thread.sleep(500);
		System.gc();
	}
	
	public void runManyThreads(final Runnable target) {
		List<Thread> threads = new ArrayList<Thread>();
		Random r = new Random();
		for (int i=0; i<100; i++) {
			// Sleep up to 200 ms
			final int sleep = r.nextInt(200);
			threads.add(new Thread(){
				public void run() {
					// Make sure we are not alone
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						System.out.print("x");
					}
					target.run();
					//System.out.print(".");
				}
			});
		}
		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.print("!");
			}
		}
	}

	
	
	public void testRunManyThreads() {
		final List<Object> runs = new ArrayList<Object>();
		runManyThreads(new Runnable() {
			public void run() {
				synchronized (runs) {
					runs.add(null);							
				}
			}
		});
		assertEquals(100, runs.size());
	}


	
	public void testMultipleLoad() {
		runManyThreads(new Runnable() {
			public void run() {
				BasicArtifact artifact;
				if (Thread.currentThread().getId() % 2 == 1) {
					artifact = tavernaCore;
				} else {
					artifact = baclavaCore;
				}
				
				ClassLoader loader;
				try {
					loader = repository.getLoader(artifact, null);
				} catch (RavenException e) {
					e.printStackTrace();
					return;
				}
				System.out.println("");
				String className = "org.jdom.Document";
				try {
					Class c = loader.loadClass(className);
					System.out.println(loader + "  " + c);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public void testSimpleLoad() throws RavenException, ClassNotFoundException {
		ClassLoader tavernaLoader = repository.getLoader(tavernaCore, null);
		ClassLoader baclavaLoader = repository.getLoader(baclavaCore, null);
		String className = "org.jdom.Document";
		tavernaLoader.loadClass(className);
		baclavaLoader.loadClass(className);
	}
	

	
	
}
