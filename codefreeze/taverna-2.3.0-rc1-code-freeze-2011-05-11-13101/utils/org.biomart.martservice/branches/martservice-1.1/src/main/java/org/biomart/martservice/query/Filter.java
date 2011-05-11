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
 * Filename           $RCSfile: Filter.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/01/31 14:12:09 $
 *               by   $Author: davidwithers $
 * Created on 26-Apr-2006
 *****************************************************************/
package org.biomart.martservice.query;

/**
 * Class for creating filter elements of mart queries.
 * 
 * @author David Withers
 */
public class Filter {
	private String name;

	private String value;

	private boolean booleanFilter;

	private boolean listFilter;

	private Dataset containingDataset;

	/**
	 * Constructs an instance of a <code>Filter</code> with the specified name.
	 * 
	 * @param name
	 *            the name of the <code>Filter</code>; must not be <code>null</code>
	 */
	public Filter(String name) {
		this(name, null);
	}

	/**
	 * Constructs a instance of a <code>Filter</code> with the specified name
	 * and value.
	 * 
	 * @param name
	 *            the name of the <code>Filter</code>; must not be <code>null</code>
	 * @param value
	 *            the value of the <code>Filter</code>
	 */
	public Filter(String name, String value) {
		this(name, value, false);
	}

	/**
	 * Constructs a instance of a <code>Filter</code> with the specified name
	 * and value, and sets a flag to indicate if this is a boolean
	 * <code>Filter</code>.
	 * 
	 * @param name
	 *            the name of the <code>Filter</code>; must not be <code>null</code>
	 * @param value
	 *            the value of the <code>Filter</code>
	 * @param booleanFilter
	 */
	public Filter(String name, String value, boolean booleanFilter) {
		if (name == null) {
			throw new IllegalArgumentException("Parameter 'name' must not be null");
		}
		this.name = name;
		this.value = value;
		this.booleanFilter = booleanFilter;
	}

	/**
	 * Constructs an instance of a <code>Filter</code> which is a copy of
	 * another <code>Filter</code>.
	 * 
	 * @param filter
	 *            the <code>Filter</code> to copy; must not be <code>null</code>
	 */
	public Filter(Filter filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Parameter 'filter' must not be null");
		}
		this.name = filter.name;
		this.value = filter.value;
		this.booleanFilter = filter.booleanFilter;
	}

	/**
	 * Returns the name of the Filter.
	 * 
	 * @return the name of the Filter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Filter.
	 * 
	 * @param name
	 *            the name of the Filter; must not be <code>null</code>
	 */
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Parameter 'name' must not be null");
		}
		this.name = name;
	}

	/**
	 * Returns the qualified name of this <code>Filter</code>.
	 * 
	 * The qualified name is <code>containingDatasetName.filterName</code> or
	 * just <code>attributeName</code> if the <code>Filter</code> is not in
	 * a <code>Dataset</code>.
	 * 
	 * @return the qualified name of this <code>Filter</code>.
	 */
	public String getQualifiedName() {
		if (containingDataset == null) {
			return name;
		} else {
			return containingDataset.getName() + "." + getName();
		}
	}

	/**
	 * Returns the <code>Dataset</code> containing this <code>Filter</code>
	 * or null if it is not in a <code>Dataset</code>.
	 * 
	 * @return the <code>Dataset</code> containing this <code>Filter</code>
	 *         or null if it is not in a <code>Dataset</code>
	 */
	public Dataset getContainingDataset() {
		return containingDataset;
	}

	/**
	 * Sets the containing <code>Dataset</code>.
	 * 
	 * @param dataset
	 *            the containing <code>Dataset</code>
	 */
	void setContainingDataset(Dataset dataset) {
		this.containingDataset = dataset;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            the value to set.
	 */
	public void setValue(String value) {
		boolean valueChanged = false;
		if (this.value == null) {
			if (value != null) {
				this.value = value;
				valueChanged = true;
			}
		} else if (!this.value.equals(value)) {
			this.value = value;
			valueChanged = true;
		}
		if (valueChanged) {
			if (containingDataset != null) {
				if (containingDataset.getContainingQuery() != null) {
					containingDataset.getContainingQuery().fireFilterChanged(
							this, containingDataset);
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if this is a boolean filter.
	 * 
	 * @return <code>true</code> if this is a boolean filter.
	 */
	public boolean isBoolean() {
		return booleanFilter;
	}

	/**
	 * Sets the booleanFilter flag.
	 * 
	 * @param booleanFilter
	 */
	public void setBoolean(boolean booleanFilter) {
		this.booleanFilter = booleanFilter;
	}

	/**
	 * Returns <code>true</code> if this is a list filter.
	 * 
	 * @return <code>true</code> if this is a list filter.
	 */
	public boolean isList() {
		return listFilter;
	}

	/**
	 * Sets the listFilter flag.
	 * 
	 * @param listFilter
	 */
	public void setList(boolean listFilter) {
		this.listFilter = listFilter;
	}

}
