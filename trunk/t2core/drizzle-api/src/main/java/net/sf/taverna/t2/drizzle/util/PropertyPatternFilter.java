/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * @author alanrw
 *
 */
public final class PropertyPatternFilter<O extends Beanable<?>> implements PropertiedObjectFilter<O> {
	
	private PropertyKey key;
	private String pattern;
	private PropertiedObjectSet<O> registry;
	
	public PropertyPatternFilter(final PropertyKey key, final String pattern, final PropertiedObjectSet<O> registry) {
		this.key = key;
		this.pattern = ".*" + pattern.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		this.registry = registry;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter#acceptObject(java.lang.Object)
	 */
	public boolean acceptObject(O object) {
		boolean result = false;
		Object value = this.registry.getPropertyValue(object, this.key);
		if (value != null) {
			result = value.toString().toLowerCase().matches(this.pattern);
		}
		return result;
	}

}
