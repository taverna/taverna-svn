package uk.ac.soton.mib104.t2.activities.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.authrest.HTTPRequestHandlerAdapter;
import net.sf.taverna.t2.activities.authrest.RESTActivity;
import net.sf.taverna.t2.activities.authrest.RESTActivityConfigurationBean;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.http.client.methods.HttpRequestBase;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.SignatureType;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;
import org.springframework.util.StringUtils;

import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;
import uk.ac.soton.mib104.t2.activities.oauth.util.OAuthActivityUtils;

/**
 * OAuth10aRESTActivity is a RESTActivity with OAuth 1.0a support. 
 * 
 * @author Mark Borkum
 */
public class OAuth10aRESTActivity extends RESTActivity {
	
	/**
	 * OAuthRequestHttpUriRequestShim is a delegate for the [Scribe] OAuthRequest class.
	 * 
	 * The delegate is seeded with the HTTP-URL, query parameters and headers of the 
	 * RESTActivity, and is subsequently used to calculate the OAuth signature. 
	 * 
	 * @author Mark Borkum
	 */
	private static final class OAuthRequestHttpUriRequestShim extends OAuthRequest {
		
		/**
		 * Map of HTTP_METHOD objects to [Scribe] Verb objects. 
		 */
		private static Map<HTTP_METHOD, Verb> HTTP_METHOD_TO_VERB;
		
		/**
		 * Map of String objects to [Scribe] Verb objects.
		 */
		private static Map<String, Verb> STRING_TO_VERB;
		
		static {
			final Map<HTTP_METHOD, Verb> httpMethodToVerb = new HashMap<HTTP_METHOD, Verb>();
			
			httpMethodToVerb.put(HTTP_METHOD.GET, Verb.GET);
			httpMethodToVerb.put(HTTP_METHOD.POST, Verb.POST);
			httpMethodToVerb.put(HTTP_METHOD.PUT, Verb.PUT);
			httpMethodToVerb.put(HTTP_METHOD.DELETE, Verb.DELETE);
			
			HTTP_METHOD_TO_VERB = Collections.unmodifiableMap(httpMethodToVerb);
			
			final Map<String, Verb> stringToVerb = new HashMap<String, Verb>();
			
			stringToVerb.put("GET", Verb.GET);
			stringToVerb.put("POST", Verb.POST);
			stringToVerb.put("PUT", Verb.PUT);
			stringToVerb.put("DELETE", Verb.DELETE);
			
			STRING_TO_VERB = Collections.unmodifiableMap(stringToVerb);
		}
		
		/**
		 * Returns the URI associated with the specified HTTP request.  
		 * 
		 * @param httpRequest  the HTTP request. 
		 * @return  the URI associated with the specified HTTP request. 
		 * @throws NullPointerException  if <code>httpRequest</code> is undefined.
		 */
		private static final String getURI(final HttpRequestBase httpRequest) {
			if (httpRequest == null) {
				throw new NullPointerException("httpRequest");
			}
			
			return httpRequest.getURI().toASCIIString();
		}
		
		/**
		 * Returns the [Scribe] Verb that is denoted by the specified HTTP_METHOD object. 
		 * 
		 * @param httpMethod  the HTTP_METHOD object.
		 * @return  the [Scribe] Verb that is denoted by the specified HTTP_METHOD object. 
		 */
		private static final Verb getVerb(final HTTP_METHOD httpMethod) {
			return HTTP_METHOD_TO_VERB.get(httpMethod);
		}
		
		/**
		 * Returns the [Scribe] Verb that is denoted by the specified HTTP request.
		 * 
		 * @param httpRequest  the HTTP request. 
		 * @return  the [Scribe] Verb that is denoted by the specified HTTP request.
		 * @throws NullPointerException  if <code>httpRequest</code> is undefined. 
		 */
		private static final Verb getVerb(final HttpRequestBase httpRequest) {
			if (httpRequest == null) {
				throw new NullPointerException("httpRequest");
			}
			
			return getVerb(httpRequest.getMethod());
		}
		
		/**
		 * Returns the [Scribe] Verb that is denoted by the specified String object. 
		 * 
		 * @param string  the String object. 
		 * @return  the [Scribe] Verb that is denoted by the specified String object. 
		 */
		private static final Verb getVerb(final String string) {
			return STRING_TO_VERB.get(string);
		}
		
		private final Map<String, String> oauthParameters = new HashMap<String, String>();
		
		/**
		 * Default constructor. 
		 * 
		 * @param verb  the [Scribe] Verb object.
		 * @param uriref  the HTTP-URI reference. 
		 */
		public OAuthRequestHttpUriRequestShim(final Verb verb, final String uriref) {
			super(verb, uriref);
		}
		
		/**
		 * RESTActivity-compatible constructor. 
		 * 
		 * @param httpMethod  the HTTP_METHOD object.
		 * @param uriref  the HTTP-URI reference.
		 */
		public OAuthRequestHttpUriRequestShim(final HTTP_METHOD httpMethod, final String uriref) {
			this(getVerb(httpMethod), uriref);
		}
		
		/**
		 * Apache HttpClient-compatible constructor.  
		 * 
		 * @param httpRequest  the HTTP request. 
		 */
		public OAuthRequestHttpUriRequestShim(final HttpRequestBase httpRequest) {
			this(getVerb(httpRequest), getURI(httpRequest));
		}
		
		@Override
		public void addOAuthParameter(final String key, final String value) {
			oauthParameters.put(key, value);
			
			return;
		}

		@Override
		public Map<String, String> getOauthParameters() {
			return oauthParameters;
		}
		
	}
	
	private static final SignatureType DEFAULT_SIGNATURE_TYPE = SignatureType.Header;
	
	private static final String PREFIX = "oauth_";
	
	private static final String PREFIXED_CLIENT_ID_INPUT = String.format("%s%s", PREFIX, OAuthActivity.CLIENT_ID_INPUT);
	private static final String PREFIXED_CLIENT_SECRET_INPUT = String.format("%s%s", PREFIX, OAuthActivity.CLIENT_SECRET_INPUT);
//	private static final String PREFIXED_REDIRECT_URI_INPUT = String.format("%s%s", PREFIX, OAuthActivity.REDIRECT_URI_INPUT);
	private static final String PREFIXED_SCOPE_INPUT = String.format("%s%s", PREFIX, OAuthActivity.SCOPE_INPUT);
	
	private static final String PREFIXED_ACCESS_TOKEN_OUTPUT = String.format("%s%s", PREFIX, OAuthActivity.ACCESS_TOKEN_OUTPUT);
	private static final String PREFIXED_ACCESS_TOKEN_SECRET_OUTPUT = String.format("%s%s", PREFIX, OAuthActivity.ACCESS_TOKEN_SECRET_OUTPUT);
	
	@Override
	protected void configurePorts() {
		super.configurePorts();

		this.addInput(PREFIXED_CLIENT_ID_INPUT, 0, true, null, String.class);
		this.addInput(PREFIXED_CLIENT_SECRET_INPUT, 0, true, null, String.class);
//		this.addInput(PREFIXED_REDIRECT_URI_INPUT, 0, true, null, String.class);
		this.addInput(PREFIXED_SCOPE_INPUT, 0, true, null, String.class);
		this.addInput(PREFIXED_ACCESS_TOKEN_OUTPUT, 0, true, null, String.class);
		this.addInput(PREFIXED_ACCESS_TOKEN_SECRET_OUTPUT, 0, true, null, String.class);
		
		final RESTActivityConfigurationBean configBean = this.getConfiguration();
		
		if (configBean != null) {
			final Map<String, Class<?>> activityInputs = configBean.getActivityInputs();
			
			if (activityInputs != null) {
				activityInputs.put(PREFIXED_CLIENT_ID_INPUT, String.class);
				activityInputs.put(PREFIXED_CLIENT_SECRET_INPUT, String.class);
//				activityInputs.put(PREFIXED_REDIRECT_URI_INPUT, String.class);
				activityInputs.put(PREFIXED_SCOPE_INPUT, String.class);
				activityInputs.put(PREFIXED_ACCESS_TOKEN_OUTPUT, String.class);
				activityInputs.put(PREFIXED_ACCESS_TOKEN_SECRET_OUTPUT, String.class);
			}
		}
		
		return;
	}

	@Override
	protected HTTPRequestHandlerAdapter createHTTPRequestHandlerAdapter(final Map<String, T2Reference> inputs, final AsynchronousActivityCallback callback) {
		if (inputs == null) {
			throw new NullPointerException("inputs");
		} else if (callback == null) {
			throw new NullPointerException("callback");
		}
		
		final InvocationContext context = callback.getContext();
		
		final ReferenceService referenceService = context.getReferenceService();
		
		final String clientId          = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_CLIENT_ID_INPUT, String.class, null),
        			 clientSecret      = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_CLIENT_SECRET_INPUT, String.class, null),
//        			 redirectEndpoint  = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_REDIRECT_URI_INPUT, String.class, null),
        			 scope			   = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_SCOPE_INPUT, String.class, null),
        			 accessToken       = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_ACCESS_TOKEN_OUTPUT, String.class, null),
        			 accessTokenSecret = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, PREFIXED_ACCESS_TOKEN_SECRET_OUTPUT, String.class, null);

		if (clientId == null) {
			callback.fail(String.format("%s is undefined", PREFIXED_CLIENT_ID_INPUT));
		} else if (clientSecret == null) {
			callback.fail(String.format("%s is undefined", PREFIXED_CLIENT_SECRET_INPUT));
		} else if (accessToken == null) {
			callback.fail(String.format("%s is undefined", PREFIXED_ACCESS_TOKEN_OUTPUT));
		} else if (accessTokenSecret == null) {
			callback.fail(String.format("%s is undefined", PREFIXED_ACCESS_TOKEN_SECRET_OUTPUT));
		} else {
			final DefaultApi10a api = OAuthActivityUtils.getDefaultApi10a();
			
			return new HTTPRequestHandlerAdapter() {
				
				/**
				 * Constructs a new OAuthRequestHttpUriRequestShim object using the specified HTTP request. 
				 * 
				 * @param httpRequest  the HTTP request. 
				 * @return  a new OAuthRequestHttpUriRequestShim object. 
				 */
				private OAuthRequestHttpUriRequestShim createShim(final HttpRequestBase httpRequest) {
					final OAuthRequestHttpUriRequestShim shim = new OAuthRequestHttpUriRequestShim(httpRequest);
					
					this.doShim(shim);
					
					return shim;
				}
				
				/**
				 * Constructs a new OAuthRequestHttpUriRequestShim object using the specified HTTP-URL. 
				 * 
				 * @param httpRequestUrl  the HTTP-URL. 
				 * @return  a new OAuthRequestHttpUriRequestShim object. 
				 */
				private OAuthRequestHttpUriRequestShim createShim(final String httpRequestUrl) {
					final OAuthRequestHttpUriRequestShim shim = new OAuthRequestHttpUriRequestShim(getConfiguration().getHttpMethod(), httpRequestUrl);
					
					this.doShim(shim);
					
					return shim;
				}
				
				/**
				 * Adds the OAuth 1.0a parameters to the "shim" as HTTP query parameters. 
				 * 
				 * @param shim  the OAuthRequestHttpUriRequestShim object. 
				 */
				private void doShim(final OAuthRequestHttpUriRequestShim shim) {
					shim.addOAuthParameter(OAuthConstants.TOKEN, accessToken);
					shim.addOAuthParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
					shim.addOAuthParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
					shim.addOAuthParameter(OAuthConstants.CONSUMER_KEY, clientId);
					shim.addOAuthParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
					shim.addOAuthParameter(OAuthConstants.VERSION, ApiDescImpl.OAuth10a.getVersion());
					
					if (StringUtils.hasText(scope)) {
				    	shim.addOAuthParameter(OAuthConstants.SCOPE, scope);
				    }
				    
					shim.addOAuthParameter(OAuthConstants.SIGNATURE, api.getSignatureService().getSignature(api.getBaseStringExtractor().extract(shim), clientSecret, accessTokenSecret));
					
					return;
				}

				@Override
				public void finalize(final HttpRequestBase httpRequest) {
					if (SignatureType.Header.equals(DEFAULT_SIGNATURE_TYPE)) {
						final OAuthRequestHttpUriRequestShim shim = this.createShim(httpRequest);
						
						// Condense the OAuth 1.0a parameters into a single HTTP header. 
						httpRequest.addHeader(OAuthConstants.HEADER, api.getHeaderExtractor().extract(shim));
					}
					
					return;
				}
				
				@Override
				public String modify(final String httpRequestUrl) {
					if (SignatureType.QueryString.equals(DEFAULT_SIGNATURE_TYPE)) {
						final OAuthRequestHttpUriRequestShim shim = this.createShim(httpRequestUrl);
						
						final Map<String, String> encodedQueryParameters = new HashMap<String, String>();
						
						for (final Map.Entry<String, String> entry : shim.getOauthParameters().entrySet()) {
							encodedQueryParameters.put(entry.getKey(), OAuthEncoder.encode(entry.getValue()));
						}
						
						// Add the OAuth 1.0a parameters to the original HTTP-URL as query parameters.
						return OAuthActivityUtils.addQueryParameters(httpRequestUrl, encodedQueryParameters);
					}
					
					return httpRequestUrl;
				}
				
			};
		}
		
		return null;
	}

}
