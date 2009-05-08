package net.sf.taverna.t2.platform.spring;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ServletContextAware;

/**
 * Extension of the RavenAwarePropertyPlaceholderConfigurer that can aggregate
 * properties defined in the web application context, only use this in contexts
 * to be loaded in a servlet container!
 * 
 * @author Tom Oinn
 * 
 */
public class RavenAwareWebPropertyPlaceholderConfigurer extends
		RavenAwarePropertyPlaceholderConfigurer implements ServletContextAware {

	private Properties contextProperties = null;

	@Override
	protected void loadProperties(Properties props) throws IOException {
		if (contextProperties != null) {
			CollectionUtils.mergePropertiesIntoMap(contextProperties, props);
		}
		super.loadProperties(props);
	}

	@SuppressWarnings("unchecked")
	public void setServletContext(ServletContext context) {
		contextProperties = new Properties();
		for (Enumeration en = context.getAttributeNames(); en.hasMoreElements();) {
			String key = (String) en.nextElement();
			Object value = context.getAttribute(key);
			contextProperties.put(key, value.toString());
			System.out.println(key + "=" + value.toString());
		}
	}
}
