package net.sf.taverna.t2.platform.spring.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

/**
 * A context profile is a way of ensuring that a set of named beans exist within
 * a particular application context. In general profiles will also provide
 * type-safe get methods which internally delegate to the application context.
 * This abstract superclass provides the functionality to verify an application
 * context against a profile defined by a properties file where the keys in the
 * file are the names of beans which must exist in the context and the values
 * are the class names of those beans.
 * <p>
 * This allows us to define 'basic profiles' of functionality we can guarantee
 * and have applications code against these profiles with an expectation that
 * there will always be the facilities the application needs available.
 * <p>
 * For convenience this class also exposes the ApplicationContext interface,
 * delegating all methods to the instance of ApplicationContext supplied on
 * construction.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class AbstractContextProfile implements ApplicationContext {

	/**
	 * A reference to the application context for this profile used to delegate
	 * methods from the ApplicationContext interface
	 */
	private final ApplicationContext context;

	/**
	 * Initialize a profile from an application context, checking that the
	 * appropriate set of beans exists and that each has the correct type within
	 * the supplied application context. The set of beans to search for is
	 * derived from a properties file located in META-INF/profiles/[fully
	 * qualified class name].properties
	 * 
	 * @param context
	 *            the application context to check
	 * @throws ContextProfileException
	 *             if an error occurs when initializing the context profile or
	 *             if the context supplied does not contain the correct set of
	 *             beans as defined by the profile
	 */
	protected AbstractContextProfile(ApplicationContext context)
			throws ContextProfileException {

		// Check that the supplied context isn't null
		if (context == null) {
			throw new ContextProfileException(
					"Cannot attach a profile to a null context");
		}
		this.context = context;

		// Find and load the appropriate properties file
		Properties properties = new Properties();
		String className = this.getClass().getCanonicalName();
		try {
			InputStream resourceStream = this.getClass().getClassLoader()
					.getResourceAsStream(
							"META-INF/profiles/" + className + ".properties");
			if (resourceStream == null) {
				throw new ContextProfileException(
						"Unable to locate properties file for profile '"
								+ className + "'");
			}
			properties.load(resourceStream);
			resourceStream.close();
		} catch (IOException ioe) {
			throw new ContextProfileException(
					"Unable to load properties for profile '" + className + "'",
					ioe);
		}

		// For each entry in the profile check that there's a corresponding bean
		// defined in the application context with the correct runtime type
		for (Object key : properties.keySet()) {
			String beanName = (String) key;
			String beanClassName = properties.getProperty(beanName);
			Class<?> beanClass;
			try {
				beanClass = Thread.currentThread().getContextClassLoader()
						.loadClass(beanClassName);
				boolean foundBean = false;
				for (String contextBeanName : getBeanNamesForType(beanClass)) {
					if (contextBeanName.equals(beanName)) {
						foundBean = true;
						break;
					}
				}
				if (!foundBean) {
					throw new ContextProfileException("Required bean named '"
							+ beanName + "' with type '" + beanClassName
							+ "' not found in profile for '" + className + "'");
				}
			} catch (ClassNotFoundException cnfe) {
				throw new ContextProfileException(
						"Unable to locate class definition for bean '"
								+ beanName + "' in profile for '" + className
								+ "'", cnfe);
			}
		}

	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final boolean containsBean(String arg0) {
		return context.containsBean(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final boolean containsBeanDefinition(String arg0) {
		return context.containsBeanDefinition(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final boolean containsLocalBean(String arg0) {
		return context.containsLocalBean(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String[] getAliases(String arg0) {
		return context.getAliases(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final AutowireCapableBeanFactory getAutowireCapableBeanFactory()
			throws IllegalStateException {
		return context.getAutowireCapableBeanFactory();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final Object getBean(String arg0, Class arg1) throws BeansException {
		return context.getBean(arg0, arg1);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final Object getBean(String arg0, Object[] arg1)
			throws BeansException {
		return context.getBean(arg0, arg1);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final Object getBean(String arg0) throws BeansException {
		return context.getBean(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final int getBeanDefinitionCount() {
		return context.getBeanDefinitionCount();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String[] getBeanDefinitionNames() {
		return context.getBeanDefinitionNames();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final String[] getBeanNamesForType(Class arg0, boolean arg1,
			boolean arg2) {
		return context.getBeanNamesForType(arg0, arg1, arg2);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final String[] getBeanNamesForType(Class arg0) {
		return context.getBeanNamesForType(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final Map getBeansOfType(Class arg0, boolean arg1, boolean arg2)
			throws BeansException {
		return context.getBeansOfType(arg0, arg1, arg2);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final Map getBeansOfType(Class arg0) throws BeansException {
		return context.getBeansOfType(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String getDisplayName() {
		return context.getDisplayName();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String getId() {
		return context.getId();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String getMessage(MessageSourceResolvable arg0, Locale arg1)
			throws NoSuchMessageException {
		return context.getMessage(arg0, arg1);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String getMessage(String arg0, Object[] arg1, Locale arg2)
			throws NoSuchMessageException {
		return context.getMessage(arg0, arg1, arg2);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final String getMessage(String arg0, Object[] arg1, String arg2,
			Locale arg3) {
		return context.getMessage(arg0, arg1, arg2, arg3);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final ApplicationContext getParent() {
		return context.getParent();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final BeanFactory getParentBeanFactory() {
		return context.getParentBeanFactory();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final Resource getResource(String arg0) {
		return context.getResource(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final Resource[] getResources(String arg0) throws IOException {
		return context.getResources(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final long getStartupDate() {
		return context.getStartupDate();
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final Class getType(String arg0)
			throws NoSuchBeanDefinitionException {
		return context.getType(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final boolean isPrototype(String arg0)
			throws NoSuchBeanDefinitionException {
		return context.isPrototype(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final boolean isSingleton(String arg0)
			throws NoSuchBeanDefinitionException {
		return context.isSingleton(arg0);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	@SuppressWarnings("unchecked")
	public final boolean isTypeMatch(String arg0, Class arg1)
			throws NoSuchBeanDefinitionException {
		return context.isTypeMatch(arg0, arg1);
	}

	/**
	 * Delegates to internal ApplicationContext supplied by constructor
	 */
	public final void publishEvent(ApplicationEvent arg0) {
		context.publishEvent(arg0);
	}
}
