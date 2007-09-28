package net.sf.taverna.t2.cyclone.activity;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

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
	private static Map<String,String> map = new HashMap<String,String>();
	
	static {
		map.put("org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor", "net.sf.taverna.t2.activities.beanshell.BeanshellActivityTranslator");
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
		String classname=map.get(processor.getClass().getName());
		if (classname==null) {
			throw new ActivityTranslatorNotFoundException("Unable to find activity translator for:"+processor.getClass().getName());
		}
		ActivityTranslator<?> result;
		try {
			result = (ActivityTranslator<?>)Class.forName(classname).newInstance();
		} catch (InstantiationException e) {
			throw new ActivityTranslatorNotFoundException("Unable to create an instance of the translator",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslatorNotFoundException("Illegal access when trying to create the translator",e);
		} catch (ClassNotFoundException e) {
			throw new ActivityTranslatorNotFoundException("Unable to find the translator class",e);
		}
		
		if (result == null) throw new ActivityTranslatorNotFoundException("Unable to find Activity Translator for:"+processor.getClass());
		return result;
	}
}
