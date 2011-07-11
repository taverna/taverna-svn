package net.sf.taverna.t2.provenance.client;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class PropertiesReader {
	private static String BUNDLE_NAME = "net.sf.taverna.t2.provenance.client.resources.APIClient"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	public static void setBundleName(String bundleName) { 
		BUNDLE_NAME = bundleName;
	}

	public String getBundleName()  { return BUNDLE_NAME; }

	public PropertiesReader() {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException e){
			throw new java.util.MissingResourceException(e.getMessage(), e.getClassName(), e.getKey());
		}
	}


	public PropertiesReader(String bundleName) {
		BUNDLE_NAME = bundleName;
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(bundleName);
			RESOURCE_BUNDLE = new PropertyResourceBundle(fis);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	public String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}