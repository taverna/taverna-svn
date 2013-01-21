package uk.ac.soton.mib104.t2.activities.oauth.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;
import uk.ac.soton.mib104.t2.activities.oauth.util.IconManager;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class OAuthServiceProvider implements ServiceDescriptionProvider {
	
	private static final URI providerId = URI.create("http://www.taverna.org.uk/2012/services/oauth");
	
	private static final String providerName = "OAuth";
	
	public static final List<String> getPath() {
		return Arrays.asList(String.format("%s services", providerName));
	}
	
	@Override
	public void findServiceDescriptionsAsync(final FindServiceDescriptionsCallBack callBack) {
		final List<ServiceDescription<?>> results = new LinkedList<ServiceDescription<?>>();
		
		for (final ApiDescImpl versionDesc : ApiDescImpl.values()) {
			results.add(OAuthServiceDesc.build(versionDesc));
		}
		
		results.add(new OAuth10aRESTServiceDesc());
		
		callBack.partialResults(results);

		callBack.finished();
	}

	@Override
	public Icon getIcon() {
		return IconManager.getSmallIcon();
	}

	@Override
	public String getId() {
		return providerId.toASCIIString();
	}
	
	@Override
	public String getName() {
		return providerName;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}

}
