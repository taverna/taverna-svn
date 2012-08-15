package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class ComponentServiceProviderConfig {
	
	private static Logger logger = Logger
	.getLogger(ComponentServiceProviderConfig.class);

	private URL source;
	
	private URL familySource;

	public ComponentServiceProviderConfig() {
		super();
		try {
			source = new URL("http://www.myexperiment.org");
		} catch (MalformedURLException e) {
			logger.error(e);
		}
		familySource = null;
	}
	
	public URL getSource() {
		return source;
	}

	public void setSource(URL source) {
		this.source = source;
	}

	public URL getFamilySource() {
		return familySource;
	}

	public void setFamilySource(URL familySource) {
		this.familySource = familySource;
	}

}
