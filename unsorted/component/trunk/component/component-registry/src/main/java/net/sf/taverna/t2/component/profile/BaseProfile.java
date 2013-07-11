/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class BaseProfile {

	private final Logger logger = Logger.getLogger(BaseProfile.class);

	private static BaseProfile instance = null;
	
	private static String BASE_PROFILE_PATH = "BaseProfile.xml";
	
	private static String BASE_PROFILE_URI = "http://build.mygrid.org.uk/taverna/BaseProfile.xml";
	
	private static int TIMEOUT = 5000;

	private static String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
	private static SimpleDateFormat format = new SimpleDateFormat(pattern);
	
	private ComponentProfile profile = null;
	
	public static synchronized BaseProfile getInstance() {
		if (instance == null) {
			instance = new BaseProfile();
		}
		return instance;
	}
	
	private BaseProfile() {
		File configFile = getBaseProfileFile();
		boolean load = false;
		long noticeTime = -1;
		long lastCheckedTime = -1;

		HttpClient client = new HttpClient();
		client.setConnectionTimeout(TIMEOUT);
		client.setTimeout(TIMEOUT);

		String message = null;
		String noticeTimeString = null;

		try {
			URI noticeURI = new URI(BASE_PROFILE_URI);
			HttpMethod method = new GetMethod(noticeURI.toString());
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("HTTP status " + statusCode + " while getting "
						+ noticeURI);
			} else {
			Header h = method.getResponseHeader("Last-Modified");
			message = method.getResponseBodyAsString();
			if (h != null) {
				noticeTimeString = h.getValue();
				noticeTime = format.parse(noticeTimeString).getTime();
				logger.info("NoticeTime is " + noticeTime);
			}
			}
		} catch (URISyntaxException e) {
			logger.error("URI problem", e);
		} catch (IOException e) {
			logger.info("Could not read notice", e);
		} catch (ParseException e) {
			logger.error("Could not parse last-modified time", e);
		}
		if (configFile.exists()) {
			lastCheckedTime = configFile.lastModified();
		}

		if ((noticeTimeString != null) && (noticeTime != -1)) {
			if (noticeTime > lastCheckedTime) {
				try {
					profile = new ComponentProfile(null, new URL(BASE_PROFILE_URI));
					FileUtils.writeStringToFile(configFile, profile.getXML());
				} catch (MalformedURLException e) {
					logger.error("URI problem", e);
					profile = null;
				} catch (ComponentRegistryException e) {
					logger.error("Component Registry problem", e);
					profile = null;
				} catch (IOException e) {
					logger.error("Unable to write profile", e);
					profile = null;
				}
			}
		}
		
		if ((profile == null) && configFile.exists()) {
			try {
				profile = new ComponentProfile(null, configFile.toURI().toURL());
			} catch (MalformedURLException | ComponentRegistryException e) {
				logger.error("URI problem", e);
				profile = null;
			}
		}

	}
	
	private File getBaseProfileFile() {
		final File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		final File config = new File(home,"conf");
		if (!config.exists()) {
			config.mkdir();
		}
		final File configFile = new File(config,
				BASE_PROFILE_PATH);
		return configFile;
		
	}

	public ComponentProfile getProfile() {
		return profile;
	}
	
}
