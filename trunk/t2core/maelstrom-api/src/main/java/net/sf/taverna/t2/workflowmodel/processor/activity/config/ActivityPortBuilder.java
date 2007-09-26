package net.sf.taverna.t2.workflowmodel.processor.activity.config;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * <p>
 * An interface defining a responsibility to building instances of Input and Output 
 * ports for an {@link Activity}. 
 * </p>
 *
 * @see AbstractActivity
 *  
 * @author Stuart Owen
 *
 */
public interface ActivityPortBuilder {
	
	/**
	 * Builds an instance of an {@link InputPort} for an Activity.
	 * @param portName
	 * @param portDepth
	 * @param mimeTypes
	 * @return
	 */
	InputPort buildInputPort(String portName, int portDepth, List<String> mimeTypes);
	
	/**
	 * Builds an instance of an {@link OutputPort} for an Activity.
	 * @param portName
	 * @param portDepth
	 * @param portGranularDepth
	 * @param mimeTypes
	 * @return
	 */
	OutputPort buildOutputPort(String portName, int portDepth, int portGranularDepth, List<String> mimeTypes);
	
}
