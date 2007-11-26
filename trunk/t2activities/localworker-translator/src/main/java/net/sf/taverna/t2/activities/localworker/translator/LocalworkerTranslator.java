package net.sf.taverna.t2.activities.localworker.translator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.scufl.Processor;

/**
 * An ActivityTranslator specifically for translating Taverna 1 LocalService
 * Processors to a Taverna 2 Beanshell Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class LocalworkerTranslator extends
		AbstractActivityTranslator<BeanshellActivityConfigurationBean> {
	
	private static Map<String, String> localWorkerToScript = new HashMap<String, String>();
	
	static {
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.ByteArrayToString", "ByteArrayToString");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.DecodeBase64", "DecodeBase64");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EchoList", "EchoList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EmitLotsOfStrings", "EmitLotsOfStrings");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EncodeBase64", "EncodeBase64");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.ExtractImageLinks", "ExtractImageLinks");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FailIfFalse", "FailIfFalse");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FailIfTrue", "FailIfTrue");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FilterStringList", "FilterStringList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FlattenList", "FlattenList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.PadNumber", "PadNumber");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.RegularExpressionStringList", "RegularExpressionStringList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.SplitByRegex", "SplitByRegex");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringConcat", "StringConcat");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringListMerge", "StringListMerge");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetDifference", "StringSetDifference");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetIntersection", "StringSetIntersection");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetUnion", "StringSetUnion");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringStripDuplicates", "StringStripDuplicates");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.TestAlwaysFailingProcessor", "TestAlwaysFailingProcessor");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.WebImageFetcher", "WebImageFetcher");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.WebPageFetcher", "WebPageFetcher");
	}

	@Override
	protected BeanshellActivity createUnconfiguredActivity() {
		return new BeanshellActivity();
	}

	@Override
	protected BeanshellActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		bean.setScript(getScript(processor));
		return bean;
	}

	public boolean canHandle(Processor processor) {
		boolean result = false;
		if (processor != null) {
			if (processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor")) {
				try {
					String localworkerClassName = getWorkerClassName(processor);
					result = localWorkerToScript.containsKey(localworkerClassName);
				} catch (ActivityTranslationException e) {
					//return false
				}
			}
		}
		return result;
	}

	private String getScript(Processor processor) throws ActivityTranslationException {
		String scriptName = localWorkerToScript.get(getWorkerClassName(processor));
		if (scriptName == null) {
			return null;
		}
		return readScript(scriptName);
	}
	
	private String readScript(String script) throws ActivityTranslationException {
		try {
			return IOUtils.toString(LocalworkerTranslator.class.getResourceAsStream("/" + script));
		} catch (IOException e) {
			throw new ActivityTranslationException("Error reading script resource for " + script, e);
		}
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
