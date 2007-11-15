/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public interface PropertyDecoder<Source, Target> {

	boolean canDecode (Class<?> sourceClass, Class<?> targetClass);
	
	Set<Target> decode (PropertiedObjectSet<Target> target, Source encodedObject);
}
