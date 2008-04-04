package net.sf.taverna.t2.activities.biomart.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;
import org.embl.ebi.escience.scufl.Processor;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Biomart Processors to a Taverna 2 Biomart Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class BiomartActivityTranslator extends AbstractActivityTranslator<BiomartActivityConfigurationBean> {

	@Override
	protected BiomartActivity createUnconfiguredActivity() {
		return new BiomartActivity();
	}

	@Override
	protected BiomartActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		BiomartActivityConfigurationBean bean = new BiomartActivityConfigurationBean();
		bean.setQuery(getQueryElement(processor));
		return bean;
	}
	
	private Element getQueryElement(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getQueryElement", Namespace.class);
			return (Element) method.invoke(processor, (Object) null);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getQueryElement through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getQueryElement, and therefore does not conform to being a Biomart processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getQueryElement on the Biomart processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getQueryElement on the Biomart processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getQueryElement on the Biomart processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor");
	}

}
