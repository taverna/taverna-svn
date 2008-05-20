package net.sf.taverna.platform.spring;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class RavenSpringIntegrationTest {

	@Test
	public void testRavenEnabledBeanConstruction() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext.xml");
		Object o = context.getBean("ravenTestBean");
		assertEquals(o.getClass().getName(),
				"net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce");
	}

}
