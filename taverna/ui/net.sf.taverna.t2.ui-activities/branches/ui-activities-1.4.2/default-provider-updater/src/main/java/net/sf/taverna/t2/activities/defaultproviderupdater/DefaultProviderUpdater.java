/**
 * 
 */
package net.sf.taverna.t2.activities.defaultproviderupdater;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

import net.sf.taverna.t2.workbench.StartupSPI;

/**
 * @author alanrw
 *
 */
public class DefaultProviderUpdater implements StartupSPI {
	
	private static final String DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME = "default_service_providers.xml";

	private File defaultConfigurableServiceProvidersFile = new File(getTavernaStartupConfigurationDirectory(),
			DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME);
	
	public static Logger logger = Logger
			.getLogger(DefaultProviderUpdater.class);

	private static int TIMEOUT = 5000;

	private static String BASE_URL = "http://www.mygrid.org.uk/taverna/updates";

	private static Profile profile = ProfileFactory.getInstance().getProfile();
	private static String version = profile.getVersion();
	
	@Override
	public boolean startup() {
		if (GraphicsEnvironment.isHeadless()) {
			return true; // if we are running headlessly just return
		}

		HttpClient client = new HttpClient();
		client.setConnectionTimeout(TIMEOUT);
		client.setTimeout(TIMEOUT);
		PluginManager.setProxy(client);
		String message = null;

		try {
			URI noticeURI = new URI(BASE_URL + "/" + version + "/" + DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME);
			HttpMethod method = new GetMethod(noticeURI.toString());
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("HTTP status " + statusCode + " while getting "
						+ noticeURI);
				return true;
			}
			Header h = method.getResponseHeader("Last-Modified");
			message = method.getResponseBodyAsString();

		} catch (URISyntaxException e) {
			logger.error("URI problem", e);
			return true;
		} catch (IOException e) {
			logger.error("Could not read " + DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME, e);
			return true;
		}

		if (message != null) {
		try {
			logger.info("Copying to " + defaultConfigurableServiceProvidersFile.getCanonicalPath());
			FileUtils.writeStringToFile(defaultConfigurableServiceProvidersFile, message);
			logger.error("Copyied to " + defaultConfigurableServiceProvidersFile.getCanonicalPath());
		} catch (IOException e) {
			logger.error(e);
		}
		}
		return true;
	}

	@Override
	public int positionHint() {
		return 10;
	}

	private static File getTavernaStartupConfigurationDirectory() {
		File distroHome = null;
		File configDirectory = null;
		try {
			distroHome = ApplicationConfig.getInstance().getStartupDir();
			configDirectory = new File(distroHome,"conf");
			if (!configDirectory.exists()) {
				configDirectory.mkdir();
			}
		} catch (IOException e) {
			logger.error("Could not get the Taverna startup directory", e);
		}
		return configDirectory;
	}	

}
