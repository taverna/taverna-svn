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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

public class LatestVersionFilterTest extends TestCase {

	public void testLatestVersionFilter() {
		LatestVersionFilter filter = new LatestVersionFilter();
		List<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.add(new BasicArtifact("second", "artifact", "1.4.0")); // 0
		artifacts.add(new BasicArtifact("second", "third", "1.3.a")); // 1
		artifacts.add(new BasicArtifact("second", "third", "1.3.9")); // 2
		artifacts.add(new BasicArtifact("second", "artifact", "1.4")); // 3
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.0")); // 4
		artifacts.add(new BasicArtifact("first", "artifact", "1.5")); // 5
		artifacts.add(new BasicArtifact("first", "artifact", "1.4.1")); // 6
		artifacts.add(new BasicArtifact("first", "artifact", "1.0")); // 7

		Set<Artifact> artifactSet = new HashSet<Artifact>(artifacts);
		assertEquals(8, artifactSet.size());
		Set<Artifact> filtered = filter.filter(artifactSet);
		assertEquals(3, filtered.size()); // One version pr artifact
		assertEquals(8, artifacts.size()); // Did not touch

		// Should only contain these three versions
		assertTrue(filtered.contains(artifacts.get(5))); // 1.5
		assertTrue(filtered.contains(artifacts.get(0))); // 1.4.0
		assertTrue(filtered.contains(artifacts.get(1))); // 1.3.a
		// And nothing else
		// (Yes, this is also asserted by filtered.size(),
		// but we test that filtered.contains() is not broken)
		assertFalse(filtered.contains(artifacts.get(4)));
	}

}
