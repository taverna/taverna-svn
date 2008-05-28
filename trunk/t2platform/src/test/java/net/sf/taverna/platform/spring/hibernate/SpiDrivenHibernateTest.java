package net.sf.taverna.platform.spring.hibernate;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Basic tests to check I've got Hibernate and Derby up and running before
 * messing around with Raven's classloader 'fun'
 * 
 * @author Tom Oinn
 * 
 */
public class SpiDrivenHibernateTest {

	private static Log log = LogFactory.getLog(SpiDrivenHibernateTest.class);

	@Test
	/**
	 * Currently doesn't work as Stian changed how Raven works without telling
	 * anyone or changing the docs.
	 */
	public void testSpiBasedDerbyInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext4.xml");
		Repository rep = (Repository) context.getBean("ravenRepository");
		System.out.println(rep.getStatus(new BasicArtifact(
				"net.sf.taverna.t2.testing", "t2platform-testhelpers-h3-api",
				"0.1-SNAPSHOT")));
		for (Artifact a : rep.getArtifacts()) {
			System.out.println(a.toString());
		}
		SpiRegistry reg = (SpiRegistry) (context.getBean("spiBean"));
		for (Class<?> theClass : reg) {
			System.out.println(theClass.getCanonicalName());
		}
		context.getBean("exampleSessionFactory");
	}
}
