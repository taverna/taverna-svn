package net.sf.taverna.t2.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.RshellPortTypes.SymanticTypes;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;

/**
 * An ActivityTranslator specifically for translating a Taverna 1 Rshell Processor to an equivalent Taverna 2 Rshell Activity
 * 
 * @see ActivityTranslator
 */
public class RshellActivityTranslator extends AbstractActivityTranslator<RshellActivityConfigurationBean> {

	@Override
	protected RshellActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		RshellActivityConfigurationBean bean = new RshellActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		populatePortSymanticTypeDetails(processor, bean);
		
		bean.setScript(getScript(processor));
		bean.setConnectionSettings(getConnectionSettings(processor));
		return bean;
	}

	@Override
	protected Activity<RshellActivityConfigurationBean> createUnconfiguredActivity() {
		return new RshellActivity();
	}
	
	private String getScript(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getScript");
			return (String) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("There was a Security exception whilst trying to invoke getScript through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getScript, an therefore does not conform to being an Rshell processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getScript on the Rshell processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getScript on the Rshell processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getScript on the Rshell processor",e);
		}
	}
	
	private RshellConnectionSettings getConnectionSettings(Processor processor) throws ActivityTranslationException {
		try {
			RshellConnectionSettings t2ConnectionSettings = new RshellConnectionSettings();
			Method method = processor.getClass().getMethod("getConnectionSettings");
			Object t1ConnectionSettings = method.invoke(processor);
			method = t1ConnectionSettings.getClass().getMethod("getHost");
			t2ConnectionSettings.setHost((String) method.invoke(t1ConnectionSettings));
			method = t1ConnectionSettings.getClass().getMethod("getPort");
			t2ConnectionSettings.setPort((Integer) method.invoke(t1ConnectionSettings));
			method = t1ConnectionSettings.getClass().getMethod("getUsername");
			t2ConnectionSettings.setUsername((String) method.invoke(t1ConnectionSettings));
			method = t1ConnectionSettings.getClass().getMethod("getPassword");
			t2ConnectionSettings.setPassword((String) method.invoke(t1ConnectionSettings));
			return t2ConnectionSettings;
		} catch (SecurityException e) {
			throw new ActivityTranslationException("There was a Security exception whilst trying to invoke getConnectionSettings through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getConnectionSettings, an therefore does not conform to being an Rshell processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getConnectionSettings on the Rshell processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getConnectionSettings on the Rshell processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getConnectionSettings on the Rshell processor",e);
		}
	}
	
	private SymanticTypes getSymanticTypes(Port port) throws ActivityTranslationException {
		try {
			Method method = port.getClass().getMethod("getSymanticTypes");
			Enum<?> symanticType = (Enum<?>) method.invoke(port);
			return SymanticTypes.valueOf(symanticType.name());
		} catch (SecurityException e) {
			throw new ActivityTranslationException("There was a Security exception whilst trying to invoke getSymanticTypes through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getSymanticTypes, an therefore does not conform to being an Rshell processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getSymanticTypes on the Rshell processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getSymanticTypes on the Rshell processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getSymanticTypes on the Rshell processor",e);
		}
	}
	
	private void populatePortSymanticTypeDetails(Processor processor,
			RshellActivityConfigurationBean bean) throws ActivityTranslationException {
		List<RShellPortSymanticTypeBean> inputDefinitions = new ArrayList<RShellPortSymanticTypeBean>();
		List<RShellPortSymanticTypeBean> outputDefinitions = new ArrayList<RShellPortSymanticTypeBean>();

		for (InputPort inputPort : processor.getInputPorts()) {
			RShellPortSymanticTypeBean symanticTypeBean = new RShellPortSymanticTypeBean();
			symanticTypeBean.setName(inputPort.getName());
			symanticTypeBean.setSymanticType(getSymanticTypes(inputPort));
			inputDefinitions.add(symanticTypeBean);
		}

		for (OutputPort outPort : processor.getOutputPorts()) {
			RShellPortSymanticTypeBean symanticTypeBean = new RShellPortSymanticTypeBean();
			symanticTypeBean.setName(outPort.getName());
			symanticTypeBean.setSymanticType(getSymanticTypes(outPort));
			outputDefinitions.add(symanticTypeBean);
		}
		bean.setInputSymanticTypes(inputDefinitions);
		bean.setOutputSymanticTypes(outputDefinitions);
	}
	
	public boolean canHandle(Processor processor) {
		return processor.getClass().getName().equals("nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellProcessor");
	}

}
