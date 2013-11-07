package net.sf.taverna.t2.component.registry.local;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;

public class LocalComponentRegistryLocator {
	private LocalComponentRegistryLocator() {
	}

	private static final Map<File, Registry> registries = new HashMap<File, Registry>();

	public static synchronized Registry getComponentRegistry(File registryDir)
			throws RegistryException {
		if (!registries.containsKey(registryDir))
			registries
					.put(registryDir, new LocalComponentRegistry(registryDir));
		return registries.get(registryDir);
	}

	public static Registry getComponentRegistry(URL componentRegistryBase)
			throws RegistryException {
		String path = componentRegistryBase.getPath();
		@SuppressWarnings("deprecation")
		String hackedPath = URLDecoder.decode(path);
		return getComponentRegistry(new File(hackedPath));
	}
}
