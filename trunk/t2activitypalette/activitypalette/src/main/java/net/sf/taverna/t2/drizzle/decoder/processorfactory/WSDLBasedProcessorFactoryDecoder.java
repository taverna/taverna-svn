/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.processorfactory;


import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.activityregistry.CommonKey;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessorFactory;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public final class WSDLBasedProcessorFactoryDecoder extends ProcessorFactoryDecoder<WSDLBasedProcessorFactory> {

	static Set<PropertyKey> keyProfile = new HashSet<PropertyKey>() {
		{ add(CommonKey.ProcessorClassKey);
		add(CommonKey.NameKey);
		add(CommonKey.WsdlLocationKey);
		add(CommonKey.WsdlOperationKey);
		add(CommonKey.WsdlPortTypeKey);
		}
	};
	
	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			ProcessorFactoryAdapter adapter,
			WSDLBasedProcessorFactory encodedFactory) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory == null) {
			throw new NullPointerException("encodedFactory cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory.getWSDLLocation() != null) {
		targetSet.setProperty(adapter, CommonKey.WsdlLocationKey, new StringValue(encodedFactory.getWSDLLocation()));
		}
		if (encodedFactory.getOperationName() != null) {
		targetSet.setProperty(adapter, CommonKey.WsdlOperationKey, new StringValue(encodedFactory.getOperationName()));
		}
		if (encodedFactory.getPortTypeName() != null) {
		targetSet.setProperty(adapter, CommonKey.WsdlPortTypeKey, new StringValue(encodedFactory.getPortTypeName().getLocalPart()));
		}
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return (targetClass.isAssignableFrom(ProcessorFactoryAdapter.class) &&
				WSDLBasedProcessorFactory.class.isAssignableFrom(sourceClass));
	}

	@Override
	public Set<PropertyKey> getPropertyKeyProfile() {
		return keyProfile;
	}

}
