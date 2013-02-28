/**
 * 
 */
package net.sf.taverna.t2.component.ui.util;

import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProvider;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class Utils {
	
	private static FileManager fileManager = FileManager.getInstance();
	
	// From http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-in-java
	public static String URL_PATTERN = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	private static ServiceDescriptionRegistry registry = ServiceDescriptionRegistryImpl.getInstance();

	public static final String LONG_STRING = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

	public static final String SHORT_STRING = "XXX";

	public static void refreshComponentServiceProvider(ComponentServiceProviderConfig config)
			throws ConfigurationException {
		ComponentServiceProvider provider = new ComponentServiceProvider();
		provider.configure(config);
		registry.removeServiceDescriptionProvider(provider);
		registry.addServiceDescriptionProvider(provider);
	}
	
	public static void removeComponentServiceProvider(
			ComponentServiceProviderConfig config)
			throws ConfigurationException {
		ComponentServiceProvider provider = new ComponentServiceProvider();
		provider.configure(config);
		registry.removeServiceDescriptionProvider(provider);
	}

	public static boolean dataflowIsComponent(Dataflow d) {
		if (d == null) {
			return false;
		}
		Object dataflowSource = fileManager.getDataflowSource(d);
		return dataflowSource instanceof ComponentVersionIdentification;
	}

	public static boolean currentDataflowIsComponent() {
		return dataflowIsComponent(fileManager.getCurrentDataflow());
	}
}
