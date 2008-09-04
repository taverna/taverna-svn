/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.translator;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * SPI registry for {@link Translator}s. Used by
 * {@link ReferenceSchemeTranslator}.
 * {@link #getTranslators(ReferenceScheme, Class)} return a filtered list of
 * translators using {@link Translator#canTranslate(ReferenceScheme, Class)}.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("unchecked")
public class TranslatorRegistry extends SPIRegistry<Translator> {

	private static TranslatorRegistry instance = null;

	/**
	 * Get (create if necessary) the {@link TranslatorRegistry} singleton.
	 * 
	 * @return The {@link TranslatorRegistry} instance.
	 */
	public static synchronized TranslatorRegistry getInstance() {
		if (instance == null) {
			instance = new TranslatorRegistry();
		}
		return instance;
	}

	/**
	 * Protected constructor, use {@link #getInstance()}
	 * 
	 * @see #getInstance()
	 */
	protected TranslatorRegistry() {
		super(Translator.class);
	}

	/**
	 * Find {@link Translator}s that can translate from <code>fromScheme</code>
	 * to <code>toType</code>.
	 * 
	 * @param fromScheme
	 *            {@link ReferenceScheme} to translate
	 * @param preference
	 *            {@link ReferenceScheme} class to translate to
	 * @return A (possibly empty) list of {@link Translator}s
	 */
	public <Translated extends ReferenceScheme> List<Translator<Translated>> getTranslators(
			DataPeer dataPeer,
			ReferenceScheme fromScheme, TranslationPreference preference) {
		List<Translator<Translated>> translators = new ArrayList<Translator<Translated>>();
		for (Translator translator : getInstances()) {
			if (translator.canTranslate(dataPeer, fromScheme, preference)) {
				translators.add(translator);
			}
		}
		return translators;
	}
}
