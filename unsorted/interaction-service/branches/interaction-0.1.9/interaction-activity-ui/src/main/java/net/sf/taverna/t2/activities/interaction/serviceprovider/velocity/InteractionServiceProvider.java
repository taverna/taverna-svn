package net.sf.taverna.t2.activities.interaction.serviceprovider.velocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;


import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.serviceprovider.InteractionServiceIcon;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class InteractionServiceProvider implements ServiceDescriptionProvider {
	
	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/myfancytool-taverna");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

		List<ServiceDescription> results = new ArrayList<ServiceDescription>();
		
		InteractionVelocity.checkVelocity();
		for (String templateName : InteractionVelocity.getTemplateNames()) {
			InteractionServiceDesc service = new InteractionServiceDesc();
			// Populate the service description bean
			service.setTemplateName(templateName);
			results.add(service);
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
		return InteractionServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "My example service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

}
