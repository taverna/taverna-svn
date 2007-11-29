/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
	
	private static HashMap<Class, DefaultProcessorFactoryDecoder> instanceMap = new
	HashMap<Class, DefaultProcessorFactoryDecoder>();

	private BeanInfo processorBeanInfo = null;

	HashMap<PropertyDescriptor, PropertyKey> descriptorToKeyMap;
	
	public static DefaultProcessorFactoryDecoder getInstance(final Class c) {
		DefaultProcessorFactoryDecoder result = null;
		if (instanceMap.containsKey(c)) {
			result = instanceMap.get(c);
		} else {
			result = new DefaultProcessorFactoryDecoder(c);
			instanceMap.put(c, result);
		}
		return result;
	}

	private DefaultProcessorFactoryDecoder(Class c) {
		descriptorToKeyMap = new HashMap<PropertyDescriptor, PropertyKey>();
		try {
			processorBeanInfo = Introspector.getBeanInfo(c, Object.class);
			for (PropertyDescriptor pd : processorBeanInfo
					.getPropertyDescriptors()) {
				if (pd.getReadMethod() != null) {
					descriptorToKeyMap.put(pd, new StringKey(pd.getName()));
				}
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void fillInDetails(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			ProcessorFactory encodedFactory) {
		for (PropertyDescriptor pd : descriptorToKeyMap.keySet()) {
			Method readMethod = pd.getReadMethod();
			try {
				Object methodResult = readMethod.invoke(encodedFactory, new Object[] {});
				if (methodResult != null) {
					StringValue value = new StringValue(methodResult.toString());
					PropertyKey key = descriptorToKeyMap.get(pd);
					targetSet.setProperty(encodedFactory, key, value);
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
		return new HashSet<PropertyKey>(descriptorToKeyMap.values());
	}

	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return targetClass.isAssignableFrom(ProcessorFactory.class);
	}

}
