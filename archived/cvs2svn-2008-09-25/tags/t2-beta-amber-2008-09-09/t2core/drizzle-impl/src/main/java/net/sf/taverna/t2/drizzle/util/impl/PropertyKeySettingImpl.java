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
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.Comparator;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public final class PropertyKeySettingImpl implements PropertyKeySetting {
	
	private PropertyKey key;
	
	private Comparator<PropertyValue> comparator;

	/**
	 * 
	 */
	public PropertyKeySettingImpl() {
		// Nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparator<PropertyValue> getComparator() {
		return this.comparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyKey getPropertyKey() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setComparator(Comparator<PropertyValue> comparator) {
		if (comparator == null) {
			throw new NullPointerException ("comparator cannot be null"); //$NON-NLS-1$
		}
		if (this.comparator != null) {
			throw new IllegalStateException ("comparator cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.comparator = comparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPropertyKey(final PropertyKey propertyKey) {
		if (propertyKey == null) {
			throw new NullPointerException ("propertyKey cannot be null"); //$NON-NLS-1$
		}
		if (this.key != null) {
			throw new IllegalStateException ("key cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.key = propertyKey;

	}

}
