package uk.ac.soton.mib104.t2.activities.oauth.util;

/**
 * ApiDesc is an interface. 
 * 
 * An ApiDesc object encapsulates the state information of an OAuth service.  
 * This state information includes:
 * <ul>
 * <li>The request token URL</li>
 * <li>The user authorization URL</li>
 * <li>The access token URL</li>
 * </ul>
 * 
 * @author Mark Borkum
 */
public interface ApiDesc {

	/**
	 * Returns the HTTP-URL that is used to request OAuth access tokens.
	 * 
	 * @return  the HTTP-URL that is used to request OAuth access tokens.
	 */
	public String getAccessTokenEndpoint();

	/**
	 * Returns the format that is used to represent OAuth access tokens.
	 * 
	 * @return  the format that is used to represent OAuth access tokens.
	 */
	public Format getAccessTokenFormat();

	/**
	 * Returns the HTTP method that is used to request OAuth access tokens.
	 * 
	 * @return  the HTTP method that is used to request OAuth access tokens.
	 */
	public HttpVerb getAccessTokenVerb();

	/**
	 * Returns the base for the User Authorization URL.
	 *  
	 * @return  the base for the User Authorization URL.
	 */
	public String getAuthorizationEndpoint();

	/**
	 * Returns the HTTP-URL that is used to request OAuth request tokens.
	 * 
	 * @return  the HTTP-URL that is used to request OAuth request tokens.
	 */
	public String getRequestTokenEndpoint();

	/**
	 * Returns the format that is used to represent OAuth request tokens.
	 * 
	 * @return  the format that is used to represent OAuth request tokens.
	 */
	public HttpVerb getRequestTokenVerb();
	
	/**
	 * Returns <code>true</code> if an OAuth request token is required by this service, otherwise, returns <code>false</code>
	 * 
	 * @return  <code>true</code> if an OAuth request token is required by this service, otherwise, <code>false</code>
	 */
	public boolean isRequestTokenRequired();
	
}
