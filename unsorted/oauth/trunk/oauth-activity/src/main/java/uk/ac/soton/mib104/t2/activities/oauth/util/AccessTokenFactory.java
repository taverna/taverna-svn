package uk.ac.soton.mib104.t2.activities.oauth.util;

import java.awt.Desktop;
import java.net.URI;

import javax.swing.JOptionPane;

import org.scribe.builder.api.Api;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * AccessTokenFactory is a factory for creating instances of the AccessToken class.
 * 
 * @author Mark Borkum
 * 
 * @see AccessToken
 */
public final class AccessTokenFactory {
	
	/**
	 * The message that is displayed by the "User Authorization URL" dialog.
	 */
	private static final String MESSAGE = new StringBuilder() // use a StringBuilder to make the code more readable.
		.append("Please approve Taverna Workbench by accessing the URL below.")
		.append('\n')
		.append("To complete the authorization process, please replace the URL with your unique verification code, and then click \"OK\".")
		.toString();
	
	/**
	 * Returns a new instance of the AccessToken class.   
	 * 
	 * @param apiDesc  the OAuth API description.
	 * @param clientId  the OAuth client identifier (referred to as the "consumer_key", "client_id", etc.)
	 * @param clientSecret  the OAuth client secret (referred to as the "consumer_secret", "client_secret", etc.)
	 * @param redirectEndpoint  the OAuth call-back URI (referred to as the "callback", "redirect_uri", etc.) 
	 * @param scope  the OAuth scope. 
	 * @return  a new instance of the Access Token class. 
	 */
	public static final AccessToken createAccessToken(final ApiDesc apiDesc, final String clientId, final String clientSecret, final String redirectEndpoint, final String scope) {
		// Realize the API description.  
		final Api api = OAuthActivityUtils.createApi(apiDesc);
		
		// Construct a new OAuth service. 
		final OAuthService service = OAuthActivityUtils.createServiceBuilder(api, clientId, clientSecret, redirectEndpoint, scope).build();
		
		try {
			// Initialize the request token.
			Token requestToken = null;
			
			try {
				// Attempt to obtain a new request token. 
				// If request tokens are not required by the service, then the call will raise an exception.   
				requestToken = service.getRequestToken();
			} catch (final UnsupportedOperationException ex) {
				// Do nothing...
			}
			
			// Construct the User Authorization URL using the request token.  
			final String userAuthorizationUrl = service.getAuthorizationUrl(requestToken);
			
			if (userAuthorizationUrl != null) {
				if (Desktop.isDesktopSupported()) {
					final Desktop desktop = Desktop.getDesktop();
					
					if ((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE)) {
						try {
							// Attempt to open the User Authorization URL using the local Web browser.  
							desktop.browse(URI.create(userAuthorizationUrl));
						} catch (final Exception ex) {
							// Do nothing...
						}
					}
				}
				
				// Show the input dialog. 
				// The user is prompted to navigate to the User Authorization URL, 
				// authenticate, and then replace said URL with an OAuth verifier. 
				final String code = (String) JOptionPane.showInputDialog(
						null, 
						MESSAGE, 
						userAuthorizationUrl, 
						JOptionPane.QUESTION_MESSAGE, 
						IconManager.getLargeIcon(), 
						null, 
						userAuthorizationUrl
					);
				
				if (code != null) {
					// Construct a new verifier using the supplied code. 
					final Verifier verifier = new Verifier(code);
					
					// Construct a new access token. 
					final Token accessToken = service.getAccessToken(requestToken, verifier);
					
					if (accessToken != null) {
						// Success!
						return createAccessToken(requestToken, accessToken);
					}
				}
			}
			
			// Partial failure...
			return createAccessToken(requestToken, null);
		} catch (final Exception ex) {
			// Do nothing...
		}
		
		// Total failure...
		return createAccessToken(null, null);
	}
	
	/**
	 * Returns an instance of the AccessToken class that acts as a delegate for the supplied request and access tokens. 
	 * 
	 * @param requestToken  the OAuth request token.
	 * @param accessToken  the OAuth access token.
	 * @return  an instance of the AccessToken class that acts as a delegate for the supplied request and access tokens.
	 */
	private static final AccessToken createAccessToken(final Token requestToken, final Token accessToken) {
		return new AccessToken() {
			
			@Override
			public String getAccessToken() {
				return (accessToken == null) ? null : accessToken.getToken();
			}

			@Override
			public String getAccessTokenSecret() {
				return (accessToken == null) ? null : accessToken.getSecret();
			}

			@Override
			public String getRequestToken() {
				return (requestToken == null) ? null : requestToken.getToken();
			}

			@Override
			public String getRequestTokenSecret() {
				return (requestToken == null) ? null : requestToken.getSecret();
			}
			
		};
	}
	
	/**
	 * Sole constructor. 
	 */
	private AccessTokenFactory() {
		super();
	}

}
