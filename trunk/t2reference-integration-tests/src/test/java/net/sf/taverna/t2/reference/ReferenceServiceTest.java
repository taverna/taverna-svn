package net.sf.taverna.t2.reference;

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

	private ReferenceService rs;

	@Test
	public void testInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"referenceServiceTestContext1.xml");
		rs = (ReferenceService) context.getBean("referenceService");
		System.out.println("Created reference service implementation :"
				+ rs.getClass().getCanonicalName());
	}

}
