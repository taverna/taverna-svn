package uk.org.taverna.scufl2.api.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;

import uk.org.taverna.scufl2.api.common.AbstractNamed;
import uk.org.taverna.scufl2.api.common.Child;
import uk.org.taverna.scufl2.api.common.Configurable;
import uk.org.taverna.scufl2.api.common.ConfigurableProperty;
import uk.org.taverna.scufl2.api.common.ToBeDecided;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;


/**
 * @author alanrw
 *
 */
public class Processor extends AbstractNamed implements Configurable, Child<Workflow> {

	private Set<OutputProcessorPort> outputPorts = new HashSet<OutputProcessorPort>();
	private Set<InputProcessorPort> inputPorts = new HashSet<InputProcessorPort>();
	private List<IterationStrategy> iterationStrategyStack = new ArrayList<IterationStrategy>();
	private ProcessorType processorType;
	private Set<ConfigurableProperty> configurableProperties = new HashSet<ConfigurableProperty>();
	private Set<StartCondition> startConditions = new HashSet<StartCondition>();
	private ToBeDecided dispatchStack;
	
	/**
	 * @return
	 */
	@XmlElement(required=true,nillable=false)	
	public ToBeDecided getDispatchStack() {
		return dispatchStack;
	}

	/**
	 * @param dispatchStack
	 */
	public void setDispatchStack(ToBeDecided dispatchStack) {
		this.dispatchStack = dispatchStack;
	}

	/**
	 * @return
	 */
	@XmlElementWrapper( name="startConditions",nillable=false,required=true)
	@XmlElement( name="startCondition",nillable=false)
	public Set<StartCondition> getStartConditions() {
		return startConditions;
	}

	/**
	 * @param startConditions
	 */
	public void setStartConditions(Set<StartCondition> startConditions) {
		this.startConditions = startConditions;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Configurable#getConfigurableProperties()
	 */
	@XmlElementWrapper( name="configurableProperties",nillable=false,required=true)
	@XmlElement( name="configurableProperty",nillable=false)
	public Set<ConfigurableProperty> getConfigurableProperties() {
		return configurableProperties;
	}

	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Configurable#setConfigurableProperties(java.util.Set)
	 */
	public void setConfigurableProperties(
			Set<ConfigurableProperty> configurableProperties) {
		this.configurableProperties = configurableProperties;
	}

	/**
	 * @param parent
	 * @param name
	 */
	public Processor(Workflow parent, String name) {
		super(name);
		setParent(parent);
	}
	
	public Processor() {
		super();
	}

	/**
	 * @param outputPorts
	 */
	public void setOutputPorts(Set<OutputProcessorPort> outputPorts) {
		this.outputPorts.clear();
		this.outputPorts.addAll(outputPorts);
	}

	/**
	 * @return
	 */
	@XmlElementWrapper( name="outputProcessorPorts",nillable=false,required=true)
	@XmlElement( name="outputProcessorPort",nillable=false)
	public Set<OutputProcessorPort> getOutputPorts() {
		return outputPorts;
	}

	/**
	 * @param inputPorts
	 */
	public void setInputPorts(Set<InputProcessorPort> inputPorts) {
		this.inputPorts = inputPorts;
	}

	/**
	 * @return
	 */
	@XmlElementWrapper( name="inputProcessorPorts",nillable=false,required=true)
	@XmlElement( name="inputProcessorPort",nillable=false)
	public Set<InputProcessorPort> getInputPorts() {
		return inputPorts;
	}

	/**
	 * @param iterationStrategyStack
	 */
	public void setIterationStrategyStack(
			List<IterationStrategy> iterationStrategyStack) {
		this.iterationStrategyStack = iterationStrategyStack;
	}

	/**
	 * @return
	 */
	@XmlElement(required=true,nillable=false)	
	public List<IterationStrategy> getIterationStrategyStack() {
		return iterationStrategyStack;
	}

	/**
	 * @param processorType
	 */
	public void setProcessorType(ProcessorType processorType) {
		this.processorType = processorType;
	}

	/**
	 * @return
	 */
	@XmlElement(required=true,nillable=false)	
	public ProcessorType getProcessorType() {
		return processorType;
	}

	private Workflow parent;

	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Child#setParent(uk.org.taverna.scufl2.api.common.WorkflowBean)
	 */
	public void setParent(Workflow parent) {
		if (this.parent != null && this.parent != parent) {
			this.parent.getProcessors().remove(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.getProcessors().add(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.org.taverna.scufl2.api.common.Child#getParent()
	 */
	@XmlTransient
	public Workflow getParent() {
		return parent;
	}

	/**
	 * @param portName
	 * @return
	 */
	public InputProcessorPort addInputPort(String portName) {
		InputProcessorPort port = new InputProcessorPort(this, portName);
		getInputPorts().add(port);
		return port;
	}
	
	
	/**
	 * @param portName
	 * @return
	 */
	public OutputProcessorPort addOutputPort(String portName) {
		OutputProcessorPort port = new OutputProcessorPort(this, portName);
		getOutputPorts().add(port);
		return port;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" +
		"getInputPorts()=" + getInputPorts() + ", " +
		"getOutputPorts()=" + getOutputPorts() + ", " +
		// TODO: Other properties
		
		
		
		"]";
	}
	
	
}

