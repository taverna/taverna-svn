package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistry.jaxbContext;
import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistry.logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.ComponentRegistry;

public class NewComponentRegistryLocator {
	private NewComponentRegistryLocator() {
	}

	private static final Map<String, NewComponentRegistry> componentRegistries = new HashMap<String, NewComponentRegistry>();

	public static synchronized ComponentRegistry getComponentRegistry(
			URL registryBase) throws RegistryException {
		if (!componentRegistries.containsKey(registryBase.toExternalForm())) {
			logger.debug("constructing registry instance for " + registryBase);
			componentRegistries.put(registryBase.toExternalForm(),
					new NewComponentRegistry(registryBase));
		}
		return componentRegistries.get(registryBase.toExternalForm());
	}

	public static boolean verifyBase(URL registryBase) {
		try {
			return new Client(jaxbContext, registryBase, false).verify();
		} catch (Exception e) {
			logger.info("failed to construct connection client to "
					+ registryBase, e);
			return false;
		}
	}
}
