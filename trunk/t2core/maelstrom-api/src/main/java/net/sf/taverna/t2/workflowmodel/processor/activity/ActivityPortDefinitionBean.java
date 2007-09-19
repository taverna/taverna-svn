package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.List;

import net.sf.taverna.t2.annotation.MimeType;

/**
 * <p>
 * Defines a configuration type that relates directly to an {@link Activity} and in particular defines details its
 * input and output ports.<br>
 * An Activity that has its ports implicitly defined may define a ConfigType that implements this interface, but this is not enforced. 
 * </p>
 * <p>
 * The properties need to be accessible through matching getters and setters, to conform to being a Java Bean for the purposes
 * of serialization to and from XML. For this reason the properties are stored as Lists, and the name,depth and, in the case of output port granularityDepth, correspond to
 * a given port by their position in each list. 
 * </p>
 * <p>
 * The abstract class {@link ActivityPortDefinitionBeanImpl} facilitates in implementing this interface.
 * </p>
 * 
 * @author Stuart Owen
 *
 */
public interface ActivityPortDefinitionBean {

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
	
	public List<MimeType> getOutputPortMimeTypes();
	
	public void setOutputPortMimeTypes(List<MimeType> mimeTypes);
	
	public List<MimeType> getInputPortMimeTypes();
	
	public void setInputPortMimeTypes(List<MimeType> mimeType);
}
