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

/**
 * This should really deal with ObjectBean rather than Object. It is not clear
 * how this should be done.
 * 
 * @author alanrw
 * 
 */
/**
 * @author alanrw
 *
 * @param <O> The object class that can be contained by the PropertiedObjectSet
 */
public class PropertiedObjectSetBean <O>{
	HashMapBean<Object, PropertiedObjectBean> propertiedObjectMap;
	
	/**
	 * 
	 */
	public PropertiedObjectSetBean () {
		super();
		this.propertiedObjectMap = new HashMapBean<Object, PropertiedObjectBean>();
	}

	/**
	 * @return the propertiedObjectMap
	 */
	public HashMapBean<Object, PropertiedObjectBean> getPropertiedObjectMap() {
		return this.propertiedObjectMap;
	}

	/**
	 * @param propertiedObjectMap the propertiedObjectMap to set
	 */
	public void setPropertiedObjectMap(
			HashMapBean<Object, PropertiedObjectBean> propertiedObjectMap) {
		this.propertiedObjectMap = propertiedObjectMap;
	}

}
