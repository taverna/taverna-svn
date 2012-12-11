package net.sf.taverna.t2.component.registry.myexperiment;

import static org.junit.Assert.fail;

import java.net.Authenticator;
import java.net.URL;

import net.sf.taverna.t2.component.registry.ComponentFamilyTest;
import net.sf.taverna.t2.security.credentialmanager.CredentialManagerAuthenticator;

import org.junit.BeforeClass;
import org.junit.Test;

public class MyExperimentComponentFamilyTest extends ComponentFamilyTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		registryTarget = new URL("http://aeon.cs.man.ac.uk:3006");
		Authenticator.setDefault(new CredentialManagerAuthenticator());
		componentRegistry = MyExperimentComponentRegistry.getComponentRegistry(registryTarget);
	}

	@Test
	public void testMyExperimentComponentFamily() {
		new MyExperimentComponentFamily(MyExperimentComponentRegistry.getComponentRegistry(registryTarget), null);
	}

	@Test
	public void testGetUri() {
	}

}
