package net.sf.taverna.service.datastore.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.datastore.bean.AbstractBean;
import net.sf.taverna.service.datastore.dao.jpa.GenericDaoImpl;

/**
 * Generic DAO (Data Access Object) interface for storing and retrieving beans
 * to/from a database.
 * 
 * @see GenericDaoImpl
 * @author Stian Soiland
 * @param <Bean>
 *            The {@link AbstractBean} subclass that the spesialisation stores
 * @param <PrimaryKey>
 *            The primary key to identify the bean.
 */
public interface GenericDao<Bean extends AbstractBean<PrimaryKey>, PrimaryKey extends Serializable> extends Iterable<Bean>  {

	/**
	 * Store the newly created bean in the database. 
	 * 
	 * @param bean The bean to store
	 */
	public void create(Bean bean);

	/**
	 * Read a bean from the database using the primary key. The primary key is
	 * what is accessible at Bean.getId().
	 * <p>
	 * Note that within a transaction (normally started for each thread) the
	 * same bean will be returned on subsequent calls with the same key.
	 * 
	 * @param id The primary key of the bean
	 * @return The bean loaded from the Entity manager
	 */
	public Bean read(PrimaryKey id);
	
	/**
	 * Update the bean in the database. Note that you also have to call
	 * {@link DAOFactory#commit()} to commit the changes.
	 * 
	 * @param bean The bean that has been updated
	 */
	public void update(Bean bean);

	/**
	 * Remove the bean from the database. This will normally involve deleting
	 * the row from the database. Note that you will have to call
	 * {@link #delete(AbstractBean)} for each referenced bean that is also to be
	 * deleted.
	 * 
	 * @param bean The bean to be deleted.
	 */
	public void delete(Bean bean);

	
	/**
	 * Freshen the bean with the database. Any changes you have made will be
	 * merged with the changes made in the database. If you want a fresh copy,
	 * use {@link #reread(AbstractBean)} instead.
	 * <p>
	 * Some implementations might not be able to modify the passed bean and
	 * could return a new instance of the bean. Therefore always use the returned
	 * value after a refresh.
	 * 
	 * @see #refresh(AbstractBean)
	 * @param bean
	 *            The bean to be refreshed
	 * @return The refreshed bean with merged changes
	 */
	public Bean refresh(Bean bean);
	
	/**
	 * Reload a bean from the database. This is normally equivalent to
	 * <code>read(bean.getId())</code>. The bean passed in is normally not
	 * affected by this call. Any uncommitted changes to the passed bean will
	 * not be reflected in the returned bean.
	 * <p>
	 * This method can be useful to pass a bean from one transaction to another
	 * as it will only read {@link Bean#getId()} which shouldn't require a
	 * database connection.
	 * 
	 * @param bean
	 *            The bean to re-read
	 * @return The bean as read fresh from the database.
	 */
	public Bean reread(Bean bean);

	/**
	 * List all the bean of the database. Unless your implementation explicitly
	 * orders the results there is no guarantee of the order of the beans.
	 * 
	 * @return The list of all beans stored in the database.
	 */
	public List<Bean> all();

	/**
	 * Iterate over the beans of the database. This is often equivalent to
	 * {@link #all()}.iterator(), but some implementations might do this more
	 * efficient. In addition this makes the DAO {@link Iterable}.
	 * 
	 * @return An iterator over the beans
	 */
	public Iterator<Bean> iterator();
	
}
