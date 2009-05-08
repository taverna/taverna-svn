package net.sf.taverna.t2.platform.spring;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Implementation of the placeholder configurer that can also act on the
 * artifact tags in raven-enabled bean definitions, allowing us to use
 * placeholders in these strings. Effectively acts as a cache for the resolved
 * properties so we can access them directly from the raven and plug-in manager
 * beans.
 * <p> 
 * @author Tom Oinn
 * 
 */
public class RavenAwarePropertyPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {

	private Properties props = null;

	@SuppressWarnings("unchecked")
	String processProperty(String source) {
		try {
			synchronized (this) {
				if (props == null) {
					props = mergeProperties();
				}
			}
			return parseStringValue(source, props, new HashSet());
		} catch (BeanDefinitionStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return source;
	}

}
