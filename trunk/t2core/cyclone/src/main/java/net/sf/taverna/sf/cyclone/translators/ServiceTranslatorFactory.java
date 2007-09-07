package net.sf.taverna.sf.cyclone.translators;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;

import net.sf.taverna.t2.workflowmodel.processor.service.Service;

/**
 * <p>
 * A Factory class responsible for providing the appropriate {@link ServiceTranslator} for a given
 * class of a Taverna 1 style Processor. This translator is responsible for providing a {@link Service} that
 * has similar capabilities of the original Processor.
 * </p>
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class ServiceTranslatorFactory {
	private static Map<Class<? extends Processor>,ServiceTranslator<?>> map = new HashMap<Class<? extends Processor>, ServiceTranslator<?>>();
	
	static {
		map.put(BeanshellProcessor.class, new BeanshellServiceTranslator());
	}
	/**
	 * Given a particular Processor class it returns an appropriate ServiceTranslator
	 * 
	 * @param processorClass - the class of the Processor requiring a ServiceTranslator
	 * @return an appropriate ServiceTranslator
	 * @throws ServiceTranslatorNotFoundException 
	 */
	public static ServiceTranslator<?> getTranslator(Class<? extends Processor> processorClass) throws ServiceTranslatorNotFoundException {
		ServiceTranslator<?> result = map.get(processorClass);
		
		if (result == null) throw new ServiceTranslatorNotFoundException("Unable to find Service Translator for:"+processorClass);
		return result;
	}
}
