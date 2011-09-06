package org.taverna.launcher;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator that can export an API with minimal effort.
 * 
 * @author Donal Fellows
 * 
 * @param <T>
 *            The API interface of the bundle.
 */
public class APIActivator<T> implements BundlePackageActivator {
	private Class<T> api;
	private T impl;
	private ServiceRegistration registration;

	/**
	 * Create a bundle activator for the given API and implementation.
	 * 
	 * @param api
	 *            The class of the API interface.
	 * @param impl
	 *            The object that implements the interface.
	 */
	APIActivator(Class<T> api, T impl) {
		this.api = api;
		this.impl = impl;
	}

	@Override
	public void start(BundleContext context) {
		registration = context.registerService(api.getName(), impl,
				new Properties());
	}

	@Override
	public void stop(BundleContext context) {
		registration.unregister();
	}

	@Override
	public Package getAPIPackage() {
		return api.getPackage();
	}
}