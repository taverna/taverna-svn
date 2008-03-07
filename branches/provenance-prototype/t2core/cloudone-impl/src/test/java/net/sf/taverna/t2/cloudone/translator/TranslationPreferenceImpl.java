package net.sf.taverna.t2.cloudone.translator;

import java.util.Set;

import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

public class TranslationPreferenceImpl implements TranslationPreference {

	public static final int UNLIMITED_COST = -1;

	@SuppressWarnings("unchecked")
	private Class<? extends ReferenceScheme> referenceSchemeClass;
	private Set<LocationalContext> contexts;
	private int maxCost;

	@SuppressWarnings("unchecked")
	public TranslationPreferenceImpl(
			Class<? extends ReferenceScheme> referenceSchemeClass,
			Set<LocationalContext> contexts) {
		this(referenceSchemeClass, contexts, UNLIMITED_COST);
	}

	@SuppressWarnings("unchecked")
	public TranslationPreferenceImpl(
			Class<? extends ReferenceScheme> referenceSchemeClass,
			Set<LocationalContext> contexts, int maxCost) {
		this.referenceSchemeClass = referenceSchemeClass;
		this.contexts = contexts;
		this.maxCost = maxCost;
	}

	public Set<LocationalContext> getContexts() {
		return contexts;
	}

	public int getMaxCost() {
		return maxCost;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends ReferenceScheme> getReferenceSchemeClass() {
		return referenceSchemeClass;
	}

}
