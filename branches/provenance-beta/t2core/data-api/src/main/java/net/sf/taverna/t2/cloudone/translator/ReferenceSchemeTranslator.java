package net.sf.taverna.t2.cloudone.translator;

import java.util.List;

import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.util.AsynchRunnable;

/**
 * Translate from one {@link ReferenceScheme} to another. Uses a
 * {@link AsynchRefScheme} which is {@link Runnable} to execute the translation
 * in an Asynchronous manner.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public interface ReferenceSchemeTranslator {

	/**
	 * Translate {@link DataDocumentIdentifier} to one of the specified
	 * preferred {@link ReferenceScheme} types. If the referenced
	 * {@link DataDocument} already contains one of the desired reference
	 * schemes, that reference scheme will be returned. If not, translation will
	 * be attempted in the order of <code>preferredTypes</code>.
	 * <p>
	 * Note that this method is designed for asynchronous execution, and return
	 * an {@link AsynchRefScheme} instance which {@link Runnable#run()} method
	 * will do the actual execution. The translated ReferenceScheme is available
	 * in {@link AsynchRunnable#getResult()}, or
	 * {@link AsynchRunnable#getException()} if the execution failed.
	 * 
	 * @see AsynchRefScheme
	 * @param id
	 *            {@link DataDocumentIdentifier} to be translated
	 * @param preferences
	 *            One or more desired {@link ReferenceScheme} classes in
	 *            preferred order (var args).
	 * @return A {@link AsynchRefScheme} that must be {@link Runnable#run()} to
	 *         do the translation and return a ReferenceScheme.
	 */
	public AsynchRefScheme translateAsynch(DataDocumentIdentifier id,
			List<TranslationPreference> preferences);

}
