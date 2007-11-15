/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class BiomartProcessorFactoryDecoder extends ProcessorFactoryDecoder<BiomartProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			BiomartProcessorFactory encodedFactory) {
		// TODO martQuery
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(BiomartProcessorFactory.class) &&
				BiomartProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
