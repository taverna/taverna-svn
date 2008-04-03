package net.sf.taverna.t2.cloudone.translator;

import java.util.Set;

import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * A preference for translation using a {@link ReferenceSchemeTranslator}. A
 * preference describes which {@link ReferenceScheme} implementation is desired
 * by {@link #getReferenceSchemeClass()}, and in which locational contexts
 * using {@link #getContexts()}. An associated {@link #getMaxCost()} describes
 * a maximum cost before attempting to translate according to another
 * {@link TranslationPreference}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public interface TranslationPreference {

	/**
	 * The {@link Set} of {@link LocationalContext}s within which the
	 * translated {@link ReferenceScheme} should be valid, as according to
	 * {@link ReferenceScheme#validInContext(Set, DataPeer)}.
	 * 
	 * @return A {@link Set} of {@link LocationalContext}
	 */
	public Set<LocationalContext> getContexts();

	/**
	 * The {@link Class} of the {@link ReferenceScheme} which the translation
	 * should return.
	 * 
	 * @return The class object of a {@link ReferenceScheme} implementation
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ReferenceScheme> getReferenceSchemeClass();

	/**
	 * The maximum cost of this translation before attempting translation
	 * according to other {@link TranslationPreference}s. 
	 * 
	 * @return The maximum cost, or -1 if there is no maximum cost.
	 */
	public int getMaxCost();

}
