package net.sf.taverna.t2.activities.interaction.serviceprovider.velocity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.serviceprovider.InteractionServiceIcon;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class InteractionServiceProvider implements ServiceDescriptionProvider {

	private static final URI providerId = URI
			.create("http://taverna.sf.net/2010/service-provider/interaction");

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@Override
	public void findServiceDescriptionsAsync(
			final FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

		final List<ServiceDescription<InteractionActivityConfigurationBean>> results = new ArrayList<ServiceDescription<InteractionActivityConfigurationBean>>();

		InteractionVelocity.checkVelocity();
		for (final String templateName : InteractionVelocity.getTemplateNames()) {
			final InteractionServiceDesc service = new InteractionServiceDesc();
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
	@Override
	public Icon getIcon() {
		return InteractionServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	@Override
	public String getName() {
		return "Interaction service";
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public String getId() {
		return providerId.toASCIIString();
	}

}
