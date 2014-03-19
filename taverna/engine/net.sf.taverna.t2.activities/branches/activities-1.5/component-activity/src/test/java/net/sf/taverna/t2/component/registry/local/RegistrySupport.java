package net.sf.taverna.t2.component.registry.local;

import static java.lang.System.getProperty;
import static net.sf.taverna.t2.component.registry.Harness.componentRegistry;
import static net.sf.taverna.t2.component.registry.Harness.componentRegistryUrl;
import static net.sf.taverna.t2.component.registry.local.LocalComponentRegistryLocator.getComponentRegistry;
import static org.apache.commons.io.FileUtils.deleteDirectory;

import java.io.File;

class RegistrySupport {
	private static File testRegistry;

	public static void pre() throws Exception {
		testRegistry = new File(getProperty("java.io.tmpdir"), "TestRegistry");
		testRegistry.mkdir();
		componentRegistryUrl = testRegistry.toURI().toURL();
		componentRegistry = getComponentRegistry(componentRegistryUrl);
	}

	public static void post() throws Exception {
		deleteDirectory(testRegistry);
	}
}
