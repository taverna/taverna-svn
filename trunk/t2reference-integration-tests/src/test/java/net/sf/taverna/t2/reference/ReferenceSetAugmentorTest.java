package net.sf.taverna.t2.reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test initialization and use of the reference set augmentor implementation
 * 
 * @author Tom Oinn
 */
public class ReferenceSetAugmentorTest {

	@SuppressWarnings("unchecked")
	@Test
	public void doTest() {
		// Initialize application context - there's a lot going on in this one,
		// see the context definition itself as that's really the test case
		// rather than this code
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceSetAugmentorTestContext1.xml");

		// Get the pre-baked reference set
		ReferenceSet rs = (ReferenceSet) context.getBean("referenceSet");

		ReferenceSetAugmentor aug = (ReferenceSetAugmentor) context.getBean("referenceSetAugmentor");
		
		ReferenceContext refContext = new ReferenceContext() {
			public <T> List<? extends T> getEntities(Class<T> arg0) {
				return new ArrayList<T>();
			}
		};
		
		Set<Class<ExternalReferenceSPI>> redTarget = new HashSet<Class<ExternalReferenceSPI>>();
		redTarget.add((Class<ExternalReferenceSPI>) context.getBean("redBean").getClass());
		
		aug.augmentReferenceSet(rs, redTarget, refContext);
		
		
	}

	
	
}
