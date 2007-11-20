/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

/**
 * @author alanrw
 *
 */
public abstract class ProcessorFactoryDecoder<FactoryType extends ProcessorFactory> implements PropertyDecoder<FactoryType,ProcessorFactory> {

	protected abstract void fillInDetails(PropertiedObjectSet<ProcessorFactory> targetSet, FactoryType encodedFactory);

	public DecodeRunIdentification<ProcessorFactory> decode(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			FactoryType encodedObject) {
		DecodeRunIdentification<ProcessorFactory> result = new DecodeRunIdentification<ProcessorFactory>();
		Set<ProcessorFactory> affectedObjects = new HashSet<ProcessorFactory>();
		affectedObjects.add(encodedObject);
		result.setAffectedObjects(affectedObjects);
		result.setPropertyKeyProfile(this.getPropertyKeyProfile());
		result.setTimeOfRun(System.currentTimeMillis());
		targetSet.addObject(encodedObject);
		Class processorClass = encodedObject.getProcessorClass();
		if (processorClass == null) {
			targetSet.setProperty(encodedObject, CommonKey.ProcessorClassKey,
					new StringValue("no processor class"));
		} else {
			String processorClassName = encodedObject.getProcessorClass().getName();
			if (processorClassName == null) {
				targetSet.setProperty(encodedObject, CommonKey.ProcessorClassKey,
						new StringValue("processor class with no name"));
			} else {
			targetSet.setProperty(encodedObject, CommonKey.ProcessorClassKey,
					new StringValue("processor classs with name" + encodedObject.getProcessorClass().getName()));	
			}
		}
		targetSet.setProperty(encodedObject, CommonKey.ProcessorClassKey,
				new StringValue(encodedObject.getProcessorClass().getName()));
		targetSet.setProperty(encodedObject, CommonKey.NameKey, new StringValue(encodedObject.getName()));
		fillInDetails(targetSet, encodedObject);
		return result;
	}
	
	abstract Set<PropertyKey> getPropertyKeyProfile();
}
