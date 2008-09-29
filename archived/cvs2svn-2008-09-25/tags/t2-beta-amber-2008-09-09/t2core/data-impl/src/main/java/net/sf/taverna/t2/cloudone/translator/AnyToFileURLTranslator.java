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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.commons.io.IOUtils;

/**
 * Translator for any type of {@link ReferenceScheme} to a file:/// based
 * {@link HttpReferenceScheme}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class AnyToFileURLTranslator implements Translator<HttpReferenceScheme> {
	/**
	 * Translate the {@link ReferenceScheme} to a {@link HttpReferenceScheme}
	 */
	@SuppressWarnings("unchecked")
	public HttpReferenceScheme translate(DataPeer dataPeer,
			ReferenceScheme ref, TranslationPreference preference) throws DereferenceException,
			TranslatorException {
		File translatedFile;
		try {
			translatedFile = File.createTempFile("translated", ".blob");
		} catch (IOException e) {
			throw new TranslatorException("Could not create temporary file", e);
		}

		// Store content
		OutputStream outStream;
		try {
			outStream = new FileOutputStream(translatedFile);
		} catch (FileNotFoundException e) {
			throw new TranslatorException(e);
		}
		InputStream inStream = ref.dereference(dataPeer.getDataManager());
		try {
			IOUtils.copy(inStream, outStream);
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			throw new TranslatorException("Could not write " + ref + " to "
					+ translatedFile, e);
		}

		URL url;
		try {
			url = translatedFile.toURI().toURL();
		} catch (MalformedURLException e) {
			// Unexpected
			throw new RuntimeException("Could not convert from URI to URL", e);
		}
		// file:/// is also an URL
		return new HttpReferenceScheme(url);
	}

	/**
	 * Check whether the specified {@link ReferenceScheme} can be translated to
	 * a {@link ReferenceScheme}.
	 * 
	 * @return true if <code>toType</code> is an {@link HttpReferenceScheme}
	 */
	@SuppressWarnings("unchecked")
	public boolean canTranslate(DataPeer dataPeer, ReferenceScheme fromScheme,
			TranslationPreference preference) {
		Class<? extends ReferenceScheme> toType = preference.getReferenceSchemeClass();
		return toType.isAssignableFrom(HttpReferenceScheme.class);
	}



}
