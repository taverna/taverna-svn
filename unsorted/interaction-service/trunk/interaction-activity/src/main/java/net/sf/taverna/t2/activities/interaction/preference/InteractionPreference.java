/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.preference;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

/**
 * @author alanrw
 *
 */
public class InteractionPreference {
	
	private static final String USE_JETTY = "useJetty";
	
	private static final String DEFAULT_USE_JETTY = "true";
	
	private static final String PORT = "port";

	private static final String DEFAULT_PORT = "8080";

	private static final String HOST = "host";

	private static final String DEFAULT_HOST = "http://localhost";
	
	private static final String WEBDAV_PATH = "webdavPath";
	
	private static final String DEFAULT_WEBDAV_PATH = "/interaction";
	
	private static final String FEED_PATH = "feedPath";
	
	private static final String DEFAULT_FEED_PATH = "/feed";

	private Logger logger = Logger.getLogger(InteractionPreference.class);
	
	private static InteractionPreference instance = null;
	
	private Properties properties;
	
	public static InteractionPreference getInstance() {
		if (instance == null) {
			instance = new InteractionPreference();
		}
		return instance;
	}
	
	private File getConfigFile() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home,"conf");
		if (!config.exists()) {
			config.mkdir();
		}
		File configFile = new File(config,
				this.getFilePrefix()+"-"+this.getUUID() + ".config");
		return configFile;
	}
	
	private InteractionPreference() {
		File configFile = getConfigFile();
		properties = new Properties();
		if (configFile.exists()) {
			try {
				FileReader reader = new FileReader(configFile);
				properties.load(reader);
				reader.close();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		if (GraphicsEnvironment.isHeadless() || System.getProperty("java.awt.headless").equals("true")) {
			System.err.println("Running headless");
			String definedHost = System.getProperty("taverna.interaction.host");
			if (definedHost != null){
				properties.setProperty(USE_JETTY, "false");
				logger.error("USE_JETTY set to false");
				properties.setProperty(HOST, definedHost);
			}
			String definedPort = System.getProperty("taverna.interaction.port");
			if (definedPort != null) {
				properties.setProperty(PORT, definedPort);
			}
			String definedWebDavPath = System.getProperty("taverna.interaction.webdav_path");
			if (definedWebDavPath != null) {
				properties.setProperty(WEBDAV_PATH, definedWebDavPath);
			}
			String definedFeedPath = System.getProperty("taverna.interaction.feed_path");
			if (definedFeedPath != null) {
				properties.setProperty(FEED_PATH, definedFeedPath);
			}
		}
		else {
			System.err.println("Running non-headless");
			logger.error("Running non-headless");
		}
		fillDefaultProperties();
	}

	private void fillDefaultProperties() {
		if (!properties.containsKey(USE_JETTY)) {
			properties.setProperty(USE_JETTY, DEFAULT_USE_JETTY);
			logger.error("USE_JETTY set to " + DEFAULT_USE_JETTY);
		}
		if (!properties.containsKey(PORT)) {
			properties.setProperty(PORT, DEFAULT_PORT);
		}
		if (!properties.containsKey(HOST)) {
			properties.setProperty(HOST, DEFAULT_HOST);
		}
		if (!properties.containsKey(WEBDAV_PATH)) {
			properties.setProperty(WEBDAV_PATH, DEFAULT_WEBDAV_PATH);
		}
		if (!properties.containsKey(FEED_PATH)) {
			properties.setProperty(FEED_PATH, DEFAULT_FEED_PATH);
		}
	}

	public String getFilePrefix() {
		return "Interaction";
	}
	
	public void store() {
		try {
			FileOutputStream out = new FileOutputStream(getConfigFile());
			properties.store(out, "");
			out.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public String getUUID() {
		return "DA992717-5A46-469D-AE25-883F0E4CD348";
	}

	public void setPort(String text) {
		properties.setProperty(PORT, text);
	}

	public void setHost(String text) {
		properties.setProperty(HOST, text);
	}
	
	public void setUseJetty(boolean use) {
		properties.setProperty(USE_JETTY, Boolean.toString(use));
	}
	
	public void setFeedPath(String path) {
		properties.setProperty(FEED_PATH, path);
	}

	public void setWebDavPath(String path) {
		properties.setProperty(WEBDAV_PATH, path);
	}
	
	public String getPort() {
		return properties.getProperty(PORT);
	}

	public String getHost() {
		return properties.getProperty(HOST);
	}
	
	public boolean getUseJetty() {
		return (Boolean.parseBoolean(properties.getProperty(USE_JETTY)));
	}
	
	public String getFeedPath() {
		return properties.getProperty(FEED_PATH);
	}
	
	public String getWebDavPath() {
		return properties.getProperty(WEBDAV_PATH);
	}
	
	public String getDefaultHost() {
		return DEFAULT_HOST;
	}
	
	public String getDefaultFeedPath() {
		return DEFAULT_FEED_PATH;
	}
	
	public String getDefaultWebDavPath() {
		return DEFAULT_WEBDAV_PATH;
	}

	public String getFeedUrl() {
		return getHost() + ":" + getPort() + getFeedPath();
	}

	public String getLocationUrl() {
		return getHost() + ":" + getPort() + getWebDavPath();
	}
	
	

}
