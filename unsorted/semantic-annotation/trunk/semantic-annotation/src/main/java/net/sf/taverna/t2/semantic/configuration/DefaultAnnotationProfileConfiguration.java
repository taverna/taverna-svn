/**
 * 
 */
package net.sf.taverna.t2.semantic.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * @author alanrw
 *
 */
public class DefaultAnnotationProfileConfiguration extends AbstractConfigurable {

	private static final String CONFIGURATION_UUID = "4AD2A8BA-FC45-496F-AD67-5586A98C670A";
	
	public static final String DEFAULT_PROFILE = "defaultProfile";
	private static final String DEFAULT_DEFAULT_PROFILE_URL = "http://www.mygrid.org.uk/taverna/profile";
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(DefaultAnnotationProfileConfiguration.class);

	/**
	 * Only present to please Configurable
	 */
	private Map<String, String> defaultPropertyMap;

	private static final DefaultAnnotationProfileConfiguration INSTANCE = new DefaultAnnotationProfileConfiguration();

	public static DefaultAnnotationProfileConfiguration getINSTANCE() {
		return INSTANCE;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getCategory()
	 */
	public String getCategory() {
		return "general";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDefaultPropertyMap()
	 */
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			defaultPropertyMap.put(DEFAULT_PROFILE, DEFAULT_DEFAULT_PROFILE_URL);
		}
		return defaultPropertyMap;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDisplayName()
	 */
	public String getDisplayName() {
		return "Annotation profile";
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getFilePrefix()
	 */
	public String getFilePrefix() {
		return "Annotation";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getUUID()
	 */
	public String getUUID() {
		return CONFIGURATION_UUID;
	}

	public static String getConfigurationUuid() {
		return CONFIGURATION_UUID;
	}
	
	public String getDefaultAnnotationProfile() {
		return getProperty(DEFAULT_PROFILE);
	}

	public URL getDefaultAnnotationProfileURL() {
		URL result = null;
			try {
				result = new URL(getDefaultAnnotationProfile());
			} catch (MalformedURLException e) {
				logger.error(e);
				result = null;
			}
			return result;
	}

}
