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
import net.sf.taverna.t2.service.model.Workflow;

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
public class HibernateWorkflowDaoTest {

	@Autowired
	private HibernateWorkflowDao workflowDao;;
	
	private Workflow workflow;

	@Before
	public void setUp() throws Exception {
		workflow = new Workflow();
		workflow.setXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>test</test>");
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#delete(java.io.Serializable)}.
	 */
	@Test
	public void testDelete() {
		workflowDao.save(workflow);
		assertEquals(workflow, workflowDao.get(workflow.getId()));
		workflowDao.delete(workflow.getId());
		assertNull(workflowDao.get(workflow.getId()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#get(java.io.Serializable)}.
	 */
	@Test
	public void testGet() {
		workflowDao.save(workflow);
		assertEquals(workflow, workflowDao.get(workflow.getId()));
		workflowDao.delete(workflow.getId());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#getAll()}.
	 */
	@Test
	public void testGetAll() {
		assertEquals(0, workflowDao.getAll().size());
		workflowDao.save(workflow);
		assertEquals(1, workflowDao.getAll().size());
		assertTrue(workflowDao.getAll().contains(workflow));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#save(net.sf.taverna.t2.service.model.IdentifiableBean)}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSave() throws InterruptedException {
		assertEquals(workflow.getCreated(), workflow.getModified());
		Thread.sleep(500);
		workflowDao.save(workflow);
		assertEquals(workflow, workflowDao.get(workflow.getId()));
		assertEquals(workflow.getCreated(), workflow.getModified());		
		Thread.sleep(500);
		workflowDao.save(workflow);
		assertTrue(workflow.getModified().after(workflow.getCreated()));
		workflowDao.delete(workflow.getId());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.store.hibernate.HibernateGenericDao#getBeanClass()}.
	 */
	@Test
	public void testGetBeanClass() {
		assertEquals(workflowDao.getBeanClass(), Workflow.class);
	}

}
