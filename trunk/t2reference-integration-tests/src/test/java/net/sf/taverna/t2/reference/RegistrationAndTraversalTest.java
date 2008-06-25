package net.sf.taverna.t2.reference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests object registration and identity traversal operations of the
 * ReferenceServiceImpl through spring backed by the InMemory...Dao
 * implementations
 * 
 * @author Tom Oinn
 */
public class RegistrationAndTraversalTest {

	String[] strings = new String[] { "foo", "bar", "urgle", "wibble" };

	@SuppressWarnings("unused")
	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testRegisterFromStringList() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceContextWithCoreExtensions.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");

		List<String> objectsToRegister = new ArrayList<String>();

		for (String item : strings) {
			objectsToRegister.add(item);
		}
		
		T2Reference ref = rs.register(objectsToRegister, 1, true, dummyContext);
		System.out.println(ref);
		
		Iterator<ContextualizedT2Reference> refIter = rs.traverseFrom(ref,0);
		while (refIter.hasNext()) {
			System.out.println(refIter.next());
		}
		
	}

}
