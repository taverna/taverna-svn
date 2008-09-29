package ${packageName};

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.embl.ebi.escience.scufl.Processor;

import net.sf.taverna.t2.activities.${artifactId}Activity;
import net.sf.taverna.t2.activities.${artifactId}ActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * An ActivityTranslator specifically for translating a Taverna 1 ${artifactId} Processor to an equivalent Taverna 2 ${artifactId} Activity
 * 
 * @see ActivityTranslator
 */
public class ${artifactId}ActivityTranslator extends AbstractActivityTranslator<${artifactId}ActivityConfigurationBean> {

	@Override
	protected ${artifactId}ActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		${artifactId}ActivityConfigurationBean bean = new ${artifactId}ActivityConfigurationBean();
		bean.setValue(getServiceLocation(processor));
		return bean;
	}

	@Override
	protected Activity<${artifactId}ActivityConfigurationBean> createUnconfiguredActivity() {
		return new ${artifactId}Activity();
	}
	
	private String getServiceLocation(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getServiceLocation");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getServiceLocation through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getServiceLocation, an therefore does not conform to being an ${artifactId} processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getServiceLocation on the ${artifactId} processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getServiceLocation on the ${artifactId} processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getServiceLocation on the ${artifactId} processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.${artifactId.toLowerCase()}${artifactId}Processor");
	}

}
