package uk.ac.soton.mib104.t2.activities.oauth.util;

/**
 * MutableApiDesc is a mutable version of the ApiDesc interface. 
 * 
 * @author Mark Borkum
 * 
 * @see ApiDesc
 */
public interface MutableApiDesc extends ApiDesc {
	
	/**
	 * Sets all properties.  
	 * 
	 * @param apiDesc  another ApiDesc
	 */
	public void set(ApiDesc apiDesc);

	/**
	 * Sets the HTTP-URL that is used to request OAuth access tokens.
	 * 
	 * @param accessTokenEndpoint  the HTTP-URL that is used to request OAuth access tokens.
	 */
	public void setAccessTokenEndpoint(String accessTokenEndpoint);

	/**
	 * Sets the format that is used to represent OAuth access tokens.
	 * 
	 * @param accessTokenFormat  the format that is used to represent OAuth access tokens.
	 */
	public void setAccessTokenFormat(Format accessTokenFormat);

	/**
	 * Sets the HTTP method that is used to request OAuth access tokens.
	 * 
	 * @param accessTokenVerb  the HTTP method that is used to request OAuth access tokens.
	 */
	public void setAccessTokenVerb(HttpVerb accessTokenVerb);

	/**
	 * Sets the base for the User Authorization URL.
	 * 
	 * @param authorizationEndpoint  the base for the User Authorization URL.
	 */
	public void setAuthorizationEndpoint(String authorizationEndpoint);

	/**
	 * Sets the HTTP-URL that is used to request OAuth request tokens.
	 * 
	 * @param requestTokenEndpoint  the HTTP-URL that is used to request OAuth request tokens.
	 */
	public void setRequestTokenEndpoint(String requestTokenEndpoint);

	/**
	 * Specifies if an OAuth request token is required by this service.
	 * 
	 * @param requestTokenRequired  <code>true</code> if an OAuth request token is required by this service, otherwise, <code>false</code>.
	 */
	public void setRequestTokenRequired(boolean requestTokenRequired);
	
	/**
	 * Sets the format that is used to represent OAuth request tokens.
	 * 
	 * @param requestTokenVerb  the format that is used to represent OAuth request tokens.
	 */
	public void setRequestTokenVerb(HttpVerb requestTokenVerb);

}
