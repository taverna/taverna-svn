package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;

public interface Translator {

	public ReferenceScheme translate(DataManager dataManager, ReferenceScheme ref,
			Class<? extends ReferenceScheme> type) throws DereferenceException, TranslatorException;

	public boolean canTranslate(ReferenceScheme fromScheme,
			Class<? extends ReferenceScheme> toType);

}
