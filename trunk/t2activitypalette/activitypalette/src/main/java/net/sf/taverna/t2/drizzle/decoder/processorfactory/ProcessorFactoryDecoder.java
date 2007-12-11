/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.processorfactory;

import java.util.HashSet;
import java.util.Set;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.decoder.CommonKey;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.query.DecodeRunIdentification;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

/**
 * @author alanrw
 *
 * @param <FactoryType>
 */
public abstract class ProcessorFactoryDecoder<FactoryType extends ProcessorFactory> implements PropertyDecoder<FactoryType,ProcessorFactoryAdapter> {

	protected abstract void fillInDetails(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet, ProcessorFactoryAdapter adapter, FactoryType encodedFactory);

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)
	 */
	public DecodeRunIdentification<ProcessorFactoryAdapter> decode(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			FactoryType encodedObject) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedObject == null) {
			throw new NullPointerException("encodedObject cannot be null"); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> result = new DecodeRunIdentification<ProcessorFactoryAdapter>();
		Set<ProcessorFactoryAdapter> affectedObjects = new HashSet<ProcessorFactoryAdapter>();
		ProcessorFactoryAdapter adapter = new ProcessorFactoryAdapter(encodedObject);
		affectedObjects.add(adapter);
		result.setAffectedObjects(affectedObjects);
		result.setPropertyKeyProfile(this.getPropertyKeyProfile());
		result.setTimeOfRun(System.currentTimeMillis());
		targetSet.addObject(adapter);
		Class<?> processorClass = encodedObject.getProcessorClass();
		if (processorClass == null) {
			targetSet.setProperty(adapter, CommonKey.ProcessorClassKey,
					new StringValue("no processor class")); //$NON-NLS-1$
		} else {
			String processorClassName = encodedObject.getProcessorClass().getName();
			if (processorClassName == null) {
				targetSet.setProperty(adapter, CommonKey.ProcessorClassKey,
						new StringValue("processor class with no name")); //$NON-NLS-1$
			} else {
			targetSet.setProperty(adapter, CommonKey.ProcessorClassKey,
					new StringValue("processor classs with name" + encodedObject.getProcessorClass().getName()));	 //$NON-NLS-1$
			}
		}
		targetSet.setProperty(adapter, CommonKey.ProcessorClassKey,
				new StringValue(encodedObject.getProcessorClass().getName()));
		targetSet.setProperty(adapter, CommonKey.NameKey, new StringValue(encodedObject.getName()));
		fillInDetails(targetSet, adapter, encodedObject);
		return result;
	}
	
	abstract Set<PropertyKey> getPropertyKeyProfile();
}
