package net.sf.taverna.t2.reference;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test initialization and use of the reference set augmentor implementation
 * 
 * @author Tom Oinn
 */
public class ReferenceSetAugmentorTest {

	@Test
	public void doTest() {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetAugmentorTestContext1.xml");

		// Get the pre-baked reference set
		ReferenceSet rs = (ReferenceSet) context.getBean("referenceSet");

		// Print out the reference set for debug
		System.out.println(rs);
	}

}
