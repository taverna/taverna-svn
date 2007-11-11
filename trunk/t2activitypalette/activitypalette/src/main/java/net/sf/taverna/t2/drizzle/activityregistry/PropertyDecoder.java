/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Set;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public interface PropertyDecoder<Target, Source> {

	boolean canDecode (Class sourceClass, Class targetClass);
	
	Set<Target> decode (PropertiedObjectSet<ProcessorFactory> target, Source encodedObject);
}
