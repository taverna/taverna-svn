package org.taverna.launcher;

import org.osgi.framework.BundleActivator;

/**
 * A bundle activator that knows how to describe what package it is talking
 * about. Only suitable for simple activators.
 * 
 * @author Donal Fellows
 */
public interface BundlePackageActivator extends BundleActivator {
	/**
	 * What is the package that provides the API for this activator's bundle?
	 * 
	 * @return The Java package.
	 */
	Package getAPIPackage();
}
