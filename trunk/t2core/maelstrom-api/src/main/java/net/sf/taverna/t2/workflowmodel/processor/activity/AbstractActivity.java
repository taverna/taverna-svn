package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

/**
 * Convenience abstract superclass for generic Activity instances. Parameterised
 * on the configuration type used by the Activity implementation - when this
 * object is serialised the getConfiguration method is used to store specific
 * details of the activity, this is then used immediately after a call to the
 * default constructor when deserialising from XML on a workflow load.
 * <p>
 * This class holds port sets and mappings, and returns references directly to
 * them rather than copies thereof.
 * <p>
 * If you're writing an abstract activity (one that cannot be directly invoked)
 * you should extend this class for convenience. This can be useful when you
 * wish to specify some kind of abstract definition of a process which will be
 * bound at workflow invocation time to a particular concrete activity through
 * the action of a custom dispatch stack layer (which you will also provide)
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * 
 * @param <ConfigType>
 *            type of configuration object to be used to hold configuration information
 */
public abstract class AbstractActivity<ConfigType> extends
		AbstractAnnotatedThing implements Activity<ConfigType> {

	protected Map<String, String> inputPortMapping = new HashMap<String, String>();

	protected Map<String, String> outputPortMapping = new HashMap<String, String>();

	protected Set<OutputPort> outputPorts = new HashSet<OutputPort>();

	protected Set<InputPort> inputPorts = new HashSet<InputPort>();

	public abstract void configure(ConfigType conf)
			throws ActivityConfigurationException;

	public abstract ConfigType getConfiguration();
	
	protected abstract ActivityPortBuilder getPortBuilder();

	public final Map<String, String> getInputPortMapping() {
		return this.inputPortMapping;
	}

	public final Set<InputPort> getInputPorts() {
		return inputPorts;
	}

	public final Map<String, String> getOutputPortMapping() {
		return this.outputPortMapping;
	}

	public final Set<OutputPort> getOutputPorts() {
		return outputPorts;
	}

	protected void addInput(String portName, int portDepth, List<String>mimeTypes) {
		inputPorts.add(getPortBuilder().buildInputPort(portName, portDepth,mimeTypes));
	}

	protected void addOutput(String portName, int portDepth, int granularDepth, List<String>mimeTypes) {
		outputPorts.add(getPortBuilder().buildOutputPort(portName, portDepth,
				granularDepth,mimeTypes));
	}

	/**
	 * Convenience method, creates a new output port with depth and granular
	 * depth both set to the value for depth, i.e. no streaming behaviour
	 * 
	 * @param portName
	 * @param portDepth
	 */
	protected void addOutput(String portName, int portDepth,List<String> mimeTypes) {
		addOutput(portName, portDepth, portDepth, mimeTypes);
	}
	
	/**
	 * <p>
	 * Simplifies configuring the Activity input and output ports if its
	 * ConfigType is an implementation of {@link ActivityPortsDefinitionBean}
	 * </p>
	 * <p>
	 * For an Activity that has ports that are defined dynamically it is natural that is
	 * ConfigType will not implement this interface. 
	 * </p> 
	 * @param configBean
	 */
	protected void configurePorts(ActivityPortsDefinitionBean configBean) {
		for (int i=0;i<configBean.getInputPortNames().size();i++) {
			String name = configBean.getInputPortNames().get(i);
			int depth = configBean.getInputPortDepth().get(i);
			addInput(name, depth, configBean.getInputPortMimeTypes().get(i));
		}
		
		for (int i=0;i<configBean.getOutputPortNames().size();i++) {
			String name = configBean.getOutputPortNames().get(i);
			int depth = configBean.getOutputPortDepth().get(i);
			int granularDepth = configBean.getOutputPortGranularDepth().get(i);
			addOutput(name, depth, granularDepth, configBean.getOutputPortMimeTypes().get(i));
		}
	}

}
