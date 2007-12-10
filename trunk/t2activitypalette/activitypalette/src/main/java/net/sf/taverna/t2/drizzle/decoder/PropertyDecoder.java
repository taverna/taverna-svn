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
 */
public interface PropertyDecoder<Source, Target extends Beanable<?>> {

	boolean canDecode (Class<?> sourceClass, Class<?> targetClass);
	
	DecodeRunIdentification<Target> decode (PropertiedObjectSet<Target> target, Source encodedObject);
	
}
