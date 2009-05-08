package net.sf.taverna.t2.platform.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.sf.taverna.t2.platform.util.PropertyInterpolator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

/**
 * Abstract superclass handling resource extraction and manipulation of
 * placeholder values for plug-in manager and raven
 * 
 * @author Tom Oinn
 */
public abstract class AbstractPlatformFactoryBean implements FactoryBean,
		ApplicationContextAware {

	private RavenAwarePropertyPlaceholderConfigurer propHolder = null;
	private ApplicationContext applicationContext = null;

	protected final List<String> getStrings(Resource theResource) {
		InputStream is;
		try {
			is = theResource.getInputStream();
			Scanner scanner = new Scanner(is);
			List<String> result = new ArrayList<String>();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.startsWith("#")) {
					result.add(getString(line));
				}
			}
			return result;
		} catch (IOException e) {
			return new ArrayList<String>();
		}
	}

	protected final String getString(String source) {
		if (propHolder != null) {
			return propHolder.processProperty(source);
		} else {
			return PropertyInterpolator.doInterpolate(source);
		}
	}

	public abstract Object getObject() throws Exception;

	@SuppressWarnings("unchecked")
	public abstract Class getObjectType();

	public abstract boolean isSingleton();

	protected ApplicationContext getContext() {
		return this.applicationContext;
	}

	public final void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext = context;
		if (context.containsBeanDefinition("platform.propertyPlaceholder")) {
			try {
				propHolder = (RavenAwarePropertyPlaceholderConfigurer) context
						.getBean("platform.propertyPlaceholder");
			} catch (ClassCastException cce) {
				//
			}
		}
	}

}
