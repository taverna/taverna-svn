/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.store.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.service.model.Identifiable;
import net.sf.taverna.t2.service.model.IdentifiableImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for InMemoryGenericDao.
 *
 * @author David Withers
 */
public class InMemoryGenericDaoTest {
	
	private Identifiable<Long> bean;

	private InMemoryGenericDao<Identifiable<Long>, Long> inMemoryGenericDao;

	/**
	 * Set up test state.
	 *
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bean = new IdentifiableImpl();
		inMemoryGenericDao = new InMemoryGenericDao<Identifiable<Long>, Long>() {
			@Override
			public Long createId() {
				return 1l;
			}
		};
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.memory.InMemoryGenericDao#delete(java.io.Serializable)}.
	 */
	@Test
	public void testDelete() {
		inMemoryGenericDao.save(bean);
		assertEquals(bean, inMemoryGenericDao.get(1l));
		inMemoryGenericDao.delete(1l);
		assertNull(inMemoryGenericDao.get(1l));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.memory.InMemoryGenericDao#get(java.io.Serializable)}.
	 */
	@Test
	public void testGet() {
		assertNull(inMemoryGenericDao.get(1l));
		inMemoryGenericDao.save(bean);
		assertEquals(bean, inMemoryGenericDao.get(1l));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.memory.InMemoryGenericDao#getAll()}.
	 */
	@Test
	public void testGetValues() {
		assertEquals(0, inMemoryGenericDao.getAll().size());
		inMemoryGenericDao.save(bean);
		assertEquals(1, inMemoryGenericDao.getAll().size());
		assertTrue(inMemoryGenericDao.getAll().contains(bean));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.memory.InMemoryGenericDao#save(net.sf.taverna.t2.service.model.Identifiable)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSave() throws InterruptedException {
		assertNull(inMemoryGenericDao.get(1l));
		assertEquals(bean.getCreated(), bean.getModified());
		Thread.sleep(500);
		inMemoryGenericDao.save(bean);
		assertEquals(bean, inMemoryGenericDao.get(1l));
		assertEquals(bean.getCreated(), bean.getModified());
		
		bean = new IdentifiableImpl();
		bean.setId(5l);
		assertNull(inMemoryGenericDao.get(5l));
		assertEquals(bean.getCreated(), bean.getModified());
		Thread.sleep(500);
		inMemoryGenericDao.save(bean);
		assertEquals(bean, inMemoryGenericDao.get(5l));
		assertTrue(bean.getModified().after(bean.getCreated()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.memory.InMemoryGenericDao#setStore(java.util.Map)}.
	 */
	@Test
	public void testSetStore() {
		assertNull(inMemoryGenericDao.get(1l));
		Map<Long, Identifiable<Long>> store = new HashMap<Long, Identifiable<Long>>();
		store.put(1l, bean);
		inMemoryGenericDao.setStore(store);
		assertEquals(bean, inMemoryGenericDao.get(1l));
		store = new HashMap<Long, Identifiable<Long>>();
		inMemoryGenericDao.setStore(store);
		assertNull(inMemoryGenericDao.get(1l));
	}

}
