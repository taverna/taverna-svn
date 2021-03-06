/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.platform.spring;

import static org.junit.Assert.*;

import net.sf.taverna.raven.spi.SpiRegistry;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests the spring / raven integration part of the T2Platform module
 * 
 * @author Tom Oinn
 * 
 */
public class RavenSpringIntegrationTest {

	@Test
	/**
	 * Test verbose form of context configuration and instantiation of a remote
	 * bean, in this case one from the t2 implementation package.
	 */
	public void testRavenEnabledBeanConstruction() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext.xml");
		Object o = context.getBean("ravenTestBean");
		assertEquals(o.getClass().getName(),
				"net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce");
	}

	@SuppressWarnings("unchecked")
	@Test
	/**
	 * Test compact form of repository specification and the spi registry bean
	 * factory
	 */
	public void testSpiWithCompactContextForm() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"applicationContext2.xml");
		SpiRegistry spi = (SpiRegistry) context.getBean("spiBean");
		System.out.println("SPI : " + spi.toString());
		for (Class c : spi) {
			System.out.println("  " + c.getCanonicalName());
		}
		System.out.println("Done SPI scan, " + spi.getClasses().size()
				+ " implementation(s) found.");
		assertEquals(spi.getClasses().size(), 1);
	}

}
