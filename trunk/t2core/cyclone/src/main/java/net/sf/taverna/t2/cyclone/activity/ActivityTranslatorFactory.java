package net.sf.taverna.t2.cyclone.activity;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * <p>
 * A Factory class responsible for providing the appropriate {@link ActivityTranslator} for a given
 * class of a Taverna 1 style Processor. This translator is responsible for providing a {@link Activity} that
 * has similar capabilities of the original Processor.
 * </p>
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class ActivityTranslatorFactory {
	private static Map<Class<? extends Processor>,ActivityTranslator<?>> map = new HashMap<Class<? extends Processor>, ActivityTranslator<?>>();
	
	static {
		map.put(BeanshellProcessor.class, new BeanshellActivityTranslator());
	}
	/**
	 * <p>
	 * Given a particular Processor class it returns an appropriate ActivityTranslator
	 * </p>
	 * 
	 * @param processor - the Processor requiring an ActivityTranslator
	 * @return an appropriate ActivityTranslator
	 * @throws ActivityTranslatorNotFoundException 
	 */
	public static ActivityTranslator<?> getTranslator(Processor processor) throws ActivityTranslatorNotFoundException {
		
		//FIXME: Use LocalArtifactClassLoader to determine the Artifact for the Processor. Then use Raven to get the corresponding Activity class mapped to that version of the Processor.
		ActivityTranslator<?> result = map.get(processor.getClass());
		
		if (result == null) throw new ActivityTranslatorNotFoundException("Unable to find Activity Translator for:"+processor.getClass());
		return result;
	}
}
