package net.sf.taverna.t2.reference;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests construction and use of the ReferenceServiceImpl through spring backed
 * by the InMemory...Dao implementations
 * 
 * @author Tom Oinn
 */
public class InMemoryReferenceServiceTest {

	@SuppressWarnings("unused")
	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		System.out.println("Created reference service implementation :"
				+ rs.getClass().getCanonicalName());
	}
	
}
