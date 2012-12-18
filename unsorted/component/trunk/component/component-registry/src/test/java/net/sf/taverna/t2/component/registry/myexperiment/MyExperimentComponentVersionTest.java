package net.sf.taverna.t2.component.registry.myexperiment;

import java.net.Authenticator;
import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.component.registry.ComponentTest;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

import org.jdom.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 *
 * @author David Withers
 */
@Ignore
public class MyExperimentComponentVersionTest extends ComponentTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		componentRegistryUrl = new URL("http://aeon.cs.man.ac.uk:3006");
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = MyExperimentComponentRegistry.getComponentRegistry(componentRegistryUrl);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MyExperimentComponentRegistry registry = MyExperimentComponentRegistry.getComponentRegistry(componentRegistryUrl);
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
