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
package net.sf.taverna.t2.cloudone.peer;

import java.util.Map;

import net.sf.taverna.t2.cloudone.peer.LocationalContext;
/**
 * Implementation of {@link LocationalContext}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class LocationalContextImpl implements LocationalContext {

	private Map<String, String> map;
	private String type;
	
	public LocationalContextImpl(String type, Map<String, String> map) {
		this.type = type;
		this.map = map;
	}
	
	public String getContextType() {
		return type;
	}

	public String getValue(String... keyPath) {
		String key = null;
		for (String keyPart : keyPath) {
			if (keyPart.equals("")) {
				throw new IllegalArgumentException("Key can't be empty");
			}
			if (key == null) {
				key = keyPart;
			} else {
				key = key + "." + keyPart;
			}
		}
		if (key == null) {
			throw new IllegalArgumentException("At least one key is needed");
		}
		return map.get(key);
	}
	
	@Override
	public String toString() {
		// FIXME: Should have some unique identifier instead of printing full map..
		return getClass().getSimpleName() + " (" + getContextType() + "): " + map;
	}


}
