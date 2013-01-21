package uk.ac.soton.mib104.t2.activities.oauth.ui.view;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;
import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivityConfigurationBean;
import uk.ac.soton.mib104.t2.activities.oauth.ui.config.OAuthConfigurationPanel;
import uk.ac.soton.mib104.t2.activities.oauth.ui.config.OAuthConfigureAction;
import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;
import uk.ac.soton.mib104.t2.activities.oauth.util.HttpVerb;
import uk.ac.soton.mib104.t2.activities.oauth.util.Format;

@SuppressWarnings("serial")
public class OAuthContextualView extends ContextualView {
	
	private final OAuthActivity activity;
	
	private final JTextField fieldAccessTokenEndpoint = new JTextField();
	
	private final JComboBox fieldAccessTokenFormat = new JComboBox(Format.values());
	
	private final JComboBox fieldAccessTokenVerb = new JComboBox(HttpVerb.values());
	
	private final JTextField fieldAuthorizationEndpoint = new JTextField();
	
	private final JTextField fieldRequestTokenEndpoint = new JTextField();
	
	private final JComboBox fieldRequestTokenVerb = new JComboBox(HttpVerb.values());
	
	{
		fieldRequestTokenEndpoint.setEditable(false);
		fieldRequestTokenVerb.setEditable(false);
		fieldAuthorizationEndpoint.setEditable(false);
		fieldAccessTokenEndpoint.setEditable(false);
		fieldAccessTokenFormat.setEditable(false);
		fieldAccessTokenVerb.setEditable(false);
		
		fieldRequestTokenEndpoint.setEnabled(false);
		fieldRequestTokenVerb.setEnabled(false);
		fieldAuthorizationEndpoint.setEnabled(false);
		fieldAccessTokenEndpoint.setEnabled(false);
		fieldAccessTokenFormat.setEnabled(false);
		fieldAccessTokenVerb.setEnabled(false);
	}

	public OAuthContextualView(final OAuthActivity activity) {
		this.activity = activity;
		
		this.initView();
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return new OAuthConfigureAction(activity, owner);
	}

	@Override
	public JComponent getMainFrame() {
		final OAuthActivityConfigurationBean configBean = activity.getConfiguration();
		
		final JPanel contentPane = new JPanel();
		
		contentPane.setLayout(new BorderLayout());
		
		contentPane.add(OAuthConfigurationPanel.createPane(configBean, fieldRequestTokenEndpoint, fieldRequestTokenVerb, fieldAuthorizationEndpoint, fieldAccessTokenEndpoint, fieldAccessTokenFormat, fieldAccessTokenVerb), BorderLayout.CENTER);
		
		this.refreshView();
		
		return contentPane;
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public String getViewTitle() {
		final OAuthActivityConfigurationBean configBean = activity.getConfiguration();
		
		final ApiDescImpl apiDesc = configBean.isRequestTokenRequired() ? ApiDescImpl.OAuth10a : ApiDescImpl.OAuth20_draft10;
		
		return String.format("%s Access Token service", apiDesc.getPreferredName());
	}
	
	@Override
	public void refreshView() {
		final OAuthActivityConfigurationBean configBean = activity.getConfiguration();

		fieldRequestTokenEndpoint.setText(configBean.getRequestTokenEndpoint());
		fieldRequestTokenVerb.setSelectedItem(configBean.getRequestTokenVerb());
		fieldAuthorizationEndpoint.setText(configBean.getAuthorizationEndpoint());
		fieldAccessTokenEndpoint.setText(configBean.getAccessTokenEndpoint());
		fieldAccessTokenFormat.setSelectedItem(configBean.getAccessTokenFormat());
		fieldAccessTokenVerb.setSelectedItem(configBean.getAccessTokenVerb());
		
		return;
	}

}
