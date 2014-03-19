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

import java.net.URL;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workbench.file.FileManager;
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
public class ComponentTest extends Harness {
	private Family componentFamily;
	private Component component;
	private Dataflow dataflow;

	@Before
	public void setUp() throws Exception {
		URL dataflowUrl = getClass().getClassLoader().getResource(
				"beanshell_test.t2flow");
		assertNotNull(dataflowUrl);
		dataflow = FileManager.getInstance()
				.openDataflowSilently(new T2FlowFileType(), dataflowUrl)
				.getDataflow();
		URL componentProfileUrl = getClass().getClassLoader().getResource(
				"ValidationComponent.xml");
		assertNotNull(componentProfileUrl);
		Profile componentProfile = ComponentUtil
				.makeProfile(componentProfileUrl);
		componentFamily = componentRegistry.createComponentFamily(
				"Test Component Family", componentProfile, "Some description",
				null, null);
		component = componentFamily.createComponentBasedOn("Test Component",
				"Some description", dataflow).getComponent();
	}

	@After
	public void tearDown() throws Exception {
		componentRegistry.removeComponentFamily(componentFamily);
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals("Test Component", component.getName());
		assertEquals("Test Component", component.getName());
	}

	@Test
	public void testGetComponentVersionMap() throws Exception {
		assertNotNull(component.getComponentVersionMap());
		assertEquals(1, component.getComponentVersionMap().size());
		assertEquals(component, component.getComponentVersionMap().get(1)
				.getComponent());
	}

	@Test
	public void testGetComponentVersion() throws Exception {
		assertNotNull(component.getComponentVersion(1));
		assertNull(component.getComponentVersion(2));
	}

	@Test
	public void testAddVersionBasedOn() throws Exception {
		assertNotNull(component.getComponentVersion(1));
		assertNull(component.getComponentVersion(2));
		Version componentVersion = component.addVersionBasedOn(dataflow,
				"Some description");
		assertNotNull(componentVersion);
		assertEquals(component, componentVersion.getComponent());
		assertEquals(2, componentVersion.getVersionNumber().intValue());
		assertEquals(dataflow.getIdentifier(), componentVersion.getDataflow()
				.getIdentifier());
	}

	@Test
	public void testGetComponentURL() throws Exception {
		assertNotNull(component.getComponentURL());
	}

}
