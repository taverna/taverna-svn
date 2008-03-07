package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.util.AsynchRunnable;

/**
 * Implemented by {@link ReferenceSchemeTranslatorImpl} to translate in an
 * asynchronous manner
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@SuppressWarnings("unchecked")
public interface AsynchRefScheme extends AsynchRunnable<ReferenceScheme> {

}