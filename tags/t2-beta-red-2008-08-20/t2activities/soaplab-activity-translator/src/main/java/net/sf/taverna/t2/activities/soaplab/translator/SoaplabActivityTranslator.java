package net.sf.taverna.t2.activities.soaplab.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;

import org.embl.ebi.escience.scufl.Processor;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Soaplab Processors to a Taverna 2 Soaplab Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class SoaplabActivityTranslator extends AbstractActivityTranslator<SoaplabActivityConfigurationBean> {

	@Override
	protected SoaplabActivity createUnconfiguredActivity() {
		return new SoaplabActivity();
	}

	@Override
	protected SoaplabActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		SoaplabActivityConfigurationBean bean = new SoaplabActivityConfigurationBean();
		bean.setEndpoint(getEndpoint(processor).toString());
		bean.setPollingInterval(getPollingInterval(processor));
		bean.setPollingBackoff(getPollingBackoff(processor));
		bean.setPollingIntervalMax(getPollingIntervalMax(processor));
		return bean;
	}
	
	private URL getEndpoint(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getEndpoint");
			return (URL) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getEndpoint through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getEndpoint, and therefore does not conform to being a Soaplab processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getEndpoint on the soaplab processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getEndpoint on the Soaplab processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getEndpoint on the Soaplab processor",e);
		}
	}
	
	private int getPollingInterval(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getPollingInterval");
			return ((Integer) method.invoke(processor)).intValue();
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getPollingInterval through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getPollingInterval, and therefore does not conform to being a Soaplab processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getPollingInterval on the soaplab processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getPollingInterval on the Soaplab processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getPollingInterval on the Soaplab processor",e);
		}
	}
	
	private double getPollingBackoff(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getPollingBackoff");
			return ((Double) method.invoke(processor)).doubleValue();
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getPollingBackoff through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getPollingBackoff, and therefore does not conform to being a Soaplab processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getPollingBackoff on the soaplab processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getPollingBackoff on the Soaplab processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getPollingBackoff on the Soaplab processor",e);
		}
	}
	
	private int getPollingIntervalMax(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getPollingIntervalMax");
			return ((Integer) method.invoke(processor)).intValue();
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getPollingIntervalMax through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getPollingIntervalMax, and therefore does not conform to being a Soaplab processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getPollingIntervalMax on the soaplab processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getPollingIntervalMax on the Soaplab processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getPollingIntervalMax on the Soaplab processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor");
	}

}
