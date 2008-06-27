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
 * Tests object registration, identity traversal and render to POJO operations
 * of the ReferenceServiceImpl through spring backed by a Hibernate + Derby Dao
 * set.
 * 
 * @author Tom Oinn
 */
public class RegistrationAndTraversalTest {

	String[] strings = new String[] { "foo", "bar", "urgle", "wibble" };

	@SuppressWarnings("unchecked")
	@Test
	public void testRegisterFromStringList() throws MalformedURLException {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"registrationAndTraversalTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");

		List<Object> objectsToRegister = new ArrayList<Object>();

		// Add four String objects to register
		for (String item : strings) {
			objectsToRegister.add(item);
		}
		// Add a single URL object to register
		objectsToRegister.add(new URL(
				"http://www.ebi.ac.uk/~tmo/defaultMartRegistry.xml"));

		// Register the POJOs with the reference service, should return a
		// T2Reference to a list of five single item reference sets
		T2Reference ref = rs.register(objectsToRegister, 1, true, null);
		System.out.println(ref);

		// Use the traversal operator to get all the children with depth 0, this
		// will iterate over the identifiers of all five previously registered
		// reference sets
		Iterator<ContextualizedT2Reference> refIter = rs.traverseFrom(ref, 0);
		while (refIter.hasNext()) {
			System.out.println(refIter.next());
		}

		// Render to a POJO, this causes each reference to be de-referenced,
		// using the value carrying reference capability if present, or the
		// openstream+StreamToValueConverterSPI if not. In this case the first
		// method is used for the four string references and the openStream for
		// the HttpUrlReference. This should print the contents of the
		// previously registered URL, not the URL itself!
		System.out.println("Retrieve as POJO...");
		List<String> strings = (List<String>) rs.renderIdentifier(ref,
				String.class, null);
		for (String s : strings) {
			System.out.println(s);
		}
		System.out.println("Done.");

	}

}
