package net.sf.taverna.t2.activities.localworker.xmlsplitter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLExtensible;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * A translator specifically targeted at the local worker XMLOutputSplitter.
 * 
 * It translates this type of local worker into a concrete Activity implementation rather than a beanshell script like most other versions.<br>
 * The reason for this is that they need to know, during execution, output port details mime type, depth and name.
 * 
 * @author Stuart Owen
 *
 */
public class XMLOutputSplitterLocalWorkerTranslator extends AbstractActivityTranslator<XMLSplitterConfigurationBean> {

	@Override
	protected XMLSplitterConfigurationBean createConfigType(Processor processor)
			throws ActivityTranslationException {
		XMLSplitterConfigurationBean bean = new XMLSplitterConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		LocalServiceProcessor localServiceProcessor = (LocalServiceProcessor)processor;
		//TODO: doing this with introspection would remove the dependency on taverna-java-processor
		Element element = ((XMLExtensible)localServiceProcessor.getWorker()).provideXML();
		String xml = new XMLOutputter().outputString(element);
		bean.setWrappedTypeXML(xml);
		return bean;
	}

	@Override
	protected Activity<XMLSplitterConfigurationBean> createUnconfiguredActivity() {
		return new XMLOutputSplitterActivity();
	}

	public boolean canHandle(Processor processor) {
		boolean result = false;
		if (processor != null) {
			if (processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor")) {
				try {
					String localworkerClassName = getWorkerClassName(processor);
					result = "org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter".equals(localworkerClassName);
				}
				catch(Exception e) {
					result = false;
				}
			}
		}
		return result;
	}
	
	private String getWorkerClassName(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getWorkerClassName");
			return (String) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getWorkerClassName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getWorkerClassName, an therefore does not conform to being a LocalService processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getWorkerClassName on the LocalService processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getWorkerClassName on the LocalService processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getWorkerClassName on the LocalService processor",e);
		}
	}

}
