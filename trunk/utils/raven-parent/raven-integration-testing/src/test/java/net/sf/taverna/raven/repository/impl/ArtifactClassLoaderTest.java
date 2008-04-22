package net.sf.taverna.raven.repository.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.JavaLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.log.LogInterface.Priority;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArtifactClassLoaderTest {

	private static final String SPI = "org.embl.ebi.escience.scuflworkers.ProcessorInfoBean";

	// Sleep up to 200 ms
	private static final int MAXSLEEP = 5000;
	private static final int THREADS = 20;
	private static final int LOOPS = 100;

	private File dir;
	BasicArtifact wsdlProcessor;
	private ClassLoader oldContextLoader;

	private List<String> classes;

	@Before
	public void setUp() throws IOException, ArtifactNotFoundException,
			ArtifactStateException {
		Log.setImplementation(new ConsoleLog());
		ConsoleLog.level = Priority.WARN;
		dir = new File("/tmp/fish");
		dir.mkdir();
		// dir = LocalRepositoryTest.createTempDirectory();
		LocalRepository repository = createRepository();
		ConsoleLog.level = Priority.DEBUG;
		// ConsoleLog.console = new PrintStream(new FileOutputStream(new
		// File("/tmp/fish.log"), true));
		assertEquals("Could not download " + wsdlProcessor,
				ArtifactStatus.Ready, repository.getStatus(wsdlProcessor));

		// SpiRegistry spiRegistry = new SpiRegistry(repository, SPI, getClass()
		// .getClassLoader());
		classes = new ArrayList<String>();
		// for (Class spiClass : spiRegistry.getClasses()) {
		// classes.add(spiClass.getCanonicalName());
		// }
		classes.add("org.embl.ebi.escience.scuflworkers.wsdl.WSDLProcessorInfoBean");
		assertFalse("No SPIs found for " + SPI, classes.isEmpty());

	}

	private LocalRepository createRepository() throws MalformedURLException {
		LocalRepository.loaderMap.clear();
		LocalRepository.repositoryCache.clear();
		LocalRepository repository = new LocalRepository(dir);
		repository.addRemoteRepository(new URL(
				"http://www.mygrid.org.uk/maven/repository/"));
		repository.addRemoteRepository(new URL(
				"http://mirrors.dotsrc.org/maven2/"));
		wsdlProcessor = new BasicArtifact("uk.org.mygrid.taverna.processors",
				"taverna-wsdl-processor", "1.7.1.0");
		repository.addArtifact(wsdlProcessor);
		repository.update();
		return repository;
	}

	@After
	public void tearDown() throws InterruptedException {
		Thread.currentThread().setContextClassLoader(oldContextLoader);
		wsdlProcessor = null;
		// try {
		// FileUtils.deleteDirectory(dir);
		// } catch (IOException e) {
		// // ignore
		// }
		System.gc();
		Thread.sleep(500);
		System.gc();
		ConsoleLog.console.flush();
		Log.setImplementation(new JavaLog());
	}

	public void runManyThreads(final Runnable target) {
		List<Thread> threads = new ArrayList<Thread>();
		final Object lock = new Object();
		for (int i = 0; i < THREADS; i++) {
			threads.add(new Thread("ArtifactClassLoaderTest manythreads") {
				@Override
				public void run() {
					synchronized (lock) {
						try {
							// Make sure we are not alone
							lock.wait(MAXSLEEP);
						} catch (InterruptedException e) {
							System.out.print("X");
						}
					}
					System.out.println(":");
					target.run();
					System.out.print("|");
				}
			});
		}
		for (Thread t : threads) {
			t.start();
		}
		// Start all threads at once
		synchronized (lock) {
			lock.notifyAll();
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

	public void loadClasses(final ClassLoader loader)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		for (String className : classes) {
			Class<?> loadedClass = loader.loadClass(className);
			loadedClass.newInstance();
			System.out.println("OK #" + Thread.currentThread().getId() + " in "
					+ loader);
		}
	}

	@Test
	public void testSimpleLoad() throws Throwable {
		final LocalRepository repository = createRepository();
		final ClassLoader loader = repository.getLoader(wsdlProcessor, null);
		loadClasses(loader);
	}

	@Test
	public void testUnique() throws Throwable {
		final LocalRepository repository1 = createRepository();
		final ClassLoader loader1 = repository1.getLoader(wsdlProcessor, null);

		final LocalRepository repository2 = createRepository();
		final ClassLoader loader2 = repository2.getLoader(wsdlProcessor, null);

		assertTrue("Loaders were not different", loader1 != loader2);

		Class<?> class1 = loader1.loadClass(classes.get(0));
		Class<?> class2 = loader2.loadClass(classes.get(0));

		assertTrue("Classes were not different", class1 != class2);
	}

	@Test
	public void testMultipleLoad() throws Throwable {
		final List<Throwable> exceptions = new ArrayList<Throwable>();
		for (int loop = 0; loop < LOOPS; loop++) {
			System.out.println("Loop " + loop);
			final LocalRepository repository = createRepository();
			final ClassLoader loader = repository
					.getLoader(wsdlProcessor, null);
			runManyThreads(new Runnable() {
				public void run() {
					try {
						loadClasses(loader);
					} catch (Throwable e) {
						System.err.println("FAILED #"
								+ Thread.currentThread().getId() + ": in "
								+ loader);
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
}
