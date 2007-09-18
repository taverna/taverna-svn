package net.sf.taverna.t2.cyclone.translators;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;

/**
 * <p>
 * An Abstract implementation of an {@link ActivityTranslator}, which is responsible for
 * creating an Activity corresponding to a Taverna 1 Processor.
 * </p>
 * <p>
 * This abstract class contains the translations steps common to all Acivities. Subclasses should implement additional
 * steps for configuring more specific properties for that Activity.
 * </p>
 * <p>
 * In particular, if an Activity configuration type is an implementation of {@link ActivityConfigurationBean}
 * then {@link AbstractActivityTranslator#populateConfigurationBeanPortDetails(Processor, ActivityConfigurationBean)}
 * can be useful for setting up the input and output ports.
 * </p>
 * 
 * @author Stuart Owen
 *
 * @param <ConfigurationType>
 */
public abstract class AbstractActivityTranslator<ConfigurationType> implements
		ActivityTranslator<ConfigurationType> {

	
	public Activity<ConfigurationType> doTranslation(Processor processor)
			throws ActivityConfigurationException {
		Activity<ConfigurationType> activity = createUnconfiguredActivity();
		ConfigurationType configType = createConfigType(processor);
		activity.configure(configType);

		return activity;
	}

	/**
	 * @return an Activity that has yet to be fully configured.
	 */
	protected abstract Activity<ConfigurationType> createUnconfiguredActivity();

	/**
	 * Creates a configuration type and populates its properties with details extracted from the Processor
	 * required to configure a corresponding Activity.
	 * 
	 * @param processor
	 * @return the configuration type.
	 */
	protected abstract ConfigurationType createConfigType(Processor processor);

	/**
	 * If the configuration type for an Activity implements {@link ActivityConfigurationBean} then this
	 * helper method is available to transfer input and output port details from the processor to the configuration type.
	 * @param processor
	 * @param configBean
	 */
	protected void populateConfigurationBeanPortDetails(Processor processor,
			ActivityConfigurationBean configBean) {
		List<String> inputPortNames = new ArrayList<String>();
		List<Integer> inputPortDepth = new ArrayList<Integer>();

		List<String> outputPortNames = new ArrayList<String>();
		List<Integer> outputPortDepth = new ArrayList<Integer>();
		List<Integer> outputPortGranularDepth = new ArrayList<Integer>();

		for (InputPort inputPort : processor.getInputPorts()) {
			inputPortNames.add(inputPort.getName());
			inputPortDepth.add(determineDepthFromSyntacticType(inputPort.getSyntacticType()));
		}

		for (OutputPort outPort : processor.getOutputPorts()) {
			outputPortNames.add(outPort.getName());
			outputPortDepth.add(determineDepthFromSyntacticType(outPort.getSyntacticType()));
			
			//TODO: check correct default value for granular depth. Setting this to the same as depth will prevent streaming.
			outputPortGranularDepth.add(0);
		}
		
		configBean.setInputPortNames(inputPortNames);
		configBean.setInputPortDepth(inputPortDepth);
		configBean.setOutputPortNames(outputPortNames);
		configBean.setOutputPortDepth(outputPortDepth);
		configBean.setOutputPortGranularDepth(outputPortGranularDepth);
		
	}

	/**
	 * @param syntacticType
	 * @return the depth determined from the syntactic mime type of the original port. i.e text/plain = 0, l('text/plain') = 1, l(l('text/plain')) = 2, ... etc.
	 */
	protected int determineDepthFromSyntacticType(String syntacticType) {
		if (syntacticType == null) {
			return 0;
		} else {
			return syntacticType.split("l\\(").length - 1;
		}
	}
}
