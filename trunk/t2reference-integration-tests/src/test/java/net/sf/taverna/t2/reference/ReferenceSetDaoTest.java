package net.sf.taverna.t2.reference;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test initialization and use of the reference set dao and service
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetDaoTest {

	@Test
	public void doTest() {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetDaoTestContext1.xml");

		// Get the reference set service object from the context. At this point
		// it will be wired and configured ready to use.
		ReferenceSetService rss = (ReferenceSetService) context
				.getBean("referenceSetService");

		// Build a simple set of two external references, these have been
		// defined in the application context definition and loaded with raven,
		// the classes are not on the classpath for this test case.
		Set<ExternalReferenceSPI> references = new HashSet<ExternalReferenceSPI>();
		references
				.add((ExternalReferenceSPI) context.getBean("exampleUrlBean"));
		references.add((ExternalReferenceSPI) context
				.getBean("exampleFileBean"));

		// If all goes well we can register the set of external references and
		// get a referenceset object back with an ID allocated appropriately
		ReferenceSet rs = rss.registerReferenceSet(references);
		System.out.println(rs);

	}

}
