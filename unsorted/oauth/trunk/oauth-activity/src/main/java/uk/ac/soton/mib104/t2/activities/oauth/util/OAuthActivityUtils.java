package uk.ac.soton.mib104.t2.activities.oauth.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;
import org.springframework.util.StringUtils;

/**
 * OAuthActivityUtils is a utility class.
 * 
 * @author Mark Borkum
 */
public final class OAuthActivityUtils {
	
	private static DefaultApi10a DEFAULT_API_OAuth10a;
	
	private static DefaultApi20 DEFAULT_API_OAuth20_draft10;
	
	/**
	 * Returns the specified HTTP-URL with the key-value pair appended. 
	 * 
	 * This method does not perform any encoding.  Values should be encoded before this method is called. 
	 * 
	 * @param uriref  the HTTP-URL. 
	 * @param key  the key.
	 * @param value  the value. 
	 * @return  the specified HTTP-URL with the key-value pair appended. 
	 */
	public static final String addQueryParameter(final String uriref, final String key, final String value) {
		// Determine the indices of the query and anchor (or "ref") separators.
	    final int queryIdx = uriref.indexOf('?'), refIdx = uriref.indexOf('#');
	    
	    // Construct the next segment of the HTTP-URL (the key-value pair with either a "?" or "&" prefix -- depending on whether or not another key-value pair is present.)
	    final String segment = String.format("%s%s%s%s", (queryIdx == -1) ? '?' : '&', key, '=', value);
	    
	    if (refIdx == -1) {
	    	// If the specified HTTP-URL does not have an anchor components, then return a concatenation of the old URI reference and the new segment.  
	    	return String.format("%s%s", uriref, segment); 
	    } else {
	    	// Otherwise, insert the new segment in between the query and anchor components. 
	    	return String.format("%s%s%s", uriref.substring(0, refIdx), segment, uriref.substring(refIdx));
	    }
	}
	
	/**
	 * Returns the specified HTTP-URL with the key-value pairs appended. 
	 * 
	 * @param uriref  the HTTP-URL.
	 * @param queryParameters  the key-value pairs.
	 * @return  the specified HTTP-URL with the key-value pairs appended. 
	 */
	public static final String addQueryParameters(final String uriref, final Map<String, String> queryParameters) {
		// Create an accumulator.
		String result = new String(uriref);
		
		// Iterate over the entries in the map; and, add each key-value pair to the accumulator.
		for (final Map.Entry<String, String> entry : queryParameters.entrySet()) {
			result = addQueryParameter(result, entry.getKey(), entry.getValue());
		}
		
		// Return the accumulator.
		return result;
	}
	
	/**
	 * Constructs a new [Scribe] Api object using the specified ApiDesc object as a template. 
	 * 
	 * @param apiDesc  the ApiDesc object. 
	 * @return  a new Scribe API descriptor. 
	 * @throws NullPointerException  if <code>apiDesc</code> is undefined.
	 */
	public static final Api createApi(final ApiDesc apiDesc) {
		if (apiDesc == null) {
			throw new NullPointerException("apiDesc");
		}
		
		// If a request token is required, then we are dealing with OAuth 1.x
		if (apiDesc.isRequestTokenRequired()) {
			return new DefaultApi10a() {

				@Override
				public String getAccessTokenEndpoint() {
					return apiDesc.getAccessTokenEndpoint();
				}

				@Override
				public AccessTokenExtractor getAccessTokenExtractor() {
					if (Format.APPLICATION_JSON.equals(apiDesc.getAccessTokenFormat())) {
						return new JsonTokenExtractor();
					} else {
						return super.getAccessTokenExtractor();
					}
				}

				@Override
				public Verb getAccessTokenVerb() {
					return getVerb(apiDesc.getAccessTokenVerb());
				}

				@Override
				public String getAuthorizationUrl(final Token requestToken) {
					final String authorizationEndpoint = apiDesc.getAuthorizationEndpoint();
					
					if (authorizationEndpoint == null) {
						return null;
					}
					
					// Add the "oauth_token" query parameter to the User Authorization URL.
					return addQueryParameter(authorizationEndpoint, OAuthConstants.TOKEN, OAuthEncoder.encode(requestToken.getToken()));
				}

				@Override
				public String getRequestTokenEndpoint() {
					return apiDesc.getRequestTokenEndpoint();
				}

				@Override
				public Verb getRequestTokenVerb() {
					return getVerb(apiDesc.getRequestTokenVerb());
				}
				
			};
		} else {
			return new DefaultApi20() {

				@Override
				public String getAccessTokenEndpoint() {
					return apiDesc.getAccessTokenEndpoint();
				}

				@Override
				public AccessTokenExtractor getAccessTokenExtractor() {
					if (Format.APPLICATION_JSON.equals(apiDesc.getAccessTokenFormat())) {
						return new JsonTokenExtractor();
					} else {
						return super.getAccessTokenExtractor();
					}
				}

				@Override
				public Verb getAccessTokenVerb() {
					return getVerb(apiDesc.getAccessTokenVerb());
				}

				@Override
				public String getAuthorizationUrl(final OAuthConfig config) {
					final String authorizationEndpoint = apiDesc.getAuthorizationEndpoint();
					
					if (authorizationEndpoint == null) {
						return null;
					}
					
					// Create a map to store multiple key-value pairs.
					final Map<String, String> queryParameters = new HashMap<String, String>();
					
					// Set the "response_type" query parameter to "code".
					queryParameters.put("response_type", OAuthConstants.CODE);
					
					// Add the "client_id" query parameter.
					queryParameters.put(OAuthConstants.CLIENT_ID, OAuthEncoder.encode(config.getApiKey()));
					
					// Add the "redirect_uri" query parameter.
					queryParameters.put(OAuthConstants.REDIRECT_URI, OAuthEncoder.encode(config.getCallback()));
					
					if (config.hasScope()) {
						// Add the "scope" query parameter (optional).
						queryParameters.put(OAuthConstants.SCOPE, OAuthEncoder.encode(config.getScope()));
					}
					
					return addQueryParameters(authorizationEndpoint, queryParameters);
				}
				
			};
		}
	}
	
	/**
	 * Constructs a new [Scribe] ServiceBuilder object.
	 * 
	 * @param api  the Scribe Api object. 
	 * @param clientId  the OAuth client identifier.
	 * @param clientSecret  the OAuth client secret.
	 * @param redirectEndpoint  the OAuth callback URL. 
	 * @param scope  the OAuth scope.
	 * @return  a new ServiceBuilder object.
	 * @throws NullPointerException  if <code>api</code> is undefined.
	 */
	public static final ServiceBuilder createServiceBuilder(final Api api, final String clientId, final String clientSecret, final String redirectEndpoint, final String scope) {
		if (api == null) {
			throw new NullPointerException("api");
		}
		
		final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(api);
		
		final ServiceBuilder withClientId         = StringUtils.hasText(clientId)         ? serviceBuilder.apiKey(clientId)             : serviceBuilder;
		final ServiceBuilder withClientSecret     = StringUtils.hasText(clientSecret)     ? withClientId.apiSecret(clientSecret)        : withClientId;
		final ServiceBuilder withRedirectEndpoint = StringUtils.hasText(redirectEndpoint) ? withClientSecret.callback(redirectEndpoint) : withClientSecret;
		final ServiceBuilder withScope            = StringUtils.hasText(scope)            ? withRedirectEndpoint.scope(scope)           : withRedirectEndpoint;
		
		return withScope;
	}
	
	/**
	 * Returns the default [Scribe] Api object for OAuth 1.0a.
	 * 
	 * @return  the default [Scribe] Api object for OAuth 1.0a.
	 */
	public static final DefaultApi10a getDefaultApi10a() {
		if (DEFAULT_API_OAuth10a == null) {
			DEFAULT_API_OAuth10a = (DefaultApi10a) createApi(ApiDescImpl.OAuth10a);
		}
		
		return DEFAULT_API_OAuth10a;
	}
	
	/**
	 * Returns the default [Scribe] Api object for OAuth 2.0.
	 * 
	 * @return  the default [Scribe] Api object for OAuth 2.0.
	 */
	public static final DefaultApi20 getDefaultApi20() {
		if (DEFAULT_API_OAuth20_draft10 == null) {
			DEFAULT_API_OAuth20_draft10 = (DefaultApi20) createApi(ApiDescImpl.OAuth20_draft10);
		}
		
		return DEFAULT_API_OAuth20_draft10;
	}
	
	/**
	 * Returns the [Scribe] Verb object that corresponds to the specified HttpVerb object. 
	 * 
	 * @param verb  the HTTP verb. 
	 * @return  the corresponding [Scribe] Verb object. 
	 * @throws NullPointerException  if <code>verb</code> is undefined.
	 * @throws IllegalArgumentException  if <code>verb</code> is invalid.
	 */
	private static final Verb getVerb(final HttpVerb verb) {
		if (verb == null) {
			throw new NullPointerException("verb");
		}
		
		switch (verb) {
		case GET:
			return Verb.GET;
		case POST:
			return Verb.POST;
		default:
			throw new IllegalArgumentException("verb");
		}
	}
	
	/**
	 * A safe version of the "renderIdentifier" method, which returns a default value instead of throwing an exception. 
	 * 
	 * @param <T>  the type of the port. 
	 * @param inputs  the inputs. 
	 * @param context  the context. 
	 * @param referenceService  the T2 reference service. 
	 * @param portName  the name of the port.
	 * @param klass  the class of the port. 
	 * @param defaultValue  the default value for the port. 
	 * @return  the "rendered" value of the specified port, or the default value. 
	 * @throws NullPointerException  if any parameters are undefined. 
	 * @see ReferenceService#renderIdentifier
	 */
	public static final <T> T renderIdentifier(final Map<String, T2Reference> inputs, final InvocationContext context, final ReferenceService referenceService, final String portName, final Class<T> klass, final T defaultValue) {
		if (inputs == null) {
			throw new NullPointerException("inputs");
		} else if (context == null) {
			throw new NullPointerException("context");
		} else if (referenceService == null) {
			throw new NullPointerException("referenceService");
		} else if (portName == null) {
			throw new NullPointerException("portName");
		} else if (klass == null) {
			throw new NullPointerException("klass");
		}
		
		if (inputs.containsKey(portName)) {
			final T2Reference ref = inputs.get(portName);
			
			if (ref != null) {
				try {
					@SuppressWarnings("unchecked")
					final T result = (T) referenceService.renderIdentifier(ref, klass, context);
					
					return result;
				} catch (final ReferenceServiceException ex) {
					// Do nothing...
				}
			}
		}
		
		return defaultValue;
	}
	
	/**
	 * Sole constructor.
	 */
	private OAuthActivityUtils() {
		super();
	}

}
