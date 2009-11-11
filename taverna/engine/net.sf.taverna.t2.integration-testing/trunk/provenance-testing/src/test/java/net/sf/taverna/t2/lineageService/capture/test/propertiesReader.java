package net.sf.taverna.t2.lineageService.capture.test;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class propertiesReader {
	private static final String BUNDLE_NAME = "net.sf.taverna.t2.lineageService.capture.test.CaptureTestFiles"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private propertiesReader() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
