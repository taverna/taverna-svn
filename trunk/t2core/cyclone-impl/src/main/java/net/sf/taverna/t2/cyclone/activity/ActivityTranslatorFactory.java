package net.sf.taverna.t2.cyclone.activity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.SpiRegistry;
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
	
	private static InstanceRegistry<ActivityTranslator<?>> instanceRegistry = null;
	
	
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
		ActivityTranslator<?> result=null;
		List<ActivityTranslator<?>> translators = getTranslators();
		for (ActivityTranslator<?> translator : translators) {
			if (translator.canHandle(processor)) {
				result=translator;
				break;
			}
		}
	
		if (result == null) {
			throw new ActivityTranslatorNotFoundException("Unable to find Activity Translator for:"+processor.getClass());
		}
		return result;
	}
	
	private static List<ActivityTranslator<?>> getTranslators() {
		return getRegistry().getInstances();
	}
	
	private synchronized static InstanceRegistry<ActivityTranslator<?>> getRegistry() {
		if (instanceRegistry==null) {
			SpiRegistry registry = new SpiRegistry(getRepository(),ActivityTranslator.class.getName(),ActivityTranslatorFactory.class.getClassLoader());
			instanceRegistry = new InstanceRegistry<ActivityTranslator<?>>(registry, new Object[0]);
		}
		return instanceRegistry;
	}

	private static Repository getRepository() {
		//FIXME: How do we get the repository correctly?
		File tmpDir=null;
		try {
			tmpDir = File.createTempFile("taverna", "raven");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		return tempRepository;
	}
}
