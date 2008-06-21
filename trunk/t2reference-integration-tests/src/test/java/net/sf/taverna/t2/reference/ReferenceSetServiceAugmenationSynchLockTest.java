package net.sf.taverna.t2.reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.Assert.assertEquals;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test initialization and use of the reference set dao and service
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetServiceAugmenationSynchLockTest {

	@SuppressWarnings("unchecked")
	@Test
	public void doAugmentationTest() throws InterruptedException {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetServiceTestContext.xml");

		// Get the reference set service object from the context. At this point
		// it will be wired and configured ready to use.
		ReferenceSetService rss = (ReferenceSetService) context
				.getBean("referenceSetService");

		ReferenceSet rs = (ReferenceSet) context.getBean("referenceSet");

		ReferenceContext refContext = new ReferenceContext() {
			public <T> List<? extends T> getEntities(Class<T> arg0) {
				return new ArrayList<T>();
			}
		};

		Set<Class<ExternalReferenceSPI>> redTarget = new HashSet<Class<ExternalReferenceSPI>>();
		redTarget.add((Class<ExternalReferenceSPI>) context.getBean("redBean")
				.getClass());

		System.out.println(rs);
		ReferenceSet rs2 = rss.registerReferenceSet(rs.getExternalReferences());
		System.out.println(rs2);
		final long startTime = System.currentTimeMillis();
		rss.getReferenceSetWithAugmentationAsynch(rs2.getId(), redTarget,
				refContext, new ReferenceSetServiceCallback() {

					public void referenceSetRetrievalFailed(
							ReferenceSetServiceException arg0) {
						// TODO Auto-generated method stub

					}

					public void referenceSetRetrieved(ReferenceSet arg0) {
						long duration = System.currentTimeMillis() - startTime;
						System.out.println("Thread 1 (" + duration + ") "
								+ arg0);

					}
				});
		rss.getReferenceSetWithAugmentationAsynch(rs2.getId(), redTarget,
				refContext, new ReferenceSetServiceCallback() {

					public void referenceSetRetrievalFailed(
							ReferenceSetServiceException arg0) {
						// TODO Auto-generated method stub

					}

					public void referenceSetRetrieved(ReferenceSet arg0) {
						long duration = System.currentTimeMillis() - startTime;
						System.out.println("Thread 2 (" + duration + ") "
								+ arg0);
					}
				});
		rss.getReferenceSetWithAugmentationAsynch(rs2.getId(), redTarget,
				refContext, new ReferenceSetServiceCallback() {

					public void referenceSetRetrievalFailed(
							ReferenceSetServiceException arg0) {
						// TODO Auto-generated method stub

					}

					public void referenceSetRetrieved(ReferenceSet arg0) {
						long duration = System.currentTimeMillis() - startTime;
						System.out.println("Thread 3 (" + duration + ") "
								+ arg0);
					}
				});
		// Give plenty of time for the test to go wrong (we could join on the
		// callbacks but I'm being lazy)
		Thread.sleep(6000);
		ReferenceSet rs3 = rss.getReferenceSet(rs2.getId());
		System.out.println("After fetch : " + rs3);
		// Reference set should have three references in. If the translators
		// have run out of synch we probably have 7, as the extra red and green
		// references from the translation path will have been added twice.
		assertEquals(rs3.getExternalReferences().size(), 3);

	}

}
