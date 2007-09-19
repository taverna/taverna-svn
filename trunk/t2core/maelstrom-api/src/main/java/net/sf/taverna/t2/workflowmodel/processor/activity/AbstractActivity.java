package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.annotation.MimeType;
import net.sf.taverna.t2.annotation.WorkflowAnnotation;
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
 * @author Stuart Owen
 * 
 * @param <ConfigType>
 *            type of configuration object to be used to hold config information
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

	protected void addInput(String portName, int portDepth, Set<WorkflowAnnotation> annotations) {
		inputPorts.add(new ActivityInputPort(portName, portDepth,annotations));
	}

	protected void addOutput(String portName, int portDepth, int granularDepth, Set<WorkflowAnnotation> annotations) {
		outputPorts.add(new ActivityOutputPort(portName, portDepth,
				granularDepth,annotations));
	}

	/**
	 * Convenience method, creates a new output port with depth and granular
	 * depth both set to the value for depth, i.e. no streaming behaviour
	 * 
	 * @param portName
	 * @param portDepth
	 */
	protected void addOutput(String portName, int portDepth,Set<WorkflowAnnotation> annotations) {
		addOutput(portName, portDepth, portDepth, annotations);
	}
	
	/**
	 * <p>
	 * Simplifies configuring the Activity input and output ports if its
	 * ConfigType is an implementation of {@link ActivityPortDefinitionBean}
	 * </p>
	 * <p>
	 * For an Activity that has ports that are defined dynamically it is natural that is
	 * ConfigType will not implement this interface. 
	 * </p> 
	 * @param configBean
	 */
	protected void configurePorts(ActivityPortDefinitionBean configBean) {
		for (int i=0;i<configBean.getInputPortNames().size();i++) {
			
			String name = configBean.getInputPortNames().get(i);
			int depth = configBean.getInputPortDepth().get(i);
			Set<WorkflowAnnotation> annotations = new HashSet<WorkflowAnnotation>();
			MimeType mimeType = configBean.getInputPortMimeTypes().get(i);
			annotations.add(mimeType);
			addInput(name, depth, annotations);
		}
		
		for (int i=0;i<configBean.getOutputPortNames().size();i++) {
			String name = configBean.getOutputPortNames().get(i);
			int depth = configBean.getOutputPortDepth().get(i);
			int granularDepth = configBean.getOutputPortGranularDepth().get(i);
			Set<WorkflowAnnotation> annotations = new HashSet<WorkflowAnnotation>();
			MimeType mimeType = configBean.getOutputPortMimeTypes().get(i);
			annotations.add(mimeType);
			addOutput(name, depth, granularDepth, annotations);
		}
	}

}
