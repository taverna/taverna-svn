package net.sf.taverna.t2.cloudone.refscheme;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information for reference provides mandatory keys for reference scheme
 * validation.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 * @param <RS> {@link ReferenceScheme} subclass
 */
@SuppressWarnings("unchecked")
public interface ReferenceSchemeInfoSPI<RS extends ReferenceScheme> {

	/**
	 * The keys that this reference scheme information will require the DataManager
	 * instance to provide values for to handle requests for contextual
	 * validation of reference scheme instances. The set contains lists of
	 * strings, each list is interpreted as a path into a configuration tree -
	 * where this list is a single string this is equivalent to a flat
	 * properties file style configuration document.
	 *
	 * @return Required keys
	 */
	public Map<String, Set<List<String>>> getRequiredKeys();
}
