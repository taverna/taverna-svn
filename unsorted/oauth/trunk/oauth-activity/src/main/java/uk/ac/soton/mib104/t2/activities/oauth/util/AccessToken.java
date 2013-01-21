package uk.ac.soton.mib104.t2.activities.oauth.util;

/**
 * AccessToken is an interface. 
 * 
 * An AccessToken object encapsulates the state information of an OAuth access 
 * token.  This state information includes:
 * <ul>
 * <li>The access token</li>
 * <li>The access token secret</li>
 * <li>The request token</li>
 * <li>The request token secret</li>
 * </ul>
 * 
 * @author Mark Borkum
 */
public interface AccessToken {
	
	/**
	 * Returns the OAuth access token.
	 * 
	 * @return the OAuth access token.
	 */
	public String getAccessToken();
	
	/**
	 * Returns the OAuth access token secret.
	 * 
	 * @return the OAuth access token.
	 */
	public String getAccessTokenSecret();
	
	/**
	 * Returns the OAuth request token.
	 * 
	 * @return the OAuth request token.
	 */
	public String getRequestToken();
	
	/**
	 * Returns the OAuth request token secret.
	 * 
	 * @return the OAuth request token secret.
	 */
	public String getRequestTokenSecret();
	
}
