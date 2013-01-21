package uk.ac.soton.mib104.t2.activities.oauth.util;

/**
 * ApiDescImpl is an enumeration.
 * 
 * An ApiDescImpl object encapsulates the state information of a specific
 * revision of the OAuth protocol.  These revisions are:
 * <ul>
 * <li>OAuth 1.0a</li>
 * <li>OAuth 2.0 (draft 10)</li>
 * </ul>
 * 
 * @author Mark Borkum
 */
public enum ApiDescImpl implements ApiDesc {
	
	OAuth10a(
			"1.0", "OAuth 1.0a", "OAuth Core 1.0 Revision A", "http://oauth.net/core/1.0a/",
			true, "http://www.example.com/oauth/request_token", HttpVerb.POST,
			"http://www.example.com/oauth/authorize",
			"http://www.example.com/oauth/access_token", Format.TEXT_PLAIN, HttpVerb.POST
	),
	OAuth20_draft10(
			"2.0", "OAuth 2.0 (draft 10)", "The OAuth 2.0 Protocol", "http://tools.ietf.org/html/draft-ietf-oauth-v2-10",
			false, null, null,
			"http://www.example.com/oauth/authorize",
			"http://www.example.com/oauth/access_token", Format.TEXT_PLAIN, HttpVerb.GET
	),
	;
	
	private final String accessTokenEndpoint; 
	
	private final Format accessTokenFormat; 
	
	private final HttpVerb accessTokenVerb;
	
	private final String altName;
	
	private final String authorizationEndpoint; 
	
	private final String documentationEndpoint;
	
	private final String preferredName;
	
	private final String requestTokenEndpoint;
	
	private final boolean requestTokenRequired;
	
	private final HttpVerb requestTokenVerb;
	
	private final String version;
	
	/**
	 * Sole constructor.
	 * 
	 * @param version  the version identifier for this implementation.
	 * @param preferredName  the preferred name for this implementation.
	 * @param altName  the alternative name for this implementation.
	 * @param documentationEndpoint  the HTTP-URL for a resource that provides documentation for this implementation.
	 * @param requestTokenRequired
	 * @param requestTokenEndpoint
	 * @param requestTokenVerb
	 * @param authorizationEndpoint
	 * @param accessTokenEndpoint
	 * @param accessTokenFormat
	 * @param accessTokenVerb
	 */
	ApiDescImpl(
			final String version, final String preferredName, final String altName, final String documentationEndpoint,
			final boolean requestTokenRequired, final String requestTokenEndpoint, final HttpVerb requestTokenVerb, 
			final String authorizationEndpoint, 
			final String accessTokenEndpoint, final Format accessTokenFormat, final HttpVerb accessTokenVerb
	) {
		this.version = version;
		this.preferredName = preferredName;
		this.altName = altName;
		this.documentationEndpoint = documentationEndpoint;
		this.requestTokenEndpoint = requestTokenEndpoint;
		this.requestTokenRequired = requestTokenRequired;
		this.requestTokenVerb = requestTokenVerb;
		this.authorizationEndpoint = authorizationEndpoint;
		this.accessTokenEndpoint = accessTokenEndpoint;
		this.accessTokenFormat = accessTokenFormat;
		this.accessTokenVerb = accessTokenVerb;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return accessTokenEndpoint;
	}

	@Override
	public Format getAccessTokenFormat() {
		return accessTokenFormat;
	}

	@Override
	public HttpVerb getAccessTokenVerb() {
		return accessTokenVerb;
	}

	/**
	 * Returns the alternative name for this implementation.
	 * 
	 * @return  the alternative name for this implementation.
	 */
	public String getAltName() {
		return altName;
	}

	@Override
	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	/**
	 * Returns the HTTP-URL for a resource that provides documentation for this implementation. 
	 * 
	 * @return the HTTP-URL for a resource that provides documentation for this implementation. 
	 */
	public String getDocumentationEndpoint() {
		return documentationEndpoint;
	}

	/**
	 * Returns the preferred name for this implementation.
	 * 
	 * @return  the preferred name for this implementation.
	 */
	public String getPreferredName() {
		return preferredName;
	}

	@Override
	public String getRequestTokenEndpoint() {
		return requestTokenEndpoint;
	}

	@Override
	public HttpVerb getRequestTokenVerb() {
		return requestTokenVerb;
	}

	/**
	 * Returns the version identifier for this implementation.
	 * 
	 * @return  the version identifier for this implementation.
	 */
	public String getVersion() {
		return version;
	}
	
	@Override
	public boolean isRequestTokenRequired() {
		return requestTokenRequired;
	}

	@Override
	public String toString() {
		return this.getPreferredName();
	}
	
}
