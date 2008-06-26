package net.sf.taverna.t2.reference;

import java.net.MalformedURLException;
import java.net.URL;
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

	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@SuppressWarnings("unchecked")
	@Test
	public void testRegisterFromStringList() throws MalformedURLException {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");

		List<Object> objectsToRegister = new ArrayList<Object>();

		for (String item : strings) {
			objectsToRegister.add(item);
		}
		objectsToRegister.add(new URL("http://www.ebi.ac.uk/~tmo/defaultMartRegistry.xml"));
		
		T2Reference ref = rs.register(objectsToRegister, 1, true, dummyContext);
		System.out.println(ref);
		
		Iterator<ContextualizedT2Reference> refIter = rs.traverseFrom(ref,0);
		while (refIter.hasNext()) {
			System.out.println(refIter.next());
		}
		
		System.out.println("Retrieve as POJO...");
		List<String> strings = (List<String>) rs.renderIdentifier(ref, String.class, dummyContext);
		for (String s : strings) {
			System.out.println(s);
		}
		System.out.println("Done.");
		
	}

}
