package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.Harness.componentRegistry;
import static net.sf.taverna.t2.component.registry.Harness.componentRegistryUrl;
import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistryLocator.getComponentRegistry;

import java.net.Authenticator;
import java.net.URL;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

class RegistrySupport {
	static final String DEPLOYMENT = "http://aeon.cs.man.ac.uk:3006";

	public static void pre() throws Exception {
		componentRegistryUrl = new URL(DEPLOYMENT);
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = getComponentRegistry(componentRegistryUrl);
	}

	public static void post() throws Exception {
		NewComponentRegistry registry = (NewComponentRegistry) getComponentRegistry(componentRegistryUrl);
		for (Profile p : registry.getComponentProfiles())
			registry.client.delete("/file.xml", "id=" + p.getId());
		for (Family f : registry.getComponentFamilies()) {
			for (Component c : f.getComponents())
				registry.deleteComponent((NewComponent) c);
			registry.client.delete("/pack.xml", "id="
					+ ((NewComponentFamily) f).getId());
		}
	}
}
