package net.sf.taverna.t2.component.registry.myexperiment;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.ComponentRegistry;

public class OldComponentRegistryLocator {
	private OldComponentRegistryLocator() {
	}

	private static final Map<String, MyExperimentComponentRegistry> componentRegistries = new HashMap<String, MyExperimentComponentRegistry>();

	/**
	 * How to get an instance of the registry access class.
	 * 
	 * @param registryURL
	 *            The address of the service.
	 * @return An API access handle. May be shared.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public static synchronized ComponentRegistry getComponentRegistry(
			URL registryURL) throws RegistryException {
		if (!componentRegistries.containsKey(registryURL.toExternalForm())) {
			componentRegistries.put(registryURL.toExternalForm(),
					new MyExperimentComponentRegistry(registryURL));
		}
		return componentRegistries.get(registryURL.toExternalForm());
	}
}
