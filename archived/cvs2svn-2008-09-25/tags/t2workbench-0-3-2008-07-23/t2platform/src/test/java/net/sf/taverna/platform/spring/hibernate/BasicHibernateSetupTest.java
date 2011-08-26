package net.sf.taverna.platform.spring.hibernate;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Basic tests to check I've got Hibernate and Derby up and running before
 * messing around with Raven's classloader 'fun'
 * 
 * @author Tom Oinn
 * 
 */
public class BasicHibernateSetupTest {

	@Test
	public void testDerbyInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext3.xml");
		context.getBean("exampleSessionFactory");
	}

}
