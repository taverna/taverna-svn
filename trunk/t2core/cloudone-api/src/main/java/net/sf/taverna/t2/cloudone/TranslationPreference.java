package net.sf.taverna.t2.cloudone;

import java.util.Set;


public interface TranslationPreference {
	
	public Set<LocationalContext> getContexts();
	
	@SuppressWarnings("unchecked")
	public Class<? extends ReferenceScheme> getReferenceSchemeClass();
	
	public int getMaxCost();

}
