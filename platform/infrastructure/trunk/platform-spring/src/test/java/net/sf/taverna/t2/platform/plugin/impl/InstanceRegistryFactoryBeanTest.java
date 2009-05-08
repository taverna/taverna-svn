/***********************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
 ***********************************************************************/
package net.sf.taverna.t2.platform.plugin.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.platform.spring.InstanceRegistryFactoryBean;
import net.sf.taverna.t2.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.platform.spring.test.DummyBean;
import net.sf.taverna.t2.platform.spring.test.DummyImplementation2;
import net.sf.taverna.t2.platform.spring.test.DummyInterface;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test {@link InstanceRegistryFactoryBean}. In particular this test also covers
 * bean initialisation of the SPI instances.
 * 
 * @author Stian Soiland-Reyes
 */
public class InstanceRegistryFactoryBeanTest {

	private ApplicationContext context;

	@Before
	public void makeContext() {
		context = new RavenAwareClassPathXmlApplicationContext(
				"instanceRegistryTestContext.xml");
	}

	@Test
	public void dummyBean() {
		DummyBean dummyBean = (DummyBean) context.getBean("dummyBean");
		assertEquals("Did not set the dummy bean", "The fish", dummyBean
				.getFish());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simpleSPI() throws Exception {
		InstanceRegistryImpl<DummyInterface> dummyRegistry = (InstanceRegistryImpl<DummyInterface>) context
				.getBean("dummyRegistry");
		List<DummyInterface> dummyInterfaces = IteratorUtils
				.toList(dummyRegistry.iterator());
		@SuppressWarnings("unused")
		SPIRegistryImpl spiReg = (SPIRegistryImpl) dummyRegistry
				.getSpiRegistry();
	
		assertTrue(
				"Should have found DummyInterface implementations from system classloader",
				! dummyInterfaces.isEmpty());

		//spiReg.classLoaderAdded(getClass().getClassLoader());
		//dummyInterfaces = IteratorUtils.toList(dummyRegistry.iterator());
		assertEquals(
				"Did not find the two expected DummyInterface implementations",
				2, dummyInterfaces.size());
		DummyImplementation2 dummyImp2;
		if (dummyInterfaces.get(0) instanceof DummyImplementation2) {
			dummyImp2 = (DummyImplementation2) dummyInterfaces.get(0);
		} else {
			dummyImp2 = (DummyImplementation2) dummyInterfaces.get(1);
		}
		assertNull("Did set the dummy bean", dummyImp2.getDummyBean());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simpleSPIWithInit() throws Exception {
		InstanceRegistryImpl<DummyInterface> dummyRegistry = (InstanceRegistryImpl<DummyInterface>) context
				.getBean("dummyRegistryInit");
		@SuppressWarnings("unused")
		SPIRegistryImpl spiReg = (SPIRegistryImpl) dummyRegistry
				.getSpiRegistry();
		//spiReg.classLoaderAdded(getClass().getClassLoader());
		List<DummyInterface> dummyInterfaces = IteratorUtils
				.toList(dummyRegistry.iterator());
		assertEquals(
				"Did not find the two expected DummyInterface implementations",
				2, dummyInterfaces.size());
		DummyImplementation2 dummyImp2;
		if (dummyInterfaces.get(0) instanceof DummyImplementation2) {
			dummyImp2 = (DummyImplementation2) dummyInterfaces.get(0);
		} else {
			dummyImp2 = (DummyImplementation2) dummyInterfaces.get(1);
		}
		assertEquals("Did not set the dummy bean", "The fish", dummyImp2
				.getDummyBean().getFish());
	}

}
