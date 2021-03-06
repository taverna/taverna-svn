package net.sf.taverna.t2.compatibility.activity;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

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
	

	private static Map<String, ActivityTranslator<?>> translatorMap = new HashMap<String, ActivityTranslator<?>>();

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
		String processorKey = generateProcessorKey(processor);
		if (!translatorMap.containsKey(processorKey)) {
			boolean foundTranslator = false;
			for (ActivityTranslator<?> translator : getRegistry().getInstances()) {
				if (translator.canHandle(processor)) {
					translatorMap.put(processorKey, translator);
					foundTranslator = true;
					break;
				}
			}
			if (!foundTranslator) {
				throw new ActivityTranslatorNotFoundException(
						"Unable to find Activity Translator for:"
								+ processor.getClass(),processor);
			}
		}

		return translatorMap.get(processorKey);
	}

	/**
	 * Normally the key is simply the fully qualified class name, unless it is a LocalServiceProcessor.
	 * If a LocalServiceProcessor then the key is LocalServiceProcessor: plus the worker classname.
	 * @param processor
	 * @return the key
	 */
	protected static String generateProcessorKey(Processor processor) {
		if (!(processor instanceof LocalServiceProcessor)) {
			return processor.getClass().getName();
		}
		else {
			//TODO: would be more desirable to do this through introspection to avoid the added dependency on taverna-java-processor
			String key="LocalServiceProcessor:";
			key+=((LocalServiceProcessor)processor).getWorkerClassName();
			return key;
		}
	}

	protected static ActivityTranslatorSPIRegistry getRegistry() {
		return registry;
	}
}
