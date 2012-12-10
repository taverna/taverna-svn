package net.sf.taverna.t2.component.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import net.sf.taverna.t2.component.profile.ComponentProfile;

import org.junit.Before;
import org.junit.Test;

public abstract class ComponentRegistryTest {

	protected static URL myExperimentTarget;
	protected static ComponentRegistry componentRegistry;
	protected ComponentProfile componentProfile;
	protected URL componentProfileUrl;

	@Before
	public void setup() throws Exception {
		componentProfileUrl = getClass().getClassLoader().getResource("ValidationComponent.xml");
		assertNotNull(componentProfileUrl);
		componentProfile = new ComponentProfile(componentProfileUrl);
	}

	@Test
	public void testGetComponentFamilies() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		ComponentFamily componentFamily = componentRegistry.createComponentFamily("TestComponentFamily", componentProfile);
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertTrue(componentRegistry.getComponentFamilies().contains(componentFamily));
		componentRegistry.removeComponentFamily(componentFamily);
		assertEquals(0, componentRegistry.getComponentFamilies().size());
	}

	@Test
	public void testGetComponentFamily() throws Exception {
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		ComponentFamily componentFamily = componentRegistry.createComponentFamily("TestComponentFamily", componentProfile);
		assertNotNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertNotNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily, componentRegistry.getComponentFamily("TestComponentFamily"));
		componentRegistry.removeComponentFamily(componentFamily);
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
	}

	@Test
	public void testCreateComponentFamily() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		ComponentFamily componentFamily = componentRegistry.createComponentFamily("TestComponentFamily", componentProfile);
		assertEquals("TestComponentFamily", componentFamily.getName());
		assertEquals(componentRegistry, componentFamily.getComponentRegistry());
		assertEquals(0, componentFamily.getComponents().size());
//		assertEquals(componentProfile, componentFamily.getComponentProfile());
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertNotNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily, componentRegistry.getComponentFamily("TestComponentFamily"));
		componentRegistry.removeComponentFamily(componentFamily);
	}

	@Test
	public void testRemoveComponentFamily() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		ComponentFamily componentFamily = componentRegistry.createComponentFamily("TestComponentFamily", componentProfile);
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertNotNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily, componentRegistry.getComponentFamily("TestComponentFamily"));
		componentRegistry.removeComponentFamily(componentFamily);
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
	}

	@Test
	public void testGetResistryBase() throws Exception {
		assertEquals(myExperimentTarget, componentRegistry.getRegistryBase());
	}

	@Test
	public void testGetComponentProfiles() throws Exception {
		assertNotNull(componentRegistry.getComponentProfiles());
	}

	@Test
	public void testAddComponentProfile() throws Exception {
		int componentProfileCount = componentRegistry.getComponentProfiles().size();
		componentRegistry.addComponentProfile(componentProfileUrl);
		assertEquals(componentProfileCount + 1, componentRegistry.getComponentProfiles().size());
	}

}
