/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder;

import net.sf.taverna.t2.drizzle.query.DecodeRunIdentification;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * @author alanrw
 *
 * @param <Source>
 * @param <Target>
 */
public interface PropertyDecoder<Source, Target extends Beanable<?>> {

	/**
	 * @param sourceClass
	 * @param targetClass
	 * @return
	 */
	boolean canDecode (Class<?> sourceClass, Class<?> targetClass);
	
	/**
	 * @param target
	 * @param encodedObject
	 * @return
	 */
	DecodeRunIdentification<Target> decode (PropertiedObjectSet<Target> target, Source encodedObject);
	
}
