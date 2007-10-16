package net.sf.taverna.t2.cloudone.translator;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.util.SPIRegistry;

public class TranslatorRegistry extends SPIRegistry<Translator> {

	private static TranslatorRegistry instance = null;

	public static TranslatorRegistry getInstance() {
		if (instance == null) {
			instance = new TranslatorRegistry();
		}
		return instance;
	}

	/**
	 * Private constructor, use {@link #getInstance()}
	 * 
	 * @see #getInstance()
	 */
	private TranslatorRegistry() {
		super(Translator.class);
	}

	/**
	 * Find {@link Translator}s that can translate from <code>fromScheme</code>
	 * to <code>toType</code>.
	 * 
	 * @param fromScheme
	 *            {@link ReferenceScheme} to translate
	 * @param toType
	 *            {@link ReferenceScheme} class to translate to
	 * @return A (possibly empty) list of {@link Translator}s
	 */
	public List<Translator> getTranslators(ReferenceScheme fromScheme,
			Class<? extends ReferenceScheme> toType) {
		List<Translator> translators = new ArrayList<Translator>();
		for (Translator translator : getInstances()) {
			if (translator.canTranslate(fromScheme, toType)) {
				translators.add(translator);
			}
		}
		return translators;
	}
}
