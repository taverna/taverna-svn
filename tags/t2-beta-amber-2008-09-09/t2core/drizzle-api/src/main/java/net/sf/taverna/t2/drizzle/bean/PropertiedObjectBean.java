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
package net.sf.taverna.t2.drizzle.bean;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * It is deliberate that the object to which the beaned PropertiedObject
 * corresponds is not visible. This is because PropertiedObjectBean should only
 * be visible under PropertiedObjectSetBean. It is only public because it is
 * seen by PropertiedObject.
 * 
 * @author alanrw
 * 
 */
@XmlRootElement()
public class PropertiedObjectBean {
	private HashMapBean<PropertyKey, PropertyValue> properties;

	/**
	 * @return the properties
	 */
	@XmlAnyElement
	public HashMapBean<PropertyKey, PropertyValue> getProperties() {
		return this.properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(
			HashMapBean<PropertyKey, PropertyValue> properties) {
		this.properties = properties;
	}

}
