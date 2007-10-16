package net.sf.taverna.t2.cloudone.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

import org.apache.commons.io.IOUtils;

public class AnyToURLTranslator implements Translator {

	public URLReferenceScheme translate(DataManager dataManager,
			ReferenceScheme ref, Class<? extends ReferenceScheme> type)
			throws DereferenceException, TranslatorException {
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
		InputStream inStream = ref.dereference(dataManager);
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
		return new URLReferenceScheme(url);
	}

	public boolean canTranslate(ReferenceScheme fromScheme,
			Class<? extends ReferenceScheme> toType) {
		return toType.isAssignableFrom(URLReferenceScheme.class);
	}

}
