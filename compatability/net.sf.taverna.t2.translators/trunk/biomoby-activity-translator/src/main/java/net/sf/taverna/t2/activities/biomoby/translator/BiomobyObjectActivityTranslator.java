/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;

import org.embl.ebi.escience.scufl.Processor;

public class BiomobyObjectActivityTranslator extends AbstractActivityTranslator<BiomobyObjectActivityConfigurationBean> {

	@Override
	protected BiomobyObjectActivityConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		BiomobyObjectActivityConfigurationBean bean = new BiomobyObjectActivityConfigurationBean();
		bean.setMobyEndpoint(getMobyEndpoint(processor));
		bean.setServiceName(getServiceName(processor));
		bean.setAuthorityName(getAuthorityName(processor));
		return bean;
	}

	@Override
	protected BiomobyObjectActivity createUnconfiguredActivity() {
		return new BiomobyObjectActivity();
	}

	private String getServiceName(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getServiceName");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getServiceName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getServiceName, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getServiceName on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getServiceName on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getServiceName on the Biomoby processor",e);
		}
	}
	
	private String getMobyEndpoint(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getMobyEndpoint");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getMobyEndpoint through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getMobyEndpoint, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getMobyEndpoint on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getMobyEndpoint on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getMobyEndpoint on the Biomoby processor",e);
		}
	}
	
	private String getAuthorityName(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getAuthorityName");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getAuthorityName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getAuthorityName, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getAuthorityName on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getAuthorityName on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getAuthorityName on the Biomoby processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor!=null && processor.getClass().getName().equals("org.biomoby.client.taverna.plugin.BiomobyObjectProcessor");
	}

}
