package net.sf.taverna.t2.component.registry.myexperiment;

import static net.sf.taverna.t2.component.registry.Harness.componentRegistry;
import static net.sf.taverna.t2.component.registry.Harness.componentRegistryUrl;
import static net.sf.taverna.t2.component.registry.myexperiment.OldComponentRegistryLocator.getComponentRegistry;

import java.net.Authenticator;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

import org.jdom.Element;

class RegistrySupport {
	public static void pre() throws Exception {
		componentRegistryUrl = new URL("http://aeon.cs.man.ac.uk:3006");
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = getComponentRegistry(componentRegistryUrl);
	}

	@SuppressWarnings("unchecked")
	public static void post() throws Exception {
		MyExperimentComponentRegistry registry = (MyExperimentComponentRegistry) getComponentRegistry(componentRegistryUrl);
		Element element = registry.getResource(componentRegistryUrl + "/files.xml", "tag=component%20profile");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
		element = registry.getResource(componentRegistryUrl + "/packs.xml", "tag=component%20family");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
		element = registry.getResource(componentRegistryUrl + "/packs.xml", "tag=component");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
	}
}
