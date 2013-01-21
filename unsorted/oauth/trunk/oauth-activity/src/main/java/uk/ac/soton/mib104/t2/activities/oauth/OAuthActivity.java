package uk.ac.soton.mib104.t2.activities.oauth;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import uk.ac.soton.mib104.t2.activities.oauth.util.AccessToken;
import uk.ac.soton.mib104.t2.activities.oauth.util.AccessTokenFactory;
import uk.ac.soton.mib104.t2.activities.oauth.util.OAuthActivityUtils;

/**
 * OAuthActivity is an asynchronous activity that obtains OAuth Access Tokens.  
 * 
 * @author Mark Borkum
 *
 * @see AccessToken
 * @see AccessTokenFactory#createAccessToken
 * @see OAuthActivityConfigurationBean
 * @see OAuthActivityHealthChecker
 */
public class OAuthActivity extends AbstractAsynchronousActivity<OAuthActivityConfigurationBean> implements AsynchronousActivity<OAuthActivityConfigurationBean> {
	
	/**
	 * The name for the "client_id" port.
	 */
	protected static final String CLIENT_ID_INPUT = "client_id";
	
	/**
	 * The name for the "client_secret" port.
	 */
	protected static final String CLIENT_SECRET_INPUT = "client_secret";
	
	/**
	 * The name for the "redirect_uri" port.
	 */
	protected static final String REDIRECT_URI_INPUT = "redirect_uri";
	
	/**
	 * The name for the "scope" port.
	 */
	protected static final String SCOPE_INPUT = "scope";
	
	/**
	 * The name for the "access_token" port.
	 */
	protected static final String ACCESS_TOKEN_OUTPUT = "access_token";
	
	/**
	 * The name for the "access_token_secret" port.
	 */
	protected static final String ACCESS_TOKEN_SECRET_OUTPUT = "access_token_secret";
	
	private OAuthActivityConfigurationBean configBean;
	
	@Override
	public void configure(final OAuthActivityConfigurationBean configBean) throws ActivityConfigurationException {
		if (configBean == null) {
			throw new ActivityConfigurationException(new NullPointerException("configBean"));
		}
		
		this.configBean = configBean;

		this.configurePorts();
		
		return;
	}
	
	protected void configurePorts() {
		this.removeInputs();
		this.removeOutputs();
		
		this.addInput(CLIENT_ID_INPUT, 0, true, null, String.class);
		this.addInput(CLIENT_SECRET_INPUT, 0, true, null, String.class);
		this.addInput(REDIRECT_URI_INPUT, 0, true, null, String.class);
		this.addInput(SCOPE_INPUT, 0, true, null, String.class);
		
		this.addOutput(ACCESS_TOKEN_OUTPUT, 0);
		this.addOutput(ACCESS_TOKEN_SECRET_OUTPUT, 0);
		
		return;
	}
	
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs, final AsynchronousActivityCallback callback) {
		if (inputs == null) {
			throw new NullPointerException("inputs");
		} else if (callback == null) {
			throw new NullPointerException("callback");
		}
		
		callback.requestRun(new Runnable() {
			
			@Override
			public void run() {
				final InvocationContext context = callback.getContext();
				
				final ReferenceService referenceService = context.getReferenceService();
				
				final String clientId         = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, CLIENT_ID_INPUT,     String.class, null),
							 clientSecret     = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, CLIENT_SECRET_INPUT, String.class, null),
							 redirectEndpoint = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, REDIRECT_URI_INPUT,  String.class, null),
							 scope            = OAuthActivityUtils.renderIdentifier(inputs, context, referenceService, SCOPE_INPUT,         String.class, null);
	
				if (clientId == null) {
					callback.fail(String.format("%s is undefined", CLIENT_ID_INPUT));
				} else if (clientSecret == null) {
					callback.fail(String.format("%s is undefined", CLIENT_SECRET_INPUT));
				} else {
					// Delegate all of the work to the AccessTokenFactory. 
					final AccessToken accessToken = AccessTokenFactory.createAccessToken(getConfiguration(), clientId, clientSecret, redirectEndpoint, scope);
					
					try {
						final Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
						
						if (accessToken.getAccessToken() == null) {
							callback.fail(String.format("%s is undefined", ACCESS_TOKEN_OUTPUT));
						} else if (accessToken.getAccessTokenSecret() == null) {
							callback.fail(String.format("%s is undefined", ACCESS_TOKEN_SECRET_OUTPUT));
						} else {
							outputs.put(ACCESS_TOKEN_OUTPUT, referenceService.register(accessToken.getAccessToken(), 0, true, context));
							outputs.put(ACCESS_TOKEN_SECRET_OUTPUT, referenceService.register(accessToken.getAccessTokenSecret(), 0, true, context));
							
							callback.receiveResult(outputs, new int[0]);
						}
					} catch (final ReferenceServiceException ex) {
						callback.fail(ex.getMessage(), ex);
					}
				}
			}
			
		});
		
		return;
	}
	
	@Override
	public OAuthActivityConfigurationBean getConfiguration() {
		return configBean;
	}

}
