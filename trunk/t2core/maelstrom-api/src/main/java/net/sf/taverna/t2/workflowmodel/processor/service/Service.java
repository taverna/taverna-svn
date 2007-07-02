package net.sf.taverna.t2.workflowmodel.processor.service;

import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

/**
 * Defines a single abstract or concrete invokable service. Each Processor
 * contains at least one of these and may contain many, similarly the dispatch
 * stack may create new Service instances from e.g. dynamic lookup or resolution
 * of an abstract service to a concrete service or set of services.
 * 
 * @author Tom Oinn
 * 
 */
public interface Service<ConfigurationType> extends Annotated {

	/**
	 * Each Service implementation stores configuration within a bean of type
	 * ConfigurationType, this method returns the configuration. This is used by
	 * the automatic serialization framework to store the service definition in
	 * the workflow XML.
	 */
	public ConfigurationType getConfiguration();

	/**
	 * When the Service implementation is built from the workflow definition XML
	 * the object is first constructed with a default constructor then this
	 * method is called, passing in the configuration bean returned by
	 * getConfiguration()
	 * 
	 * @throws ServiceConfigurationException
	 *             if a problem occurs when configuring the service
	 */
	public void configure(ConfigurationType conf)
			throws ServiceConfigurationException;

	/**
	 * A Service contains a set of named input ports. Names must be unique
	 * within this set.
	 * 
	 * @return the set of input ports for this service
	 */
	public Set<InputPort> getInputPorts();

	/**
	 * A processor may have different input port names to the service or
	 * services it contains. This map is keyed on the processor input port names
	 * with the corresponding value being the service port name.
	 * 
	 * @return mapping from processor input port names to service input port
	 *         names
	 */
	public Map<String, String> getInputPortMapping();

	/**
	 * A Service contains a set of named output ports. As with input ports names
	 * must be unique within the set.
	 * 
	 * @return
	 */
	public Set<OutputPort> getOutputPorts();

	/**
	 * Outputs of the service may be named differently to those of the
	 * processor. This map is keyed on service output port name with each
	 * corresponding value being the processor output port name to which the
	 * service output is bound.
	 * 
	 * @return mapping from service output port name to processor output port
	 *         name
	 */
	public Map<String, String> getOutputPortMapping();

}
