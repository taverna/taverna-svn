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

import java.io.InputStream;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

/**
 * Translator for any type of {@link ReferenceScheme} to a
 * {@link BlobReferenceScheme}.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
// TODO: This does not build on Java 1.6 if parameterised type is
// BlobReferenceScheme<?>
@SuppressWarnings("unchecked")
public class AnyToBlobTranslator implements Translator<BlobReferenceScheme> {

	public BlobReferenceScheme<? extends ReferenceBean> translate(
			DataPeer dataPeer, ReferenceScheme ref,
			TranslationPreference preference) throws DereferenceException,
			TranslatorException {
		DataManager dataManager = dataPeer.getDataManager();
		BlobStore blobStore = dataManager.getBlobStore();

		InputStream stream = ref.dereference(dataManager);
		BlobReferenceScheme<?> blobRef = blobStore.storeFromStream(stream);
		return blobRef;
	}

	/**
	 * Check whether the given {@link ReferenceScheme} can be translated to a
	 * {@link ReferenceScheme}.
	 * 
	 * @return true if <code>toType</code> is {@link BlobReferenceScheme}.
	 */
	public boolean canTranslate(DataPeer dataPeer, ReferenceScheme fromScheme,
			TranslationPreference preference) {
		Class<? extends ReferenceScheme> toType = preference
				.getReferenceSchemeClass();
		if (!toType.isAssignableFrom(BlobReferenceScheme.class)) {
			return false;
		}
		return BlobReferenceSchemeImpl.validInBlobContext(preference
				.getContexts(), dataPeer);
	}

}
