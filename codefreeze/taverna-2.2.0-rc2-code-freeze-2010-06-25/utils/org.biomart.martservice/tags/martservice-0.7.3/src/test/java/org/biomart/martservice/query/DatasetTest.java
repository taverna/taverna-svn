/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: DatasetTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/01/31 14:12:10 $
 *               by   $Author: davidwithers $
 * Created on 03-May-2006
 *****************************************************************/
package org.biomart.martservice.query;

import junit.framework.TestCase;

/**
 * 
 * @author David Withers
 */
public class DatasetTest extends TestCase {
	private String attributeName;

	private Attribute attribute;

	private String filterName;

	private String filterValue;

	private Filter filter;

//	private Link link;

	private String datasetName;

	private Dataset dataset;

	private Query query;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		attributeName = "attribute name";
		attribute = new Attribute(attributeName);

		filterName = "filter name";
		filterValue = "filter value";
		filter = new Filter(filterName, filterValue);

//		link = new Link("source", "target", "id");

		datasetName = "dataset name";
		dataset = new Dataset(datasetName);
		
		query = new Query("default");
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.Dataset(String)'
	 */
	public final void testDatasetString() {
		Dataset dataset = new Dataset(datasetName);
		assertEquals("Name should be '" + datasetName + "'", dataset.getName(),
				datasetName);
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.Dataset(Dataset)'
	 */
	public final void testDatasetDataset() {
		dataset.addAttribute(attribute);
		dataset.addFilter(filter);
		Dataset copy = new Dataset(dataset);
		assertEquals("Name should be '" + datasetName + "'", copy.getName(),
				datasetName);
		assertEquals(copy.getAttributes().size(), 1);
		assertEquals(((Attribute) copy.getAttributes().get(0)).getName(),
				attribute.getName());
		assertEquals(copy.getFilters().size(), 1);
		assertEquals(((Filter) copy.getFilters().get(0)).getName(), filter
				.getName());
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.getName()'
	 */
	public final void testGetName() {
		assertEquals("Name should be '" + datasetName + "'", dataset.getName(),
				datasetName);
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.setName(String)'
	 */
	public final void testSetName() {
		String newName = "new dataset name";
		filter.setName(newName);
		assertEquals("Name should be '" + newName + "'", filter.getName(),
				newName);
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.getAttributes()'
	 */
	public final void testGetAttributes() {
		assertEquals(dataset.getAttributes().size(), 0);
		dataset.addAttribute(attribute);
		assertEquals(dataset.getAttributes().size(), 1);
		assertEquals(dataset.getAttributes().get(0), attribute);
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.addAttribute(Attribute)'
	 */
	public final void testAddAttribute() {
		assertTrue(dataset.addAttribute(attribute));
		assertFalse(dataset.addAttribute(attribute));
		assertEquals(dataset.getAttributes().size(), 1);
		assertEquals(dataset.getAttributes().get(0), attribute);
		query.addDataset(dataset);
		assertTrue(dataset.addAttribute(new Attribute("new attribute")));
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.addAttributes(Attribute[])'
	 */
	public final void testAddAttributes() {
		dataset.addAttributes(new Attribute[] {});
		assertEquals(dataset.getAttributes().size(), 0);
		dataset.addAttributes(new Attribute[] { attribute });
		assertEquals(dataset.getAttributes().size(), 1);
		assertEquals(dataset.getAttributes().get(0), attribute);
		Attribute anotherAttribute = new Attribute("another attribute");
		dataset.addAttributes(new Attribute[] { attribute, anotherAttribute });
		assertEquals(dataset.getAttributes().size(), 2);
		assertEquals(dataset.getAttributes().get(0), attribute);
		assertEquals(dataset.getAttributes().get(1), anotherAttribute);
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.hasAttribute(Attribute)'
	 */
	public final void testHasAttribute() {
		assertFalse(dataset.hasAttribute(attribute));
		dataset.addAttribute(attribute);
		assertTrue(dataset.hasAttribute(attribute));
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.hasAttributes()'
	 */
	public final void testHasAttributes() {
		assertFalse(dataset.hasAttributes());
		dataset.addAttribute(attribute);
		assertTrue(dataset.hasAttributes());
		dataset.removeAttribute(attribute);
		assertFalse(dataset.hasAttributes());
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.removeAttribute(Attribute)'
	 */
	public final void testRemoveAttribute() {
		assertFalse(dataset.removeAttribute(attribute));
		dataset.addAttribute(attribute);
		assertTrue(dataset.removeAttribute(attribute));
		assertEquals(dataset.getAttributes().size(), 0);
		query.addDataset(dataset);
		dataset.addAttribute(attribute);
		assertTrue(dataset.removeAttribute(attribute));
	}

	public void testRemoveAllAttributes() {
		dataset.removeAllAttributes();
		assertFalse(dataset.hasAttributes());
		dataset.addAttribute(attribute);
		dataset.removeAllAttributes();
		assertFalse(dataset.hasAttributes());
		dataset.addAttributes(new Attribute[] { attribute, new Attribute("new attribute") });
		dataset.removeAllAttributes();
		assertFalse(dataset.hasAttributes());
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.getFilters()'
	 */
	public final void testGetFilters() {
		assertEquals(dataset.getFilters().size(), 0);
		dataset.addFilter(filter);
		assertEquals(dataset.getFilters().size(), 1);
		assertEquals(dataset.getFilters().get(0), filter);
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.addFilter(Filter)'
	 */
	public final void testAddFilter() {
		assertTrue(dataset.addFilter(filter));
		assertFalse(dataset.addFilter(filter));
		assertEquals(dataset.getFilters().size(), 1);
		assertEquals(dataset.getFilters().get(0), filter);
		query.addDataset(dataset);
		assertTrue(dataset.addFilter(new Filter("new filter")));
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.addFilters(Filter[])'
	 */
	public final void testAddFilters() {
		dataset.addFilters(new Filter[] {});
		assertEquals(dataset.getFilters().size(), 0);
		dataset.addFilters(new Filter[] { filter });
		assertEquals(dataset.getFilters().size(), 1);
		assertEquals(dataset.getFilters().get(0), filter);
		Filter anotherFilter = new Filter("another filter");
		dataset.addFilters(new Filter[] { filter, anotherFilter });
		assertEquals(dataset.getFilters().size(), 2);
		assertEquals(dataset.getFilters().get(0), filter);
		assertEquals(dataset.getFilters().get(1), anotherFilter);
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.hasFilter(Filter)'
	 */
	public final void testHasFilter() {
		assertFalse(dataset.hasFilter(filter));
		dataset.addFilter(filter);
		assertTrue(dataset.hasFilter(filter));
		dataset.removeFilter(filter);
		assertFalse(dataset.hasFilters());
	}

	/*
	 * Test method for 'org.biomart.martservice.query.Dataset.hasFilters()'
	 */
	public final void testHasFilters() {
		assertFalse(dataset.hasFilters());
		dataset.addFilter(filter);
		assertTrue(dataset.hasFilters());
	}

	/*
	 * Test method for
	 * 'org.biomart.martservice.query.Dataset.removeFilter(Filter)'
	 */
	public final void testRemoveFilter() {
		assertFalse(dataset.removeFilter(filter));
		dataset.addFilter(filter);
		assertTrue(dataset.removeFilter(filter));
		assertEquals(dataset.getFilters().size(), 0);
		query.addDataset(dataset);
		dataset.addFilter(filter);
		assertTrue(dataset.removeFilter(filter));
	}

	public void testRemoveAllFilters() {
		dataset.removeAllFilters();
		assertFalse(dataset.hasFilters());
		dataset.addFilter(filter);
		dataset.removeAllFilters();
		assertFalse(dataset.hasFilters());
		dataset.addFilters(new Filter[] { filter, new Filter("new filter") });
		dataset.removeAllFilters();
		assertFalse(dataset.hasFilters());
	}

	public void testGetContainingQuery() {
		assertNull(dataset.getContainingQuery());
		query.addDataset(dataset);
		assertEquals(dataset.getContainingQuery(), query);
	}

	public void testSetContainingQuery() {
		dataset.setContainingQuery(query);
		assertEquals(dataset.getContainingQuery(), query);
		dataset.setContainingQuery(null);
		assertNull(dataset.getContainingQuery());
	}

}
