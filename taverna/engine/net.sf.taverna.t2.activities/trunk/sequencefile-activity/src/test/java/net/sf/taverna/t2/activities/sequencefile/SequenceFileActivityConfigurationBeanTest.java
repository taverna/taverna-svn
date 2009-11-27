/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sequencefile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for SequenceFileActivityConfigurationBean.
 * 
 * @author David Withers
 */
public class SequenceFileActivityConfigurationBeanTest {

	private SequenceFileActivityConfigurationBean configuration;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		configuration = new SequenceFileActivityConfigurationBean();
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#hashCode()}
	 * .
	 */
	@Test
	public void testHashCode() {
		assertEquals(configuration.hashCode(), configuration.hashCode());
		assertEquals(configuration.hashCode(), new SequenceFileActivityConfigurationBean()
				.hashCode());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#SequenceFileActivityConfigurationBean()}
	 * .
	 */
	@Test
	public void testSequenceFileActivityConfigurationBean() {
		assertNotNull(configuration);
		assertEquals(FileFormat.fasta, configuration.getFileFormat());
		assertEquals(SequenceType.dna, configuration.getSequenceType());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#SequenceFileActivityConfigurationBean(net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean)}
	 * .
	 */
	@Test
	public void testSequenceFileActivityConfigurationBeanSequenceFileActivityConfigurationBean() {
		configuration.setFileFormat(FileFormat.swissprot);
		configuration.setSequenceType(SequenceType.protein);
		SequenceFileActivityConfigurationBean newConfiguration = new SequenceFileActivityConfigurationBean(
				configuration);
		assertEquals(configuration, newConfiguration);
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#getFileFormat()}
	 * .
	 */
	@Test
	public void testGetFileFormat() {
		assertEquals(FileFormat.fasta, configuration.getFileFormat());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#setFileFormat(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetFileFormat() {
		configuration.setFileFormat(FileFormat.raw);
		assertEquals(FileFormat.raw, configuration.getFileFormat());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#getSequenceType()}
	 * .
	 */
	@Test
	public void testGetSequenceType() {
		assertEquals(SequenceType.dna, configuration.getSequenceType());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#setSequenceType(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetSequenceType() {
		configuration.setSequenceType(SequenceType.rna);
		assertEquals(SequenceType.rna, configuration.getSequenceType());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean#equals(java.lang.Object)}
	 * .
	 */
	@Test
	public void testEqualsObject() {
		assertTrue(configuration.equals(configuration));
		assertTrue(configuration.equals(new SequenceFileActivityConfigurationBean()));
		assertFalse(configuration.equals(null));
		configuration.setFileFormat(FileFormat.embl);
		assertFalse(configuration.equals(new SequenceFileActivityConfigurationBean()));
	}

}
