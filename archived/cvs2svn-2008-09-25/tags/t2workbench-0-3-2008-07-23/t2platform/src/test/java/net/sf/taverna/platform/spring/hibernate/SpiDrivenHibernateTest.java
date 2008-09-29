package net.sf.taverna.platform.spring.hibernate;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;

/**
 * Uses a patched version of hibernate to pull in bean definitions from Raven
 * 
 * @author Tom Oinn
 * 
 */
public class SpiDrivenHibernateTest {

	@Test
	/**
	 * Works around the current raven issues by preloading a bean from the
	 * implementation package - shouldn't be needed as raven should be scanning
	 * for existing artifacts!
	 */
	public void testSpiBasedDerbyInit() {
		new RavenAwareClassPathXmlApplicationContext("applicationContext4.xml");
	}
}
