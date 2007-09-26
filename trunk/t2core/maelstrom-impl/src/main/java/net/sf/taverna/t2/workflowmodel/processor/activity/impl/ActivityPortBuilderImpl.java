package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.annotation.impl.MimeTypeImpl;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortBuilder;

/**
 * <p>
 * An implementation of the {@link ActivityPortBuilder} that specifically ties it to 
 * the {@link ActivityInputPortImpl} and {@link ActivityOutputPortImpl} implementations.
 * </p>
 * @author Stuart Owen
 *
 */
public class ActivityPortBuilderImpl implements ActivityPortBuilder {

	private static ActivityPortBuilder instance = new ActivityPortBuilderImpl();
	
	/**
	 * Provides a instance of the ActivityPortBuilder.
	 */
	public static ActivityPortBuilder getInstance() {
		return instance;
	}
	
	
	/**
	 * Builds an instance of {@link ActivityInputPortImpl}
	 */
	public InputPort buildInputPort(String portName, int portDepth,
			List<String> mimeTypes) {
		Set<WorkflowAnnotation> annotations = createMimeTypeSet(mimeTypes);
		return new ActivityInputPortImpl(portName,portDepth,annotations);
	}

	/**
	 * Builds an instance of {@link ActivityOutputPortImpl}
	 */
	public OutputPort buildOutputPort(String portName, int portDepth,
			int portGranularDepth, List<String>mimeTypes) {
		Set<WorkflowAnnotation> annotations = createMimeTypeSet(mimeTypes);
		return new ActivityOutputPortImpl(portName,portDepth,portGranularDepth,annotations);
	}
	
	private Set<WorkflowAnnotation> createMimeTypeSet(List<String> mimeTypes) {
		Set<WorkflowAnnotation> annotations = new HashSet<WorkflowAnnotation>();
		for (String mimeType : mimeTypes) {
			annotations.add(new MimeTypeImpl(mimeType));
		}
		return annotations;
	}

}
