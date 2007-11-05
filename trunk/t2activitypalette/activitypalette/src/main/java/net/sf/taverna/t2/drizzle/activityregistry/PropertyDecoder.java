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
public interface PropertyDecoder {

	boolean canDecode (Object encodedObject);
	
	Set<ProcessorFactory> decode (PropertiedObjectSet<ProcessorFactory> target, Object encodedObject);
}
