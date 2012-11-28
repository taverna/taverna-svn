/**
 * 
 */
package net.sf.taverna.t2.component.ui.util;

import net.sf.taverna.t2.component.ui.menu.ComponentLocationChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProvider;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

/**
 * @author alanrw
 *
 */
public class Utils {

	public static void refreshComponentServiceProvider(ComponentServiceProviderConfig config)
			throws ConfigurationException {
		ComponentServiceProvider provider = new ComponentServiceProvider();
		provider.configure(config);
		ServiceDescriptionRegistry registry = ServiceDescriptionRegistryImpl.getInstance();
		registry.removeServiceDescriptionProvider(provider);
		registry.addServiceDescriptionProvider(provider);
	}

}
