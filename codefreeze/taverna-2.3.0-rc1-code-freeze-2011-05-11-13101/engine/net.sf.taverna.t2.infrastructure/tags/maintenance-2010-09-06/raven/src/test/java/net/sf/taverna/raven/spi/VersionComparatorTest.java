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
package net.sf.taverna.raven.spi;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

public class VersionComparatorTest extends TestCase {
	/*
	 * Test method for
	 * 'net.sf.taverna.raven.spi.LatestVersionFilter.lessThan(String, String)'
	 */
	public void testCompareVersions() {
		VersionComparator cmp = VersionComparator.getInstance();
		assertTrue(cmp.compareVersions("1.3", "1.3.1") < 0);
		assertEquals(0, cmp.compareVersions("1.3", "1.3"));
		assertEquals(0, cmp.compareVersions("1.3.01", "1.3.1"));
		assertEquals(0, cmp.compareVersions("1.a.01", "1.a.1"));
		assertTrue(cmp.compareVersions("1.3.2.a", "1.3.2.b") < 0);
		// As stated in documentation
		assertTrue(cmp.compareVersions("1.2.10", "1.2.8") > 0);
		assertTrue(cmp.compareVersions("1.5", "1.4.1") > 0);
		assertTrue(cmp.compareVersions("1.3", "1.3.0") < 0);
		assertEquals(0, cmp.compareVersions("1.01.2", "1.1.2"));
	}

	public void testSort() {
		List<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.add(new BasicArtifact("second", "artifact", "1.4.0")); // 5
		artifacts.add(new BasicArtifact("second", "third", "1.3.a")); // 7
		artifacts.add(new BasicArtifact("second", "third", "1.3.9")); // 6
		artifacts.add(new BasicArtifact("second", "artifact", "1.4")); // 4
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.0")); // 1
		artifacts.add(new BasicArtifact("first", "artifact", "1.5")); // 3
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.1")); // 2
		artifacts.add(new BasicArtifact("first", "artifact", "1.0")); // 0
		VersionComparator.sort(artifacts);
		// Note that #1 and #5 have the same version number, and so
		// we must check the groupID as well
		assertEquals("1.0", artifacts.get(0).getVersion());
		assertEquals("first", artifacts.get(1).getGroupId());
		assertEquals("1.4.0", artifacts.get(1).getVersion());
		assertEquals("1.4.1", artifacts.get(2).getVersion());
		assertEquals("1.5", artifacts.get(3).getVersion());
		assertEquals("1.4", artifacts.get(4).getVersion());
		assertEquals("second", artifacts.get(5).getGroupId());
		assertEquals("1.4.0", artifacts.get(5).getVersion());
		assertEquals("1.3.9", artifacts.get(6).getVersion());
		assertEquals("1.3.a", artifacts.get(7).getVersion());
	}

}
