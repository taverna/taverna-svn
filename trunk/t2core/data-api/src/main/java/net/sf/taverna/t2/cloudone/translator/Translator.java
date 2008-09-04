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

import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Allows translation from one {@link ReferenceScheme} to another based on the
 * preferences provided by the user
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <TranslatedReferenceScheme>
 */
@SuppressWarnings("unchecked")
public interface Translator<TranslatedReferenceScheme extends ReferenceScheme> {
	/**
	 * 
	 * @param dataPeer -
	 *            the peer where the translation is taking place
	 * @param ref
	 *            the original {@link ReferenceScheme}
	 * @param preference
	 *            the {@link ReferenceScheme} required back, together with the
	 *            {@link LocationalContext} it should be valid in and the
	 *            maximum cost acceptable
	 * @return the translateds {@link ReferenceScheme}
	 * @throws DereferenceException
	 * @throws TranslatorException
	 */
	public TranslatedReferenceScheme translate(DataPeer dataPeer,
			ReferenceScheme ref, TranslationPreference preference)
			throws DereferenceException, TranslatorException;

	/**
	 * Check whether the given {@link ReferenceScheme} can be translated to the
	 * TranslatedReferenceScheme
	 * 
	 * @return true if <code>toType</code> is TranslatedReferenceScheme
	 */
	public boolean canTranslate(DataPeer dataPeer, ReferenceScheme fromScheme,
			TranslationPreference preference);

}
