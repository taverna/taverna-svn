package net.sf.taverna.t2.security.credentialmanager;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

/**
 * Credential manager backed {@link Authenticator}.
 * <p>
 * Initialize by using: <code>
 * Authenticator.setDefault(new CredentialManagerAuthenticator());
 * </code>
 * <p>
 * Special case included for proxy authentication
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class CredentialManagerAuthenticator extends Authenticator {

	private static Logger logger = Logger
			.getLogger(CredentialManagerAuthenticator.class);

	private CredentialManager credManager;

	public CredentialManagerAuthenticator() {
		this.setCredManager(null); // Discover when first needed
	}

	public CredentialManagerAuthenticator(CredentialManager credManager) {
		this.setCredManager(credManager);
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public CredentialManager getCredManager() {
		if (credManager == null) {
			try {
				credManager = CredentialManager.getInstance();
			} catch (CMException e) {
				logger.warn("Could not obtain Credential Manager instance.", e);
			}
		}
		return credManager;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType().equals(RequestorType.PROXY)) {
			String password = System.getProperty("http.proxyPassword");
			String username = System.getProperty("http.proxyUser");
			if (username == null || password == null) {
				// No proxy authentication set
				return null;
			} else {
				return new PasswordAuthentication(username, password
						.toCharArray());
			}
		}

		URI uri;
		if (getRequestingURL() != null) {
			try {
				uri = getRequestingURL().toURI();
			} catch (URISyntaxException e) {
				logger.warn("Unsupported request (invalid URL) for "
						+ getRequestingURL());
				return null;
			}
		} else {
			// Construct an URI of socket://hostname:port
			String host = getRequestingHost();
			if (host == null) {
				// Use IP adress
				host = getRequestingSite().getHostAddress();
			}
			int port = getRequestingPort();
			if (host == null || port < 0) {
				logger.warn("Unsupported request for " + getRequestingScheme()
						+ " " + getRequestingSite());
				return null;
			}
			uri = URI.create("socket://" + host + ":" + port);
		}

		CredentialManager cm = getCredManager();
		if (cm == null) {
			logger.warn("No credential manager");
			return null;
		}
		boolean usePathRecursion = false;
		String realm = getRequestingPrompt();
		if (getRequestingScheme().equals("basic")
				|| getRequestingScheme().equals("digest")) {
			usePathRecursion = true;
			if (realm != null && realm.length() > 0) {
				try {
					uri = CMUtils.resolveUriFragment(uri, realm);
				} catch (URISyntaxException e) {
					logger.warn("Could not URI-encode fragment for realm: "
							+ realm);
				}
			}
		}

		UsernamePassword usernameAndPassword;
		try {
			usernameAndPassword = cm.getUsernameAndPasswordForService(uri,
					usePathRecursion, realm);
		} catch (CMException e) {
			logger.warn("Could not get username and password for " + uri, e);
			return null;
		}
		if (usernameAndPassword == null) {
			logger.warn("No username/password found for " + uri);
			return null;
		}
		PasswordAuthentication pwAuth = new PasswordAuthentication(
				usernameAndPassword.getUsername(), usernameAndPassword
						.getPassword());
		usernameAndPassword.resetPassword();
		return pwAuth;
	}

	
	
}
