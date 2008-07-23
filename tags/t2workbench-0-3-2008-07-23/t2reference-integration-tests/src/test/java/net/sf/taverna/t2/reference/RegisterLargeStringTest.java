package net.sf.taverna.t2.reference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests that we can register a large string as a reference in the database,
 * exercising the text sql type
 * 
 * @author Tom Oinn
 */
public class RegisterLargeStringTest {

	@SuppressWarnings("unused")
	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testRegisterFromStringList() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");

		List<String> objectsToRegister = new ArrayList<String>();

		String largeString = getLargeString();
		System.out.println(largeString.length());
		objectsToRegister.add(largeString);
		objectsToRegister.add(largeString);
		objectsToRegister.add(largeString);
		objectsToRegister.add(largeString);
		objectsToRegister.add(largeString);
		objectsToRegister.add(largeString);
		
		
		T2Reference ref = rs.register(objectsToRegister, 1, true, dummyContext);
		System.out.println(ref);

		Iterator<ContextualizedT2Reference> refIter = rs.traverseFrom(ref, 0);
		while (refIter.hasNext()) {
			System.out.println(refIter.next());
		}

	}
	
	private static String getLargeString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10000; i++) {
			sb.append("abcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabcde\n");
		}
		return sb.toString();
	}

}
