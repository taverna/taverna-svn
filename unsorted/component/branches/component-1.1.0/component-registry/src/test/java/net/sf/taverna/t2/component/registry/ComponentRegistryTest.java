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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * 
 * @author David Withers
 */
@Ignore
public abstract class ComponentRegistryTest extends Harness {
	private Profile componentProfile;

	@Before
	public void setup() throws Exception {
		URL componentProfileUrl = getClass().getClassLoader().getResource(
				"ValidationComponent.xml");
		assertNotNull(componentProfileUrl);
		componentProfile = ComponentUtil.makeProfile(componentProfileUrl);
	}

	@After
	public void tearDown() throws Exception {
		for (Family componentFamily : componentRegistry.getComponentFamilies()) {
			componentRegistry.removeComponentFamily(componentFamily);
		}
	}

	@Test
	public void testGetComponentFamilies() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		Family componentFamily = componentRegistry.createComponentFamily(
				"TestComponentFamily", componentProfile, "Some description",
				null, null);
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertTrue(componentRegistry.getComponentFamilies().contains(
				componentFamily));
		componentRegistry.removeComponentFamily(componentFamily);
		assertEquals(0, componentRegistry.getComponentFamilies().size());
	}

	@Test
	public void testGetComponentFamily() throws Exception {
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		Family componentFamily = componentRegistry.createComponentFamily(
				"TestComponentFamily", componentProfile, "Some description",
				null, null);
		assertNotNull(componentRegistry
				.getComponentFamily("TestComponentFamily"));
		assertNotNull(componentRegistry
				.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily,
				componentRegistry.getComponentFamily("TestComponentFamily"));
		componentRegistry.removeComponentFamily(componentFamily);
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
	}

	@Test
	public void testCreateComponentFamily() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		Family componentFamily = componentRegistry.createComponentFamily(
				"TestComponentFamily", componentProfile, "Some description",
				null, null);
		assertEquals("TestComponentFamily", componentFamily.getName());
		assertEquals(componentRegistry, componentFamily.getComponentRegistry());
		assertEquals(0, componentFamily.getComponents().size());
		// assertEquals(componentProfile,
		// componentFamily.getComponentProfile());
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertNotNull(componentRegistry
				.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily,
				componentRegistry.getComponentFamily("TestComponentFamily"));
	}

	@Test(expected = RegistryException.class)
	public void testCreateComponentFamilyDuplicate() throws Exception {
		componentRegistry.createComponentFamily("TestComponentFamily",
				componentProfile, "Some description", null, null);
		componentRegistry.createComponentFamily("TestComponentFamily",
				componentProfile, "Some description", null, null);
	}

	@Test(expected = RegistryException.class)
	public void testCreateComponentFamilyNullProfile() throws Exception {
		componentRegistry.createComponentFamily("TestComponentFamily", null,
				"Some description", null, null);
	}

	@Test(expected = RegistryException.class)
	public void testCreateComponentFamilyNullName() throws Exception {
		componentRegistry.createComponentFamily(null, componentProfile,
				"Some description", null, null);
	}

	@Test
	public void testRemoveComponentFamily() throws Exception {
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
		Family componentFamily = componentRegistry.createComponentFamily(
				"TestComponentFamily", componentProfile, "Some description",
				null, null);
		assertEquals(1, componentRegistry.getComponentFamilies().size());
		assertNotNull(componentRegistry
				.getComponentFamily("TestComponentFamily"));
		assertEquals(componentFamily,
				componentRegistry.getComponentFamily("TestComponentFamily"));
		componentRegistry.removeComponentFamily(componentFamily);
		assertEquals(0, componentRegistry.getComponentFamilies().size());
		assertNull(componentRegistry.getComponentFamily("TestComponentFamily"));
	}

	@Test
	public void testGetResistryBase() throws Exception {
		assertEquals(componentRegistryUrl, componentRegistry.getRegistryBase());
	}

	@Test
	public void testGetComponentProfiles() throws Exception {
		assertNotNull(componentRegistry.getComponentProfiles());
	}

	@Test
	public void testAddComponentProfile() throws Exception {
		List<Profile> componentProfiles = componentRegistry
				.getComponentProfiles();
		boolean contained = false;
		for (Profile p : componentProfiles) {
			if (p.getId().equals(componentProfile.getId())) {
				contained = true;
			}
		}
		assertFalse(contained);
		int componentProfileCount = componentProfiles.size();
		componentRegistry.addComponentProfile(componentProfile, null, null);
		int newSize = componentRegistry.getComponentProfiles().size();
		assertEquals(componentProfileCount + 1, newSize);
	}

	@Test(expected = RegistryException.class)
	public void testAddComponentProfileNullProfile() throws Exception {
		componentRegistry.addComponentProfile(null, null, null);
	}

}
