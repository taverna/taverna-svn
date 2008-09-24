/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.processorfactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.drizzle.util.StringValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 * 
 */
public final class DefaultProcessorFactoryDecoder extends
		ProcessorFactoryDecoder<ProcessorFactory> {
	
	private static HashMap<Class<?>, DefaultProcessorFactoryDecoder> instanceMap = new
	HashMap<Class<?>, DefaultProcessorFactoryDecoder>();

	private BeanInfo processorBeanInfo = null;

	HashMap<PropertyDescriptor, PropertyKey> descriptorToKeyMap;
	
	/**
	 * @param c
	 * @return
	 */
	public static DefaultProcessorFactoryDecoder getInstance(final Class<?> c) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$
		}
		DefaultProcessorFactoryDecoder result = null;
		if (instanceMap.containsKey(c)) {
			result = instanceMap.get(c);
		} else {
			result = new DefaultProcessorFactoryDecoder(c);
			instanceMap.put(c, result);
		}
		return result;
	}

	private DefaultProcessorFactoryDecoder(Class<?> c) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$
		}
		this.descriptorToKeyMap = new HashMap<PropertyDescriptor, PropertyKey>();
		try {
			this.processorBeanInfo = Introspector.getBeanInfo(c, Object.class);
			for (PropertyDescriptor pd : this.processorBeanInfo
					.getPropertyDescriptors()) {
				if (pd.getReadMethod() != null) {
					this.descriptorToKeyMap.put(pd, new StringKey(pd.getName()));
				}
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			ProcessorFactoryAdapter adapter,
			ProcessorFactory encodedFactory) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedFactory == null) {
			throw new NullPointerException("encodedFactory cannot be null"); //$NON-NLS-1$
		}
		for (PropertyDescriptor pd : this.descriptorToKeyMap.keySet()) {
			Method readMethod = pd.getReadMethod();
			try {
				Object methodResult = readMethod.invoke(encodedFactory, new Object[] {});
				if (methodResult != null) {
					StringValue value = new StringValue(methodResult.toString());
					PropertyKey key = this.descriptorToKeyMap.get(pd);
					targetSet.setProperty(adapter, key, value);
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	Set<PropertyKey> getPropertyKeyProfile() {
		return new HashSet<PropertyKey>(this.descriptorToKeyMap.values());
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#canDecode(java.lang.Class, java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return targetClass.isAssignableFrom(ProcessorFactoryAdapter.class);
	}

}
