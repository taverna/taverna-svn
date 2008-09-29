package net.sf.taverna.platform.spring;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A subclass of the ClassPathXmlApplicationContext which uses the raven aware
 * bean factory and therefore can cope with the presence of raven repository and
 * artifact attributes correctly on beans within the configuration file. Use in
 * place of ClassPathXmlApplicationContext to magically enable raven support.
 * 
 * @author Tom Oinn
 * 
 */
public class RavenAwareClassPathXmlApplicationContext extends
		ClassPathXmlApplicationContext {

	private static DefaultListableBeanFactory factory = new RavenAwareListableBeanFactory();

	public RavenAwareClassPathXmlApplicationContext(String param) {
		super(param);
	}

	@Override
	protected DefaultListableBeanFactory createBeanFactory() {
		return factory;
	}
}