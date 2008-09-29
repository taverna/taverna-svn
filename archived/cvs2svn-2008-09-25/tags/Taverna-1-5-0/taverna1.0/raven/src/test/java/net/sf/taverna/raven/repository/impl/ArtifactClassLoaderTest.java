package net.sf.taverna.raven.repository.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import net.sf.taverna.raven.RavenException;
import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.JavaLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.LogInterface.Priority;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;

public class ArtifactClassLoaderTest extends TestCase {

	// Sleep up to 200 ms
	private static final int MAXSLEEP = 300;
	private static final int THREADS = 200;
	
	private static final String CLASSNAME = "org.jdom.Namespace";
	//private static final String CLASSNAME = "org.embl.ebi.escience.scufl.XScufl";
	
	private File dir;
	private LocalRepository repository;
	BasicArtifact tavernaCore;
	BasicArtifact baclavaCore;
	private ClassLoader oldContextLoader;
	private BasicArtifact xerces;
	private ClassLoader xerxesLoader;
	private BasicArtifact jdom;

	public void setUp() throws IOException, ArtifactNotFoundException, ArtifactStateException {
		Log.setImplementation(new ConsoleLog());
		ConsoleLog.level = Priority.WARN;
		dir = LocalRepositoryTest.createTempDirectory();
		repository = new LocalRepository(dir);
		repository.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		repository.addRemoteRepository(new URL("http://rpc268.cs.man.ac.uk/repository/"));
		tavernaCore = new BasicArtifact("uk.org.mygrid.taverna",
				"taverna-core","1.5-SNAPSHOT");
		baclavaCore = new BasicArtifact("uk.org.mygrid.taverna.baclava",
				"baclava-core","1.5-SNAPSHOT");
		jdom = new BasicArtifact("jdom", "jdom", "1.0");
		xerces = new BasicArtifact("xerces", "xercesImpl", "2.6.2");
		repository.addArtifact(xerces);
		repository.addArtifact(jdom);
		repository.addArtifact(tavernaCore);
		repository.addArtifact(baclavaCore);
		repository.update();
		ConsoleLog.level = Priority.DEBUG;
		//ConsoleLog.console = new PrintStream(new FileOutputStream(new File("/tmp/fish.log"), true));
		
		// Make xerces available in context class loader
		oldContextLoader = Thread.currentThread().getContextClassLoader();
		xerxesLoader = repository.getLoader(xerces, oldContextLoader);
		Thread.currentThread().setContextClassLoader(xerxesLoader);
	}
	
	public void tearDown() throws InterruptedException {
		Thread.currentThread().setContextClassLoader(oldContextLoader);
		baclavaCore = null;
		tavernaCore = null;
		repository = null;
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			// ignore
		}
		System.gc();
		Thread.sleep(500);
		System.gc();
		ConsoleLog.console.flush();
		Log.setImplementation(new JavaLog());
	}
	
	public void runManyThreads(final Runnable target) {
		List<Thread> threads = new ArrayList<Thread>();
		Random r = new Random();
		for (int i=0; i<THREADS; i++) {
			final int sleep = r.nextInt(MAXSLEEP);
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
		assertEquals(THREADS, runs.size());
	}


	// WARNING: This test don't work from mvn test
	public void testMultipleLoad() throws Throwable {
		final List<Throwable> exceptions = new ArrayList<Throwable>();
		runManyThreads(new Runnable() {
			public void run() {
				BasicArtifact artifact;
				if (Thread.currentThread().getId() % 2 == 1) {
					artifact = tavernaCore;
				} else {
					artifact = baclavaCore;
				}
				ClassLoader loader = null;
				try {
					loader = repository.getLoader(artifact, null);
					Class c = loader.loadClass(CLASSNAME);
					System.out.println(Thread.currentThread().getId() + ": " + loader + 
							" found " + c.getName() + " in " + c.getClassLoader());
				} catch (Throwable e) {
					System.out.println(Thread.currentThread().getId() + ": failed using " + loader);
					synchronized (exceptions) {
						exceptions.add(e);						
					}
				}
			}
		});
		for (Throwable t : exceptions) {
			t.printStackTrace();
		}
		if (exceptions.size() > 0) {
			throw exceptions.get(0);
		}
	}

	// WARNING: This test don't work from mvn test
	public void testSimpleLoad() throws RavenException, ClassNotFoundException {
		ClassLoader jdomLoader = repository.getLoader(jdom, xerxesLoader);
		jdomLoader.loadClass(CLASSNAME);
		ClassLoader baclavaCoreLoader = repository.getLoader(baclavaCore, xerxesLoader);
		ClassLoader tavernaCoreLoader = repository.getLoader(tavernaCore, xerxesLoader);
		tavernaCoreLoader.loadClass(CLASSNAME);
		baclavaCoreLoader.loadClass(CLASSNAME);
	}

	// Try running this test suite as:
	// : stain@mira ~/Documents/workspace/taverna1.x/raven;
	//   java -classpath test-classes/:classes/:/Users/stain/.m2/repository/junit/junit/4.0/junit-4.0.jar net.sf.taverna.raven.repository.impl.ArtifactClassLoaderTest
	
	public static void main(String args[]) {
	      JUnitCore.main(ArtifactClassLoaderTest.class.getName());
	}
	
}
