package net.sf.taverna.raven.launcher;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * An authenticator that returns proxy credentials from system properties
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class ProxyAuthenticator extends Authenticator {
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType().equals(RequestorType.PROXY)) {
			String username = System
					.getProperty(LauncherHttpProxyConfiguration.PROXY_USER);
			String password = System
					.getProperty(LauncherHttpProxyConfiguration.PROXY_PASSWORD);
			if (username != null && password != null) {
				return new PasswordAuthentication(username, password
						.toCharArray());
			}
		}
		return null;
	}
}
