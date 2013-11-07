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
package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistryLocator.getComponentRegistry;
import static net.sf.taverna.t2.component.registry.standard.Utils.getDataflowFromUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.URL;

import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentRegistryTest;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * 
 * @author David Withers
 */
@Ignore
public class NewComponentRegistryTest extends ComponentRegistryTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RegistrySupport.pre();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RegistrySupport.post();
	}

	@Test
	public void testGetComponentRegistry() throws Exception {
		assertSame(componentRegistry,
				getComponentRegistry(componentRegistryUrl));
	}

	@Test
	public void testUploadWorkflow() throws Exception {
		URL dataflowUrl = getClass().getClassLoader().getResource(
				"beanshell_test.t2flow");
		Dataflow dataflow = getDataflowFromUri(dataflowUrl.toString());

		NewComponentRegistry registry = (NewComponentRegistry) getComponentRegistry(componentRegistryUrl);
		Version v = registry.createComponentFrom(null, "Test Workflow",
				"test description", dataflow, null, Policy.PRIVATE);
		assertEquals("test description", v.getDescription());
		registry.deleteComponent((NewComponent) v.getComponent());
	}

}
