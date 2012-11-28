package net.sf.taverna.t2.component.registry.myexperiment;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyExperimentComponentRegistryTest {

	private static final URI myExperimentTarget = URI.create("http://sandbox.myexperiment.org");
//	private static final URI myExperimentTarget = URI.create("http://www.myexperiment.org");
	private ComponentRegistry componentRegistry;

	@BeforeClass
	public static void staticSetup() throws Exception {
//		UsernamePassword usernamePassword = new UsernamePassword("test", "test");
//		CredentialManager.getInstance().saveUsernameAndPasswordForService(usernamePassword, myExperimentTarget);
	}

	@Before
	public void setup() throws Exception {
		componentRegistry = MyExperimentComponentRegistry.getComponentRegistry(myExperimentTarget.toURL());
	}

	@Test
	public void testGetComponentRegistry() throws Exception {
		assertSame(componentRegistry, MyExperimentComponentRegistry.getComponentRegistry(myExperimentTarget.toURL()));
	}

	@Test
	public void testGetComponentFamilies() throws Exception {
		List<ComponentFamily> componentFamilies = componentRegistry.getComponentFamilies();
		for (ComponentFamily componentFamily : componentFamilies) {
			System.out.println("Component family : " + componentFamily.getName());
			List<Component> components = componentFamily.getComponents();
			for (Component component : components) {
				System.out.println("  Component : " + component.getName());
				Collection<ComponentVersion> values = component.getComponentVersionMap().values();
				for (ComponentVersion componentVersion : values) {
					System.out.println("    Version : " + componentVersion.getVersionNumber());
				}
			}
		}
	}

	@Test
	public void testCreateComponentFamily() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveComponentFamily() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResourceElements() {
		fail("Not yet implemented");
	}

}
