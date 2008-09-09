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
