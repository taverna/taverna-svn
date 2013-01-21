package uk.ac.soton.mib104.t2.activities.oauth;

import java.io.Serializable;

import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDesc;
import uk.ac.soton.mib104.t2.activities.oauth.util.MutableApiDesc;
import uk.ac.soton.mib104.t2.activities.oauth.util.HttpVerb;
import uk.ac.soton.mib104.t2.activities.oauth.util.Format;

/**
 * OAuthActivityConfigurationBean is a bean. 
 * 
 * An OAuthActivityConfigurationBean instance encapsulates the state information
 * of an OAuthActivity. 
 * 
 * @author Mark Borkum
 * 
 * @see OAuthActivity
 * @see OAuthActivityHealthChecker
 */
public class OAuthActivityConfigurationBean implements MutableApiDesc, Serializable {
	
	private static final long serialVersionUID = -9033536430818381968L;
	
	private String accessTokenEndpoint;
	
	private Format accessTokenFormat;
	
	private HttpVerb accessTokenVerb;
	
	private String authorizationEndpoint;

	private String requestTokenEndpoint;

	private boolean requestTokenRequired;
	
	private HttpVerb requestTokenVerb;

	/**
	 * Default constructor.
	 */
	public OAuthActivityConfigurationBean() {
		this(null);
	}

	/**
	 * Utility constructor. 
	 * 
	 * @param apiDesc  the template Api descriptor. 
	 */
	public OAuthActivityConfigurationBean(final ApiDesc apiDesc) {
		super();
		
		this.set(apiDesc);
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

	@Override
	public String getAuthorizationEndpoint() {
		return authorizationEndpoint;
	}

	@Override
	public String getRequestTokenEndpoint() {
		return requestTokenEndpoint;
	}

	@Override
	public HttpVerb getRequestTokenVerb() {
		return requestTokenVerb;
	}

	@Override
	public boolean isRequestTokenRequired() {
		return requestTokenRequired;
	}

	@Override
	public void set(final ApiDesc apiDesc) {
		if (apiDesc != null) {
			this.setRequestTokenEndpoint(apiDesc.getRequestTokenEndpoint());
			this.setRequestTokenRequired(apiDesc.isRequestTokenRequired());
			this.setRequestTokenVerb(apiDesc.getRequestTokenVerb());
			this.setAuthorizationEndpoint(apiDesc.getAuthorizationEndpoint());
			this.setAccessTokenEndpoint(apiDesc.getAccessTokenEndpoint());
			this.setAccessTokenFormat(apiDesc.getAccessTokenFormat());
			this.setAccessTokenVerb(apiDesc.getAccessTokenVerb());
		}
		
		return;
	}

	@Override
	public void setAccessTokenEndpoint(final String accessTokenEndpoint) {
		this.accessTokenEndpoint = accessTokenEndpoint;
	}

	@Override
	public void setAccessTokenFormat(final Format accessTokenFormat) {
		this.accessTokenFormat = accessTokenFormat;
	}

	@Override
	public void setAccessTokenVerb(final HttpVerb accessTokenVerb) {
		this.accessTokenVerb = accessTokenVerb;
	}

	@Override
	public void setAuthorizationEndpoint(final String authorizationEndpoint) {
		this.authorizationEndpoint = authorizationEndpoint;
	}

	@Override
	public void setRequestTokenEndpoint(final String requestTokenEndpoint) {
		this.requestTokenEndpoint = requestTokenEndpoint;
	}

	@Override
	public void setRequestTokenRequired(final boolean requestTokenRequired) {
		this.requestTokenRequired = requestTokenRequired;
	}

	@Override
	public void setRequestTokenVerb(final HttpVerb requestTokenVerb) {
		this.requestTokenVerb = requestTokenVerb;
	}

}
