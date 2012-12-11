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

import static org.junit.Assert.*;

import java.net.URL;

import net.sf.taverna.t2.component.profile.ComponentProfile;

import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author David Withers
 */
public class ComponentFamilyTest {

	protected static URL registryTarget;
	protected static ComponentRegistry componentRegistry;
    protected ComponentFamily componentFamily;
	protected ComponentProfile componentProfile;
	protected URL componentProfileUrl;

	@Before
	public void setup() throws Exception {
		componentProfileUrl = getClass().getClassLoader().getResource("ValidationComponent.xml");
		assertNotNull(componentProfileUrl);
		componentProfile = new ComponentProfile(componentProfileUrl);
	}

	@Test
    public void testGetComponentRegistry() {
    }

    @Test
    public void testGetName() {
    }

    @Test
    public void testGetComponentProfile() {
    }

    @Test
    public void testGetComponents() {
    }

    @Test
    public void testCreateComponentBasedOn() {
    }

    @Test
    public void testGetComponent() {
    }

}
