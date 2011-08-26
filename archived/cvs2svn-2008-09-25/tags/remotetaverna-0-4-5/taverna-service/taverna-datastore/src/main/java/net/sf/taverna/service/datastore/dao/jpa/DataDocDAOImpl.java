package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.dao.DataDocDAO;

public class DataDocDAOImpl extends GenericDaoImpl<DataDoc, String> implements DataDocDAO {

	public DataDocDAOImpl(EntityManager em) {
		super(DataDoc.class, em);
	}
	
	@Override
	public String namedQueryAll() {
		return DataDoc.NAMED_QUERY_ALL;
	}
}

