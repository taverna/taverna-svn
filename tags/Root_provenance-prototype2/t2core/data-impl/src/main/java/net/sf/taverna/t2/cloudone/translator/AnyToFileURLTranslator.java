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
