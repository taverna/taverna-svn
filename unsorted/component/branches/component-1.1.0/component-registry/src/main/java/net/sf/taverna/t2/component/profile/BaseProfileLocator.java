/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.api.RegistryException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class BaseProfileLocator {
	private static final Logger logger = getLogger(BaseProfileLocator.class);
	private static final String BASE_PROFILE_PATH = "BaseProfile.xml";
	private static final String BASE_PROFILE_URI = "http://build.mygrid.org.uk/taverna/BaseProfile.xml";
	private static final int TIMEOUT = 5000;
	private static final String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
	private static final SimpleDateFormat format = new SimpleDateFormat(pattern);

	private static BaseProfileLocator instance = null;

	private ComponentProfile profile = null;

	public static synchronized BaseProfileLocator getInstance() {
		if (instance == null)
			instance = new BaseProfileLocator();
		return instance;
	}

	public static ComponentProfile getBaseProfile() {
		return getInstance().getProfile();
	}

	private BaseProfileLocator() {
		File configFile = getBaseProfileFile();
		@SuppressWarnings("unused")
		boolean load = false;
		long noticeTime = -1;
		long lastCheckedTime = -1;

		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(TIMEOUT);
		params.setSoTimeout(TIMEOUT);
		HttpClient client = new HttpClient(params);

		try {
			noticeTime = getNoticeTimestamp(noticeTime, client);
			logger.info("NoticeTime is " + noticeTime);
		} catch (URISyntaxException e) {
			logger.error("URI problem", e);
		} catch (IOException e) {
			logger.info("Could not read notice", e);
		} catch (ParseException e) {
			logger.error("Could not parse last-modified time", e);
		}
		if (configFile.exists())
			lastCheckedTime = configFile.lastModified();

		try {
			if ((noticeTime != -1) && (noticeTime > lastCheckedTime)) {
				profile = new ComponentProfile(null, new URL(BASE_PROFILE_URI));
				writeStringToFile(configFile, profile.getXML());
			}
		} catch (MalformedURLException e) {
			logger.error("URI problem", e);
			profile = null;
		} catch (RegistryException e) {
			logger.error("Component Registry problem", e);
			profile = null;
		} catch (IOException e) {
			logger.error("Unable to write profile", e);
			profile = null;
		}

		try {
			if ((profile == null) && configFile.exists())
				profile = new ComponentProfile(null, configFile.toURI().toURL());
		} catch (Exception e) {
			logger.error("URI problem", e);
			profile = null;
		}
	}

	private long getNoticeTimestamp(long noticeTime, HttpClient client)
			throws URISyntaxException, IOException, HttpException,
			ParseException {
		URI noticeURI = new URI(BASE_PROFILE_URI);
		HttpMethod method = new GetMethod(noticeURI.toString());
		int statusCode = client.executeMethod(method);
		if (statusCode != SC_OK) {
			logger.warn("HTTP status " + statusCode + " while getting "
					+ noticeURI);
			return -1;
		}
		Header h = method.getResponseHeader("Last-Modified");
		if (h != null)
			return format.parse(h.getValue()).getTime();
		return -1;
	}

	private File getBaseProfileFile() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home, "conf");
		if (!config.exists())
			config.mkdir();
		return new File(config, BASE_PROFILE_PATH);
	}

	public ComponentProfile getProfile() {
		return profile;
	}
}
