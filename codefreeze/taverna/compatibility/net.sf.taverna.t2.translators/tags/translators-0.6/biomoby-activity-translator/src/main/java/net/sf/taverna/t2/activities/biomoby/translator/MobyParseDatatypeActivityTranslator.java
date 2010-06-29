/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivity;
import net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;

import org.embl.ebi.escience.scufl.Processor;

public class MobyParseDatatypeActivityTranslator extends
		AbstractActivityTranslator<MobyParseDatatypeActivityConfigurationBean> {

	@Override
	protected MobyParseDatatypeActivityConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		MobyParseDatatypeActivityConfigurationBean bean =  new MobyParseDatatypeActivityConfigurationBean();
		bean.setDatatypeName(getDatatypeName(processor));
		bean.setRegistryEndpoint(getRegistryEndpoint(processor));
		bean.setArticleNameUsedByService(getArticleNameUsedByService(processor));
		return bean;
	}

	@Override
	protected MobyParseDatatypeActivity createUnconfiguredActivity() {
		return new MobyParseDatatypeActivity();
	}

	private String getDatatypeName(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getDatatypeName");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getDatatypeName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getDatatypeName, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getDatatypeName on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getDatatypeName on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getDatatypeName on the Biomoby processor",e);
		}
	}
	
	private String getRegistryEndpoint(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getRegistryEndpoint");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getRegistryEndpoint through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getRegistryEndpoint, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getRegistryEndpoint on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getRegistryEndpoint on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getRegistryEndpoint on the Biomoby processor",e);
		}
	}
	
	private String getArticleNameUsedByService(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getArticleNameUsedByService");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getArticleNameUsedByService through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getArticleNameUsedByService, an therefore does not conform to being an Biomoby processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getArticleNameUsedByService on the Biomoby processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getArticleNameUsedByService on the Biomoby processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getArticleNameUsedByService on the Biomoby processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.biomoby.client.taverna.plugin.MobyParseDatatypeProcessor");
	}

}
