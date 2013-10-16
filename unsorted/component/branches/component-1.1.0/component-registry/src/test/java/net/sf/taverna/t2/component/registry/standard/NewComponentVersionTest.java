package net.sf.taverna.t2.component.registry.standard;

import net.sf.taverna.t2.component.registry.ComponentVersionTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 *
 * @author David Withers
 */
@Ignore
public class NewComponentVersionTest extends ComponentVersionTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RegistrySupport.pre();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RegistrySupport.post();
	}
}
