package uk.ac.manchester.cs.img.esc.ui.serviceprovider;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

import uk.ac.manchester.cs.img.esc.ConnectionUtil;

import com.connexience.server.api.API;
import com.connexience.server.api.APIConnectException;
import com.connexience.server.api.APIInstantiationException;
import com.connexience.server.api.APIParseException;
import com.connexience.server.api.APISecurityException;
import com.connexience.server.api.IWorkflow;

public class EscServiceProvider extends
	AbstractConfigurableServiceProvider<EscServiceProviderConfig> implements
	ConfigurableServiceProvider<EscServiceProviderConfig> {
	
	public EscServiceProvider() {
		super(new EscServiceProviderConfig());
	}

	private static final URI providerId = URI
		.create("http://example.com/2010/service-provider/esc-activity-ui");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {

		List<ServiceDescription> results = new ArrayList<ServiceDescription>();
		
		API api;
		
		try {
			api = ConnectionUtil.getAPI(getConfiguration().getServiceProviderURLString());

		for (IWorkflow w : api.listWorkflows()) {
			EscServiceDesc service = new EscServiceDesc();
			service.setId(w.getId());
			service.setName(w.getName());
			service.setUrl(getConfiguration().getServiceProviderURLString());

			service.setDescription(w.getDescription());
			results.add(service);
		}

		} catch (APIConnectException e) {
			callBack.fail("Unable to connect", e);
		} catch (MalformedURLException e) {
			callBack.fail("Malformed URL", e);
		} catch (APISecurityException e) {
			callBack.fail("Permissions issue at " + getConfiguration().getServiceProviderURLString(), e);
		} catch (APIParseException e) {
			callBack.fail("Communication format problem", e);

		} catch (APIInstantiationException e) {
			callBack.fail("API set up problem", e);

		} catch (CMException e) {
			callBack.fail("Credential management failure", e);
		}
		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
		callBack.partialResults(results);

		// No more results will be coming
		callBack.finished();
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return EscServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "eScience Central workflows";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList(getConfiguration().getServiceProviderURLString());
	}

}
