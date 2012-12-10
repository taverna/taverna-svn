package net.sf.taverna.t2.component.registry.myexperiment;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.Authenticator;
import java.net.URL;

import net.sf.taverna.t2.component.registry.ComponentRegistryTest;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyExperimentComponentRegistryTest extends ComponentRegistryTest {

	@BeforeClass
	public static void staticSetup() throws Exception {
		myExperimentTarget = new URL("http://aeon.cs.man.ac.uk:3006");
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = (MyExperimentComponentRegistry) MyExperimentComponentRegistry.getComponentRegistry(myExperimentTarget);
	}

	@Before
	public void setup() throws Exception {
		super.setup();
		assertTrue(componentRegistry.getComponentProfiles().size() > 0);
		componentProfile = componentRegistry.getComponentProfiles().get(0);
	}

	@Test
	public void testGetComponentRegistry() throws Exception {
		assertSame(componentRegistry, MyExperimentComponentRegistry.getComponentRegistry(myExperimentTarget));
	}

}
