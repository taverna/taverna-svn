package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

@SuppressWarnings("unchecked")
public interface Translator<TranslatedReferenceScheme extends ReferenceScheme> {

	public TranslatedReferenceScheme translate(DataPeer dataPeer,
			ReferenceScheme ref, TranslationPreference preference) throws DereferenceException,
			TranslatorException;

	public boolean canTranslate(DataPeer dataPeer, ReferenceScheme fromScheme,
			TranslationPreference preference);

}
