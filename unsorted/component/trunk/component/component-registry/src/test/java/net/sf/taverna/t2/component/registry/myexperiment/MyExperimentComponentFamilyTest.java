package net.sf.taverna.t2.component.registry.myexperiment;

import java.net.Authenticator;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.component.registry.ComponentFamilyTest;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

import org.jdom.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class MyExperimentComponentFamilyTest extends ComponentFamilyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		registryTarget = new URL("http://aeon.cs.man.ac.uk:3006");
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = MyExperimentComponentRegistry.getComponentRegistry(registryTarget);
	}

	@AfterClass
	public static void cleanUpAfterClass() throws Exception {
		MyExperimentComponentRegistry registry = MyExperimentComponentRegistry.getComponentRegistry(registryTarget);
		Element element = registry.getResource(registryTarget + "/files.xml", "tag=component%20profile");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
		element = registry.getResource(registryTarget + "/packs.xml", "tag=component%20family");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
		element = registry.getResource(registryTarget + "/packs.xml", "tag=component");
		for (Element child : (List<Element>) element.getChildren()) {
			registry.deleteResource(child.getAttributeValue("uri"));
		}
	}

}
