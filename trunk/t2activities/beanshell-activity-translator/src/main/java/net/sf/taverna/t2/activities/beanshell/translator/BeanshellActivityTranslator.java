package net.sf.taverna.t2.activities.beanshell.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Beanshell Processors to a Taverna 2 Beanshell Activity
 * 
 * @see ActivityTranslator
 * @author Stuart Owen
 */
public class BeanshellActivityTranslator extends AbstractActivityTranslator<BeanshellActivityConfigurationBean> {

	@Override
	protected Activity<BeanshellActivityConfigurationBean> createUnconfiguredActivity() {
		return new BeanshellActivity();
	}

	@Override
	protected BeanshellActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		
		bean.setScript(determineScript(processor));
		return bean;
	}
	
	private String determineScript(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getScript");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getString through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getScript, an therefore does not conform to being a Beanshell processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getScript on the beanshell processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getScript on the Beanshell processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getScript on the Beanshell processor",e);
		}
	}

}
