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
package net.sf.taverna.t2.service.store.memory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.service.model.Identifiable;
import net.sf.taverna.t2.service.store.GenericDao;

/**
 * Implementation of {@link net.sf.taverna.t2.service.store.GenericDao} using in-memory storage.
 *
 * @author David Withers
 */
public abstract class InMemoryGenericDao<Bean extends Identifiable<Id>, Id extends Serializable> implements GenericDao<Bean, Id> {

	private Map<Id, Bean> store = new HashMap<Id, Bean>();
	
	public void delete(Id id) {
		store.remove(id);
	}

	public Bean get(Id id) {
		return store.get(id);
	}

	public Collection<Bean> getAll() {
		return store.values();
	}

	public void save(Bean bean) {
		if (bean.getId() == null) {
			bean.setId(createId());
		} else {
			bean.updateModified();
		}
		store.put(bean.getId(), bean);
	}

	public void setStore(Map<Id, Bean> store) {
		this.store = store;
	}

	public abstract Id createId();
	
}
