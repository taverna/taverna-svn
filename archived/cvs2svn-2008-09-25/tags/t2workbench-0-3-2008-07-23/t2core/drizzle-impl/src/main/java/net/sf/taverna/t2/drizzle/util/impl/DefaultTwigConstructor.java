/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.TwigConstructor;

/**
 * @author alanrw
 *
 */
public final class DefaultTwigConstructor implements TwigConstructor<Object> {
	
	private static DefaultTwigConstructor theInstance;
	
	private DefaultTwigConstructor() {
	}

	public static DefaultTwigConstructor getInstance() {
		if (theInstance == null) {
			theInstance = new DefaultTwigConstructor();
		}
		return theInstance;
	}
	
	public boolean canHandle(@SuppressWarnings("unused")
	Class<?> sourceClass) {
		return true;
	}

	public PropertiedTreeNode<Object> createTwig(Object object) {
		PropertiedTreeObjectNode<Object> result = new PropertiedTreeObjectNodeImpl<Object>();
		result.setObject(object);
		return result;
	}

}
