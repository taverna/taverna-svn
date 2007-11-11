/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.StringValue;

/**
 * @author alanrw
 *
 */
public abstract class ProcessorFactoryDecoder<FactoryType extends ProcessorFactory> implements PropertyDecoder<FactoryType,FactoryType> {

	protected abstract void fillInDetails(PropertiedObjectSet<ProcessorFactory> targetSet, FactoryType encodedFactory);

	public Set<FactoryType> decode(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			FactoryType encodedObject) {
		Set<FactoryType> result = new HashSet<FactoryType>();
		result.add (encodedObject);
		targetSet.addObject(encodedObject);
		targetSet.setProperty(encodedObject, CommonKey.ProcessorClassKey,
				new StringValue(encodedObject.getProcessorClass().getName()));
		targetSet.setProperty(encodedObject, CommonKey.NameKey, new StringValue(encodedObject.getName()));
		fillInDetails(targetSet, encodedObject);
		return result;
	}
}
