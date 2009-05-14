package net.sf.taverna.t2.activities.dependencyactivity;

/**
 * Different ways to share a class loader among activities:
 * 
 * <dl>
 * <dt>workflow</dt>
 * <dd>Same classloader for all activities using the <code>workflow</code> classloader sharing policy</dd>
 * <dt>system</dt>
 * <dd>System classloader</dd>
 * </dl>
 * 
 */
public enum ClassLoaderSharing {
	workflow, system
}