package net.sf.taverna.t2.reference.impl;

import java.util.List;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.h3.ReferenceSetImpl;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateReferenceSetDao extends HibernateDaoSupport {

	public void storeOrUpdate(ReferenceSetImpl rs) {
		getHibernateTemplate().saveOrUpdate(rs);
	}
	
	@SuppressWarnings("unchecked")
	public List<ReferenceSetImpl> getReferenceSetImpl(T2Reference ref) {
		String queryString="from ReferenceSetImpl";
		return getHibernateTemplate().find(queryString);
	}
	
}
