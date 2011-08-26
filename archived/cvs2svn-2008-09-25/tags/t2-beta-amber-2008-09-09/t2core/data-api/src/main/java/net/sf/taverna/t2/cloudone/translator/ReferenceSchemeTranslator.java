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
