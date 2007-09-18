package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.List;

/**
 * <p>
 * Defines a configuration type that relates directly to an {@link Activity} and in particular defines its
 * input and output ports.<br>
 * It is not enforced that an Activities configuration type should implement this interface,
 * but doing so allows the use of generic methods to set up the properties common to all activities. 
 * </p>
 * <p>
 * The properties need to be accessible through matching getters and setters, to conform to being a Java Bean for the purposes
 * of serialization to and from XML. For this reason the properties are stored as Lists, and the name,depth and, in the case of output port granularityDepth, correspond to
 * a given port by their position in each list. 
 * </p>
 * <p>
 * The abstract class {@link AbstractServiceConfigurationBean} facilitates in implementing this interfaces.
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public interface ActivityConfigurationBean {

	public List<String> getInputPortNames();
	
	public void setInputPortNames(List<String> inputNames);
	
	public List<Integer> getInputPortDepth();
	
	public void setInputPortDepth(List<Integer> inputDepth);
	
	public List<String> getOutputPortNames();
	
	public void setOutputPortNames(List<String> outputNames);
	
	public List<Integer> getOutputPortDepth();
	
	public void setOutputPortDepth(List<Integer> outputDepth);
	
	public List<Integer> getOutputPortGranularDepth();
	
	public void setOutputPortGranularDepth(List<Integer> outputGranularDepth);
	
}
