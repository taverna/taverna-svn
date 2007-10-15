package net.sf.taverna.t2.activities.biomart.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.biomart.martservice.MartQuery;
import org.embl.ebi.escience.scufl.Processor;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Biomart Processors to a Taverna 2 Biomart Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class BiomartActivityTranslator extends AbstractActivityTranslator<BiomartActivityConfigurationBean> {

	@Override
	protected Activity<BiomartActivityConfigurationBean> createUnconfiguredActivity() {
		return new BiomartActivity();
	}

	@Override
	protected BiomartActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		BiomartActivityConfigurationBean bean = new BiomartActivityConfigurationBean();
		bean.setQuery(getQuery(processor));
		return bean;
	}
	
	private MartQuery getQuery(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getQuery");
			return (MartQuery) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getQuery through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getQuery, and therefore does not conform to being a Biomart processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getQuery on the beanshell processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getQuery on the Biomart processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getQuery on the Biomart processor",e);
		}
	}

}
