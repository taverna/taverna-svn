package net.sf.taverna.t2.reference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
				"referenceServiceTestContext1.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");
		System.out.println("Created reference service implementation :"
				+ rs.getClass().getCanonicalName());
	}

	@Test
	public void testURLRegistration() throws MalformedURLException {
		URL testUrl = new URL("http://www.ebi.ac.uk/~tmo/patterns.xml");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceServiceTestContext1.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			T2Reference ref = rs.register(testUrl, 0, true, dummyContext);
			ReferenceSet refSet = (ReferenceSet) rs.resolveIdentifier(ref,
					null, dummyContext);
			System.out.println(refSet.toString() + "  -  "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

}
