/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;


import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class BeanshellProcessorFactoryDecoder extends ProcessorFactoryDecoder<BeanshellProcessorFactory> {

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			BeanshellProcessorFactory encodedFactory) {
		//TODO details from prototype
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(BeanshellProcessorFactory.class) &&
				BeanshellProcessorFactory.class.isAssignableFrom(sourceClass));
	}

}
