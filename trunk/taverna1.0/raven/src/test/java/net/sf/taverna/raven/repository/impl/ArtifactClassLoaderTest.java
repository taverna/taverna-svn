package net.sf.taverna.raven.repository.impl;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import net.sf.taverna.raven.repository.BasicArtifact;

public class ArtifactClassLoaderTest extends TestCase {

	// Sleep up to 200 ms
	private static final int MAXSLEEP = 300;
	private static final int THREADS = 200;
	private static final String CLASSNAME = "org.apache.log4j.Logger";
	private File dir;
	private LocalRepository repository;
	BasicArtifact commonsLogging;
	BasicArtifact log4j;

	public void setUp() throws IOException {
		Log.setImplementation(new ConsoleLog());
		ConsoleLog.level = Priority.WARN;
		dir = LocalRepositoryTest.createTempDirectory();
		repository = new LocalRepository(dir);
		repository.addRemoteRepository(new URL("http://mirrors.dotsrc.org/maven2/"));
		commonsLogging = new BasicArtifact("commons-logging",
				"commons-logging","1.1");
		log4j = new BasicArtifact("log4j",
				"log4j","1.2.12");
		repository.addArtifact(commonsLogging);
		repository.addArtifact(log4j);
		repository.update();
		ConsoleLog.level = Priority.DEBUG;
		ConsoleLog.console = new PrintStream(new FileOutputStream(new File("/tmp/fish.log"), true));
	}
	
	public void tearDown() throws InterruptedException {
		log4j = null;
		commonsLogging = null;
		repository = null;
//		try {
//			deleteDirectory(dir);
//		} catch (IOException e) {
//			//
//		}
		System.gc();
		Thread.sleep(500);
		System.gc();
		ConsoleLog.console.close();
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
	
	public void testSimpleLoad() throws RavenException, ClassNotFoundException {
		ClassLoader commonsLoader = repository.getLoader(commonsLogging, null);
		ClassLoader log4jLoader = repository.getLoader(log4j, null);
		log4jLoader.loadClass(CLASSNAME);
		commonsLoader.loadClass(CLASSNAME);
	}

	public void testMultipleLoad() throws Throwable {
		final List<Throwable> exceptions = new ArrayList<Throwable>();
		runManyThreads(new Runnable() {
			public void run() {
				try {
					BasicArtifact artifact;
					if (Thread.currentThread().getId() % 2 == 1) {
						artifact = commonsLogging;
					} else {
						artifact = log4j;
					}
					ClassLoader loader;
					loader = repository.getLoader(artifact, null);
					Class c = loader.loadClass(CLASSNAME);
					System.out.println(loader + "  " + c);
				} catch (Throwable e) {
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


	
	
}
