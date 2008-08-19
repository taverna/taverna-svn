package net.sf.taverna.t2.reference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests construction and use of the ReferenceServiceImpl through spring
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceServiceTest {

	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		System.out.println("Created reference service implementation :"
				+ rs.getClass().getCanonicalName());
	}

	@Test
	public void testURLRegistration() throws MalformedURLException {
		URL testUrl = new URL("http://www.ebi.ac.uk/~tmo/patterns.xml");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			T2Reference ref = rs.register(testUrl, 0, true, dummyContext);
			ReferenceSet refSet = (ReferenceSet) rs.resolveIdentifier(ref,
					null, dummyContext);
			System.out.println(refSet.toString() + "  -  "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	@Test
	/**
	 * Test multiple concurrent read / write cycles to the database, producing
	 * some rather informal profiling information at the end.
	 */
	public void testSaturatedReadWriteCycle() throws MalformedURLException {
		int concurrentThreads = 5;
		final int jobsPerThread = 200;
		int joinPoints = 5;
		final URL testUrl = new URL("http://www.ebi.ac.uk/~tmo/patterns.xml");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceServiceTestContext.xml");
		//ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
		//				"inMemoryReferenceServiceContextWithCoreExtensions.xml");
		final ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			private int lastCount = getCount();
			private long lastTime = System.currentTimeMillis();

			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				long interval = currentTime - lastTime;
				lastTime = currentTime;
				int newCount = getCount();
				long jobsProcessed = (long) (newCount - lastCount);
				lastCount = newCount;
				if (interval == 0) {
					System.out.println("(infinity) " + jobsProcessed);
				} else {
					System.out.println((int) (jobsProcessed * 1000 / interval));
				}
			}
		}, 1000, 1000);
		long testStartTime = System.currentTimeMillis();
		for (int i = 0; i < joinPoints; i++) {
			Thread[] threads = new Thread[concurrentThreads];
			for (int j = 0; j < concurrentThreads; j++) {
				threads[j] = new Thread() {
					@Override
					public void run() {
						for (int k = 0; k < jobsPerThread; k++) {
							try {
								T2Reference ref = rs.register(testUrl, 0, true,
										dummyContext);
								@SuppressWarnings("unused")
								ReferenceSet refSet = (ReferenceSet) rs
										.resolveIdentifier(ref, null,
												dummyContext);
								incrementRequestsProcessed();
							} catch (ReferenceServiceException rse) {
								System.out.println(rse);
							}
						}
					}
				};
				threads[j].start();
			}
			for (int j = 0; j < concurrentThreads; j++) {
				try {
					threads[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		timer.cancel();
		System.out
				.println("Test completed, using "
						+ concurrentThreads
						+ " threads, "
						+ getCount()
						+ " at "
						+ (long) ((getCount() * 1000) / (System
								.currentTimeMillis() - testStartTime))
						+ " jobs per second averaged over test run");
	}

	private int counter = 0;

	private synchronized void incrementRequestsProcessed() {
		counter++;
	}

	private int getCount() {
		return counter;
	}
}
