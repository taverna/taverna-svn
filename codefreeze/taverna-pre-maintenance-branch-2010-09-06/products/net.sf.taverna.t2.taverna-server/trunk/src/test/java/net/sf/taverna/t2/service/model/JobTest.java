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
package net.sf.taverna.t2.service.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author David Withers
 */
public class JobTest {
	
	private Random random;
	
	private long value;

	private Job job;
	
	@Before
	public void setUp() throws Exception {
		random = new Random();
		job = new Job();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(job.hashCode(), job.hashCode());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertEquals(job, job);
		Job job2 = new Job();
		job.setId(5l);
		assertFalse(job.equals(job2));
		assertFalse(job2.equals(job));
		assertFalse(job.equals(null));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#getOutputs()}.
	 */
	@Test
	public void testGetWorkflow() {
		assertNull(job.getWorkflow());
		value = random.nextLong();
		job.setWorkflow(value);
		assertEquals(new Long(value), job.getWorkflow());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#setWorkflow(java.lang.Long)}.
	 */
	@Test
	public void testSetWorkflow() {
		assertNull(job.getWorkflow());
		job.setWorkflow(0l);
		assertEquals(new Long(0l), job.getWorkflow());
		value = random.nextLong();
		job.setWorkflow(-value);
		assertEquals(new Long(-value), job.getWorkflow());
		value = random.nextLong();
		job.setWorkflow(new Long(value));
		assertEquals(new Long(value), job.getWorkflow());
		job.setWorkflow(null);
		assertNull(job.getWorkflow());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#getInputs()}.
	 */
	@Test
	public void testGetInputs() {
		assertNull(job.getInputs());
		value = random.nextLong();
		job.setInputs(value);
		assertEquals(new Long(value), job.getInputs());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#setInputs(java.lang.Long)}.
	 */
	@Test
	public void testSetInputs() {
		assertNull(job.getInputs());
		job.setInputs(0l);
		assertEquals(new Long(0l), job.getInputs());
		value = random.nextLong();
		job.setInputs(-value);
		assertEquals(new Long(-value), job.getInputs());
		value = random.nextLong();
		job.setInputs(new Long(value));
		assertEquals(new Long(value), job.getInputs());
		job.setInputs(null);
		assertNull(job.getInputs());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#getOutputs()}.
	 */
	@Test
	public void testGetOutputs() {
		assertNull(job.getOutputs());
		value = random.nextLong();
		job.setOutputs(value);
		assertEquals(new Long(value), job.getOutputs());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#setOutputs(java.lang.Long)}.
	 */
	@Test
	public void testSetOutputs() {
		assertNull(job.getOutputs());
		job.setOutputs(0l);
		assertEquals(new Long(0l), job.getOutputs());
		value = random.nextLong();
		job.setOutputs(-value);
		assertEquals(new Long(-value), job.getOutputs());
		value = random.nextLong();
		job.setOutputs(new Long(value));
		assertEquals(new Long(value), job.getOutputs());
		job.setOutputs(null);
		assertNull(job.getOutputs());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#getStatus()}.
	 */
	@Test
	public void testGetStatus() {
		assertNull(job.getStatus());
		job.setStatus("RUNNING");
		assertEquals("RUNNING", job.getStatus());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.model.Job#setStatus(java.lang.String)}.
	 */
	@Test
	public void testSetStatus() {
		assertNull(job.getStatus());
		job.setStatus("COMPLETE");
		assertEquals("COMPLETE", job.getStatus());
		job.setStatus(null);
		assertNull(job.getStatus());
	}

}
