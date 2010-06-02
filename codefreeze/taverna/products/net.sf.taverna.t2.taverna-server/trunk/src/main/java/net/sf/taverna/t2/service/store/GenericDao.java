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
package net.sf.taverna.t2.service.store;

import java.io.Serializable;
import java.util.Collection;

import net.sf.taverna.t2.service.model.Identifiable;

/**
 * A DAO interface for storing {@link net.sf.taverna.t2.service.model.Identifiable}s.
 *
 * @author David Withers
 * @param <Bean>
 * @param <Id>
 */
public interface GenericDao<Bean extends Identifiable<Id>, Id extends Serializable> {

	/**
	 * Returns the {@link Identifiable} bean with the specified identifier.
	 *
	 * @param id the identifier
	 * @return the {@link Identifiable} bean with the specified identifier
	 */
	public Bean get(Id id);
	
	/**
	 * Returns all the {@link Identifiable} beans.
	 *
	 * @return all the {@link Identifiable} beans
	 */
	public Collection<Bean> getAll();
	
	/**
	 * Stores the {@link Identifiable} bean.
	 *
	 * @param bean the bean to store
	 */
	public void save(Bean bean);
	
	/**
	 * Removes the {@link Identifiable} bean.
	 *
	 * @param id the id of the bean to remove.
	 */
	public void delete(Id id);
	
}
