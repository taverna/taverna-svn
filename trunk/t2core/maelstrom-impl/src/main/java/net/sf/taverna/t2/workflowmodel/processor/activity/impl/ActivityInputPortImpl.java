package net.sf.taverna.t2.workflowmodel.processor.activity.impl;

import java.util.Collections;
import java.util.List;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

/**
 * An input port on an Activity instance. Simply used as a bean to hold port
 * name and depth properties.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * 
 */
public class ActivityInputPortImpl extends AbstractPort implements
		ActivityInputPort {

	private Class<?> translatedElementClass;
	private List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes;
	boolean allowsLiteralValues;

	/**
	 * Constructs an Activity input port instance with the provided name and
	 * depth.
	 * 
	 * @param portName
	 * @param portDepth
	 */
	public ActivityInputPortImpl(String portName, int portDepth) {
		super(portName, portDepth);
	}

	/**
	 * Constructs an Activity input port with the provided name and depth,
	 * together with a list of predetermined annotations.
	 * 
	 * @param portName
	 * @param portDepth
	 */
	public ActivityInputPortImpl(String portName, int portDepth,
			boolean allowsLiteralValues,
			List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes,
			Class<?> translatedElementClass) {
		this(portName, portDepth);
		this.allowsLiteralValues = allowsLiteralValues;
		this.handledReferenceSchemes = handledReferenceSchemes;
		this.translatedElementClass = translatedElementClass;
	}

	public boolean allowsLiteralValues() {
		return this.allowsLiteralValues();
	}

	public List<Class<? extends ReferenceScheme<?>>> getHandledReferenceSchemes() {
		return Collections.unmodifiableList(this.handledReferenceSchemes);
	}

	public Class<?> getTranslatedElementClass() {
		return this.translatedElementClass;
	}

}
