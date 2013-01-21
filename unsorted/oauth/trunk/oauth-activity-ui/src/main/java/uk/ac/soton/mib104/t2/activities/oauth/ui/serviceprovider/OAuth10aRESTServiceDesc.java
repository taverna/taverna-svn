package uk.ac.soton.mib104.t2.activities.oauth.ui.serviceprovider;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import uk.ac.soton.mib104.t2.activities.oauth.OAuth10aRESTActivity;
import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;
import uk.ac.soton.mib104.t2.activities.oauth.util.IconManager;

import net.sf.taverna.t2.activities.authrest.RESTActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class OAuth10aRESTServiceDesc extends ServiceDescription<RESTActivityConfigurationBean> {

	@Override
	public Class<? extends Activity<RESTActivityConfigurationBean>> getActivityClass() {
		return OAuth10aRESTActivity.class;
	}
	
	@Override
	public RESTActivityConfigurationBean getActivityConfiguration() {
		return RESTActivityConfigurationBean.getDefaultInstance();
	}

	@Override
	public String getDescription() {
		return String.format("A generic REST service (with %s authorization) that can handle all HTTP methods", ApiDescImpl.OAuth10a.getPreferredName());
	}

	@Override
	public Icon getIcon() {
		return IconManager.getSmallRESTIcon();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList(ApiDescImpl.OAuth10a);
	}

	@Override
	public String getName() {
		return String.format("REST + %s Service", ApiDescImpl.OAuth10a.getPreferredName());
	}

	@Override
	public List<String> getPath() {
		return OAuthServiceProvider.getPath();
	}

}
