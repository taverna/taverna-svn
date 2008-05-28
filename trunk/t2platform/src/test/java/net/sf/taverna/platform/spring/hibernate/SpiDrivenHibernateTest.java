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
 * Uses a patched version of hibernate to pull in bean definitions from Raven
 * 
 * @author Tom Oinn
 * 
 */
public class SpiDrivenHibernateTest {

	private static Log log = LogFactory.getLog(SpiDrivenHibernateTest.class);

	@Test
	/**
	 * Works around the current raven issues by preloading a bean from the
	 * implementation package - shouldn't be needed as raven should be scanning
	 * for existing artifacts!
	 */
	public void testSpiBasedDerbyInit() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext4.xml");
	}
}
