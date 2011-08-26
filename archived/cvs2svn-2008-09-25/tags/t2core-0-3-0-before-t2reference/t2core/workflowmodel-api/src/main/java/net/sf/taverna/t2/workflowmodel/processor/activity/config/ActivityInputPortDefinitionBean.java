package net.sf.taverna.t2.workflowmodel.processor.activity.config;

import java.util.List;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * A bean that describes properties of an Input port.
 * 
 * @author Stuart Owen
 *
 */
public class ActivityInputPortDefinitionBean extends ActivityPortDefinitionBean {

	private List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes;
	
	private Class<?> translatedElementType;
	
	private boolean allowsLiteralValues;

	public List<Class<? extends ReferenceScheme<?>>> getHandledReferenceSchemes() {
		return handledReferenceSchemes;
	}

	public void setHandledReferenceSchemes(
			List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes) {
		this.handledReferenceSchemes = handledReferenceSchemes;
	}

	public Class<?> getTranslatedElementType() {
		return translatedElementType;
	}

	public void setTranslatedElementType(Class<?> translatedElementType) {
		this.translatedElementType = translatedElementType;
	}

	public boolean getAllowsLiteralValues() {
		return allowsLiteralValues;
	}

	public void setAllowsLiteralValues(boolean allowsLiteralValues) {
		this.allowsLiteralValues = allowsLiteralValues;
	}
	
	
}
