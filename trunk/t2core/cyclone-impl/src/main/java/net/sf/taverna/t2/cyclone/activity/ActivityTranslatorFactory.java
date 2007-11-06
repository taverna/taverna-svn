package net.sf.taverna.t2.cyclone.activity;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * A Factory class responsible for providing the appropriate
 * {@link ActivityTranslator} for a given class of a Taverna 1 style Processor.
 * This translator is responsible for providing a {@link Activity} that has
 * similar capabilities of the original Processor.
 * </p>
 * 
 * @author Stuart Owen
 * @author David Withers
 * 
 */
public class ActivityTranslatorFactory {
	

	private static Map<Class<?>, ActivityTranslator<?>> translatorMap = new HashMap<Class<?>, ActivityTranslator<?>>();

	private static ActivityTranslatorSPIRegistry registry = new ActivityTranslatorSPIRegistry();
	/**
	 * <p>
	 * Given a particular Processor class it returns an appropriate
	 * ActivityTranslator
	 * </p>
	 * 
	 * @param processor -
	 *            the Processor requiring an ActivityTranslator
	 * @return an appropriate ActivityTranslator
	 * @throws ActivityTranslatorNotFoundException
	 */
	public static ActivityTranslator<?> getTranslator(Processor processor)
			throws ActivityTranslatorNotFoundException {
		if (!translatorMap.containsKey(processor.getClass())) {
			boolean foundTranslator = false;
			for (ActivityTranslator<?> translator : getRegistry().getInstances()) {
				if (translator.canHandle(processor)) {
					translatorMap.put(processor.getClass(), translator);
					foundTranslator = true;
					break;
				}
			}
			if (!foundTranslator) {
				throw new ActivityTranslatorNotFoundException(
						"Unable to find Activity Translator for:"
								+ processor.getClass());
			}
		}

		return translatorMap.get(processor.getClass());
	}

	protected static ActivityTranslatorSPIRegistry getRegistry() {
		return registry;
	}
}
