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
package net.sf.taverna.t2.component.registry.myexperiment;

import static net.sf.taverna.t2.component.registry.myexperiment.OldComponentRegistryLocator.getComponentRegistry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import net.sf.taverna.t2.component.registry.ComponentRegistryTest;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;
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
public class MyExperimentComponentRegistryTest extends ComponentRegistryTest {
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
		MyExperimentComponentRegistry registry = (MyExperimentComponentRegistry) getComponentRegistry(componentRegistryUrl);
		URL dataflowUrl = getClass().getClassLoader().getResource(
				"beanshell_test.t2flow");
		Dataflow dataflow = FileManager.getInstance()
				.openDataflowSilently(new T2FlowFileType(), dataflowUrl)
				.getDataflow();
		ByteArrayOutputStream dataflowStream = new ByteArrayOutputStream();
		FileManager.getInstance().saveDataflowSilently(dataflow,
				new T2FlowFileType(), dataflowStream, false);
		String dataflowString = dataflowStream.toString("UTF-8");
		Element element = registry.uploadWorkflow(dataflowString,
				"Test Workflow", "Test description", null,
				MyExperimentComponentRegistry.PUBLIC.getPolicyString());
		assertEquals("Test Workflow", element.getChild("title").getValue());
		registry.deleteResource(element.getAttributeValue("uri"));
	}

}
