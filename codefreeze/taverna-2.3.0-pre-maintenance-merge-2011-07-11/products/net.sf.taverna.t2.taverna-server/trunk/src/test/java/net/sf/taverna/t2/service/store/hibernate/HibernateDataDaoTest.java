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
package net.sf.taverna.t2.service.store.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.service.model.Data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 *
 * @author David Withers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HibernateDataDaoTest {

	@Autowired
	private HibernateDataDao dataDao;;
	
	private Data data;

	@Before
	public void setUp() throws Exception {
		data = new Data();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#delete(java.io.Serializable)}.
	 */
	@Test
	public void testDelete() {
		dataDao.save(data);
		assertEquals(data, dataDao.get(data.getId()));
		dataDao.delete(data.getId());
		assertNull(dataDao.get(data.getId()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#get(java.io.Serializable)}.
	 */
	@Test
	public void testGet() {
		dataDao.save(data);
		assertEquals(data, dataDao.get(data.getId()));
		dataDao.delete(data.getId());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#getAll()}.
	 */
	@Test
	public void testGetAll() {
		assertEquals(0, dataDao.getAll().size());
		dataDao.save(data);
		assertEquals(1, dataDao.getAll().size());
		assertTrue(dataDao.getAll().contains(data));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#save(net.sf.taverna.t2.service.model.IdentifiableBean)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSave() throws InterruptedException {
		assertEquals(data.getCreated(), data.getModified());
		Thread.sleep(500);
		dataDao.save(data);
		assertEquals(data, dataDao.get(data.getId()));
		assertEquals(data.getCreated(), data.getModified());		
		Thread.sleep(500);
		dataDao.save(data);
		assertTrue(data.getModified().after(data.getCreated()));
		dataDao.delete(data.getId());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#getBeanClass()}.
	 */
	@Test
	public void testGetBeanClass() {
		assertEquals(dataDao.getBeanClass(), Data.class);
	}

}
