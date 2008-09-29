package net.sf.taverna.t2.workflowmodel.invocation.impl;

import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceGenerator;

import org.junit.Ignore;
import org.springframework.context.ApplicationContext;

@Ignore
/**
 * Implementation of InvocationContext which pulls a ReferenceService from the
 * inMemoryReferenceServiceContext.xml context definition.
 * 
 * @author Tom Oinn
 */
public class DummyInvocationContext implements InvocationContext {

	private static ApplicationContext context = null;
	
	public DummyInvocationContext() {
		getReferenceService(); //force the context to be created early - otherwise tests seem to randomly fail depending upon the order they are run.
	}

	public synchronized ReferenceService getReferenceService() {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
			this.getReferenceService();
		}
		ReferenceService rs = (ReferenceService) context
				.getBean("referenceService");
		
		return rs;
	}

	public synchronized static T2Reference nextReference() {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
		}
		return ((T2ReferenceGenerator) context.getBean("referenceGenerator"))
				.nextReferenceSetReference();
	}

	public synchronized static T2Reference nextListReference(int depth) {
		if (context == null) {
			context = new RavenAwareClassPathXmlApplicationContext(
					"inMemoryReferenceServiceContext.xml");
		}
		return ((T2ReferenceGenerator) context.getBean("referenceGenerator"))
				.nextListReference(false, depth);
	}
	
	public <T> List<? extends T> getEntities(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
