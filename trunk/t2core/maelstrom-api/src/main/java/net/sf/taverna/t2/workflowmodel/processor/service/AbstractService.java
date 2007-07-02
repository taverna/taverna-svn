package net.sf.taverna.t2.workflowmodel.processor.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

/**
 * Convenience abstract superclass for generic Service instances. Paramaterized
 * on the configuration type used by the Service implementation - when this
 * object is serialized the getConfiguration method is used to store specific
 * details of the service, this is then used immediately after a call to the
 * default constructor when deserializing from XML on a workflow load.
 * <p>
 * This class holds port sets and mappings, and returns references directly to
 * them rather than copies thereof.
 * <p>
 * If you're writing an abstract service (one that cannot be directly invoked)
 * you should extend this class for convenience. This can be useful when you
 * wish to specify some kind of abstract definition of a process which will be
 * bound at workflow invocation time to a particular concrete service through
 * the action of a custom dispatch stack layer (which you will also provide)
 * 
 * @author Tom Oinn
 * 
 * @param <ConfigType>
 *            type of configuration object to be used to hold config information
 */
public abstract class AbstractService<ConfigType> extends
		AbstractAnnotatedThing implements Service<ConfigType> {

	protected Map<String, String> inputPortMapping = new HashMap<String, String>();

	protected Map<String, String> outputPortMapping = new HashMap<String, String>();

	protected Set<OutputPort> outputPorts = new HashSet<OutputPort>();

	protected Set<InputPort> inputPorts = new HashSet<InputPort>();

	public abstract void configure(ConfigType conf)
			throws ServiceConfigurationException;

	public abstract ConfigType getConfiguration();

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

	protected void addInput(String portName, int portDepth) {
		inputPorts.add(new ServiceInputPort(portName, portDepth));
	}

	protected void addOutput(String portName, int portDepth, int granularDepth) {
		outputPorts.add(new ServiceOutputPort(portName, portDepth,
				granularDepth));
	}

	/**
	 * Convenience method, creates a new output port with depth and granular
	 * depth both set to the value for depth, i.e. no streaming behaviour
	 * 
	 * @param portName
	 * @param portDepth
	 */
	protected void addOutput(String portName, int portDepth) {
		addOutput(portName, portDepth, portDepth);
	}

}
