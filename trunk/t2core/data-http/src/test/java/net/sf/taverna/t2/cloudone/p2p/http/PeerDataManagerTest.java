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
package net.sf.taverna.t2.cloudone.p2p.http;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.PeerDataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.peer.PeerContainer;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PeerDataManagerTest extends AbstractCloudOneServerTest {

	@Test
	public void megaDM() throws Exception {
		InMemoryDataManager mem1 = new InMemoryDataManager("fish", Collections
				.<LocationalContext> emptySet());
		InMemoryDataManager mem2 = new InMemoryDataManager("soup", Collections
				.<LocationalContext> emptySet());
		PeerDataManager megaDM = new PeerDataManager(mem1);

		EntityListIdentifier emptyList1 = mem1.registerEmptyList(2);
		EntityListIdentifier emptyList2 = mem2.registerEmptyList(2);
		assertNotNull(megaDM.getEntity(emptyList1));
		try {
			megaDM.getEntity(emptyList2);
			fail("Did not throw NotFoundException for " + emptyList2);
		} catch (NotFoundException e) {
			// Expected
		}

	}

	@Test
	public void addPeer() throws RetrievalException, NotFoundException {

		InMemoryDataManager inMemoryDataManager = new InMemoryDataManager(
				"fish", Collections.<LocationalContext> emptySet());
		PeerDataManager megaDM = new PeerDataManager(inMemoryDataManager);
		EntityListIdentifier emptyList1 = cloudApp.getDataManager()
				.registerEmptyList(2);
		PeerContainer peer = new HttpPeerContainer();
		megaDM.addPeer(peer);
		Entity<EntityListIdentifier, ?> entity = megaDM.getEntity(emptyList1);
		assertNotNull("Entity was null", entity);
	}

	@Ignore("Hard coded identifier, requires server")
	@SuppressWarnings("unchecked")
	@Test
	public void getRemoteList() throws Exception {
		InMemoryDataManager inMemoryDataManager = new InMemoryDataManager(
				"fish", Collections.<LocationalContext> emptySet());
		PeerDataManager megaDM = new PeerDataManager(inMemoryDataManager);
		PeerContainer peer = new HttpPeerContainer();
		megaDM.addPeer(peer);
		
		EntityListIdentifier listId = EntityIdentifiers
				.parseListIdentifier("urn:t2data:list://http2p_nemesis.cs.man.ac.uk_7380/6831f2d7-d2e5-4c7f-ba30-e74312e11061/2");
		
		DataFacade facade = new DataFacade(megaDM);
		Object resolved = facade.resolve(listId);
		assertTrue("Resolved entity was not a list", resolved instanceof List);
		List<List<String>> deepList = (List<List<String>>) resolved;
		assertEquals(1, deepList.size());
		List<String> resolvedList = deepList.get(0);
		assertEquals(2, resolvedList.size());
		assertEquals("abcdefghi", resolvedList.get(0));
		assertEquals("qwertyuiop", resolvedList.get(1));
	}

}
