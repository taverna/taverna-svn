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

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workbench.file.impl.FileManagerImpl;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

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
public class ComponentFamilyTest extends Harness {
	private Family componentFamily;
	private Profile componentProfile;
	private Dataflow dataflow;

	@Before
	public void setup() throws Exception {
		URL componentProfileUrl = getClass().getClassLoader().getResource(
				"ValidationComponent.xml");
		assertNotNull(componentProfileUrl);
		componentProfile = ComponentUtil.makeProfile(componentProfileUrl);
		componentRegistry.addComponentProfile(componentProfile, null, null);
		URL dataflowUrl = getClass().getClassLoader().getResource(
				"beanshell_test.t2flow");
		assertNotNull(dataflowUrl);
		dataflow = new FileManagerImpl().openDataflowSilently(
				new T2FlowFileType(), dataflowUrl).getDataflow();
		componentFamily = componentRegistry.createComponentFamily(
				"Test Component Family", componentProfile, "Some description",
				null, null);
	}

	@After
	public void tearDown() throws Exception {
		componentRegistry.removeComponentFamily(componentFamily);
	}

	@Test
	public void testGetComponentRegistry() throws Exception {
		assertEquals(componentRegistry, componentFamily.getComponentRegistry());
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("Test Component Family", componentFamily.getName());
		assertEquals("Test Component Family", componentFamily.getName());
	}

	@Test
	public void testGetComponentProfile() throws Exception {
		Profile componentProfile2 = componentFamily.getComponentProfile();
		assertNotNull(componentProfile2);
		String id = componentProfile.getId();
		String id2 = componentProfile2.getId();
		assertEquals(id, id2);
	}

	@Test
	public void testGetComponents() throws Exception {
		assertEquals(0, componentFamily.getComponents().size());
		assertEquals(0, componentFamily.getComponents().size());
		Version componentVersion = componentFamily.createComponentBasedOn(
				"Test Component", "Some description", dataflow);
		assertEquals(1, componentFamily.getComponents().size());
		assertTrue(componentFamily.getComponents().contains(
				componentVersion.getComponent()));
		// componentFamily.removeComponent(componentVersion.getComponent());
		// assertEquals(0, componentFamily.getComponents().size());
	}

	@Test
	public void testCreateComponentBasedOn() throws Exception {
		Version componentVersion = componentFamily.createComponentBasedOn(
				"Test Component", "Some description", dataflow);
		assertEquals("Test Component", componentVersion.getComponent()
				.getName());
	}

	@Test
	public void testGetComponent() throws Exception {
		assertNull(componentFamily.getComponent("Test Component"));
		Version componentVersion = componentFamily.createComponentBasedOn(
				"Test Component", "Some description", dataflow);
		assertNotNull(componentFamily.getComponent("Test Component"));
		assertEquals(componentVersion.getComponent(),
				componentFamily.getComponent("Test Component"));
	}

}
