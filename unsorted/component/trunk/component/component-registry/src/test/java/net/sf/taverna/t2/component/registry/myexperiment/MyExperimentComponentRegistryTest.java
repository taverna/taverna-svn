package net.sf.taverna.t2.component.registry.myexperiment;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;

import org.junit.Before;
import org.junit.Test;

public class MyExperimentComponentRegistryTest {

	private ComponentRegistry componentRegistry;

	@Before
	public void setup() throws Exception {
		componentRegistry = MyExperimentComponentRegistry.getComponentRegistry(new URL("http://www.myexperiment.org"));
	}

	@Test
	public void testGetComponentRegistry() throws Exception {
		assertSame(componentRegistry, MyExperimentComponentRegistry.getComponentRegistry(new URL("http://www.myexperiment.org")));
	}

	@Test
	public void testGetComponentFamilies() throws Exception {
		List<ComponentFamily> componentFamilies = componentRegistry.getComponentFamilies();
		for (ComponentFamily componentFamily : componentFamilies) {
			System.out.println(componentFamily.getName());
		}
	}

	@Test
	public void testCreateComponentFamily() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddComponentFamily() {
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
