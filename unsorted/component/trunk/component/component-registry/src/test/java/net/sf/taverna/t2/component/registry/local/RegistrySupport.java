package net.sf.taverna.t2.component.registry.local;

import static net.sf.taverna.t2.component.registry.Harness.componentRegistry;
import static net.sf.taverna.t2.component.registry.Harness.componentRegistryUrl;

import java.io.File;

import org.apache.commons.io.FileUtils;

class RegistrySupport {
	private static File testRegistry;

	public static void pre() throws Exception {
		testRegistry = new File(System.getProperty("java.io.tmpdir"),
				"TestRegistry");
		testRegistry.mkdir();
		componentRegistryUrl = testRegistry.toURI().toURL();
		componentRegistry = LocalComponentRegistry
				.getComponentRegistry(componentRegistryUrl);
	}

	public static void post() throws Exception {
		FileUtils.deleteDirectory(testRegistry);
	}
}
