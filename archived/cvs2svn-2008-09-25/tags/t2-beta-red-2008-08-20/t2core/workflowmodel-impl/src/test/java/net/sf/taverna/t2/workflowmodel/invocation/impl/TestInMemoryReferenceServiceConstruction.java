package net.sf.taverna.t2.workflowmodel.invocation.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;

import org.junit.Test;
import org.springframework.context.ApplicationContext;


public class TestInMemoryReferenceServiceConstruction {

	@SuppressWarnings("unused")
	private ReferenceContext dummyContext = new ReferenceContext() {
		public <T> List<? extends T> getEntities(Class<T> arg0) {
			return new ArrayList<T>();
		}
	};

	@Test
	public void testInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");
		System.out.println("Created reference service implementation : "
				+ rs.getClass().getCanonicalName());
	}
	
}
