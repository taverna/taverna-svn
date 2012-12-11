/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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

	protected static URL registryTarget;
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
		assertEquals(registryTarget, componentRegistry.getRegistryBase());
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
