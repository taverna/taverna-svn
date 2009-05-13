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
package net.sf.taverna.t2.service.store.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import net.sf.taverna.t2.service.model.Identifiable;
import net.sf.taverna.t2.service.store.GenericDao;

/**
 * Implementation of {@link net.sf.taverna.t2.service.store.GenericDao} using Hibernate.
 *
 * @author David Withers
 */
public abstract class HibernateGenericDao<Bean extends Identifiable<Id>, Id extends Serializable> extends HibernateDaoSupport implements GenericDao<Bean, Id> {

	public void delete(Id id) {
		getHibernateTemplate().delete(get(id));
	}

	@SuppressWarnings("unchecked")
	public Bean get(Id id) {
		return (Bean) getHibernateTemplate().get(getBeanClass(), id);
	}

	@SuppressWarnings("unchecked")
	public Collection<Bean> getAll() {
		return getHibernateTemplate().loadAll(getBeanClass());
	}

	public void save(Bean bean) {
		if (bean.getId() != null) {
			bean.updateModified();
		}
		getHibernateTemplate().saveOrUpdate(bean);
	}

	protected abstract Class<Bean> getBeanClass();
	
}
