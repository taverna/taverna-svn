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
package net.sf.taverna.raven.repository;

import junit.framework.TestCase;

public class BasicArtifactTest extends TestCase {

	public void testEqual() {
		BasicArtifact batik = new BasicArtifact("batik", "batik-swing", "1.6");
		BasicArtifact batikSame = new BasicArtifact("batik", "batik-swing",
				"1.6");
		BasicArtifact batikVersion = new BasicArtifact("batik", "batik-swing",
				"1.6.1");
		BasicArtifact batikArtifact = new BasicArtifact("batik",
				"batik-swings", "1.6");
		BasicArtifact batikGroup = new BasicArtifact("batikk", "batik-swing",
				"1.6");
		assertEquals(batik, batik);
		assertEquals(batik, batikSame);
		assertFalse(batik.equals(batikVersion));
		assertFalse(batik.equals(batikArtifact));
		assertFalse(batik.equals(batikGroup));

		assertEquals(0, batik.compareTo(batikSame));
		assertTrue(batik.compareTo(batikVersion) < 0);
		assertTrue(batik.compareTo(batikArtifact) < 0);
		assertTrue(batik.compareTo(batikGroup) < 0);
		assertTrue(batikVersion.compareTo(batikGroup) < 0);

	}

	public void testToString() {
		BasicArtifact mavenReporting = new BasicArtifact(
				"org.apache.maven.reporting", "maven-reporting-api", "2.0");
		assertEquals("org.apache.maven.reporting:maven-reporting-api:2.0",
				mavenReporting.toString());
	}

}
