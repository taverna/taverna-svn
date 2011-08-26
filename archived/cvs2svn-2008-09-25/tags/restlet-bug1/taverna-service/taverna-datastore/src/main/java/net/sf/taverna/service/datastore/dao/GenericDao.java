package net.sf.taverna.service.datastore.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public interface GenericDao<Bean, PrimaryKey extends Serializable> extends Iterable<Bean>  {

	public void create(Bean bean);

	public Bean read(PrimaryKey id);
	
	public void update(Bean bean);

	public void delete(Bean bean);

	public void refresh(Bean bean);
	
	public List<Bean> all();
	
	public Iterator<Bean> iterator();
	
}
