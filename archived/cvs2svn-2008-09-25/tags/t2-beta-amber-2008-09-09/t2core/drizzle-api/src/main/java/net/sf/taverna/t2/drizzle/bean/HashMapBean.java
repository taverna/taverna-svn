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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author alanrw
 *
 */
public class HashMapBean<K,V> {
	private List<HashMapEntryBean<K,V>> entry = new ArrayList<HashMapEntryBean<K,V>>();
    public HashMapBean (Map<K,V> map) {
        for( Map.Entry<K,V> e : map.entrySet() )
            this.entry.add(new HashMapEntryBean<K,V>(e));
    }
    public HashMapBean() {
    	
    }
	/**
	 * @return the entry
	 */
	public synchronized final List<HashMapEntryBean<K, V>> getEntry() {
		return this.entry;
	}
	/**
	 * @param entry the entry to set
	 */
	public synchronized final void setEntry(List<HashMapEntryBean<K, V>> entry) {
		this.entry = entry;
	}
}
