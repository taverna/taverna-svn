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
package net.sf.taverna.platform.spring.hibernate;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;

import org.junit.Test;

/**
 * Uses a patched version of hibernate to pull in bean definitions from Raven
 * 
 * @author Tom Oinn
 * 
 */
public class SpiDrivenHibernateTest {

	@Test
	/**
	 * Works around the current raven issues by preloading a bean from the
	 * implementation package - shouldn't be needed as raven should be scanning
	 * for existing artifacts!
	 */
	public void testSpiBasedDerbyInit() {
		new RavenAwareClassPathXmlApplicationContext("applicationContext4.xml");
	}
}
