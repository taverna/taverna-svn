package uk.ac.soton.mib104.t2.activities.oauth.ui.serviceprovider;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.springframework.util.StringUtils;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;
import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivityConfigurationBean;
import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDesc;
import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;
import uk.ac.soton.mib104.t2.activities.oauth.util.IconManager;
import uk.ac.soton.mib104.t2.activities.oauth.util.MutableApiDesc;
import uk.ac.soton.mib104.t2.activities.oauth.util.HttpVerb;
import uk.ac.soton.mib104.t2.activities.oauth.util.Format;

public class OAuthServiceDesc extends ServiceDescription<OAuthActivityConfigurationBean> implements MutableApiDesc {

	public static final OAuthServiceDesc build(final ApiDescImpl apiDescImpl) throws NullPointerException {
		if (apiDescImpl == null) {
			throw new NullPointerException("apiDescImpl");
		}
		
		final OAuthServiceDesc serviceDesc = new OAuthServiceDesc(apiDescImpl);
		
		serviceDesc.setName(String.format("%s Access Token", apiDescImpl.getPreferredName()));
		serviceDesc.setDescription(String.format("A service that obtains %s Access Tokens", apiDescImpl.getPreferredName()));
		
		return serviceDesc;
	}

	public OAuthServiceDesc() {
		this(null);
	}
	
	public OAuthServiceDesc(final ApiDesc apiDesc) {
		super();
		
		this.set(apiDesc);
	}

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<OAuthActivityConfigurationBean>> getActivityClass() {
		return OAuthActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an end-point URL or method name. 
	 */
	@Override
	public OAuthActivityConfigurationBean getActivityConfiguration() {
		return new OAuthActivityConfigurationBean(this);
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return IconManager.getSmallIcon();
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.<Object>asList(requestTokenEndpoint, requestTokenVerb, authorizationEndpoint, accessTokenEndpoint, accessTokenFormat, accessTokenVerb);
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		return OAuthServiceProvider.getPath();
	}
	
	// === Name === //
	
	private String name = null;
	
	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) throws IllegalArgumentException, NullPointerException {
		if (name == null) {
			throw new NullPointerException("name");
		} else if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("StringUtils.hasText(name) == false");
		}
		
		this.name = name;
	}
	
	// === Attributes === //
	
	private String accessTokenEndpoint;
	
	private Format accessTokenFormat;
	
	private HttpVerb accessTokenVerb;
	
	private String authorizationEndpoint;

	private String requestTokenEndpoint;
	
	private boolean requestTokenRequired;

	private HttpVerb requestTokenVerb;
	
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
