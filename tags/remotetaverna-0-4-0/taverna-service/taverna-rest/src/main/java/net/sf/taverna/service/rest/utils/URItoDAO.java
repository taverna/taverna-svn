package net.sf.taverna.service.rest.utils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.AbstractBean;
import net.sf.taverna.service.datastore.bean.AbstractUUID;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.GenericDao;
import net.sf.taverna.service.datastore.dao.DAOFactory.DAO;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;

/**
 * Resolve URIs to retrieve DAO beans.
 * <p>
 * Generally this class does the opposite of {@link URIFactory}. Given an URI,
 * retrieve the DAO object. For example, from
 * <code>http://localhost:8976/v1/jobs/1928319238</code>, load the {@link Job} with
 * id "1928319238".
 * 
 * @author Stian Soiland
 */
public class URItoDAO {
	private static Logger logger = Logger.getLogger(URItoDAO.class);

	private DAOFactory daoFactory = DAOFactory.getFactory();

	private URIFactory uriFactory;
	
	/**
	 * The get*DAO() methods from the {@link DAOFactory} as identified by {@link #findDAOs()}.
	 * 
	 */
	private Map<Class<?>, Method> daoMethods = findDAOs();

	/**
	 * Get an {@link URItoDAO} instance based on the given {@link URIFactory}.
	 * 
	 * @param uriFactory
	 *            The {@link URIFactory} to be used for resolving URIs
	 * @return An {@link URItoDAO} instance
	 */
	public synchronized static URItoDAO getInstance(URIFactory uriFactory) {
		return new URItoDAO(uriFactory);
	}

	/**
	 * Use {@link #getInstance()} instead
	 */
	private URItoDAO(URIFactory uriFactory) {
		this.uriFactory = uriFactory;
	}

	/**
	 * Retrieve a resource of the given class from the URI given.
	 * 
	 * @see #getResource(String, Class)
	 * @param <ResourceClass>
	 *            The class of the resource bean
	 * @param uri
	 *            The full URI to the resource
	 * @param resourceClass
	 *            The class of the resource bean
	 * @return A {@link ResourceClass} loaded from its {@link GenericDao}
	 */
	public <ResourceClass extends AbstractUUID> ResourceClass getResource(
		Reference uri, Class<ResourceClass> resourceClass) {
		return getResource(uri.getTargetRef().toString(), resourceClass);
	}

	/**
	 * Retrieve a resource of the given class from the URI given.
	 * 
	 * @param <ResourceClass>
	 *            The class of the resource bean
	 * @param uri
	 *            The full URI to the resource
	 * @param resourceClass
	 *            The class of the resource bean
	 * @return A {@link ResourceClass} loaded from the {@link DAOFactory}
	 */
	public <ResourceClass extends AbstractUUID> ResourceClass getResource(
		String uri, Class<ResourceClass> resourceClass) {
		String id = getId(uri, resourceClass);
		return daoRead(resourceClass, id);
	}

	/**
	 * Get the identifier part of the URI to a resource of the given class.
	 * Normally this is the primary key for use with
	 * {@link GenericDao#read(Serializable)}
	 * 
	 * @param <ResourceClass>
	 *            The class of the resource bean
	 * @param uri
	 *            The full URI to the resource
	 * @param resourceClass
	 *            The class of the resource bean
	 * @return The identifier of the resource
	 */
	public <ResourceClass extends AbstractUUID> String getId(String uri,
		Class<ResourceClass> resourceClass) {
		String prefix = uriFactory.getURI(resourceClass) + "/";
		logger.debug("Prefix for " + resourceClass + ": " + prefix);
		if (!uri.startsWith(prefix)) {
			logger.warn("Invalid resource URI " + uri
				+ " - did not start with " + prefix);
			throw new IllegalArgumentException("Invalid resource URI " + uri);
		}
		String id = uri.substring(prefix.length());
		return id;
	}

	/**
	 * Retrieve a resource from appropriate DAO given the resource class and
	 * primary key.
	 * 
	 * @see GenericDao#read(Serializable)
	 * @see #findDAOs()
	 * @param <ResourceClass>
	 *            Bean class
	 * @param <PrimaryKey>
	 *            The class of the primary key
	 * @param resourceClass
	 *            The class object of the bean
	 * @param id
	 *            The primary key
	 * @return The loaded {@link ResourceClass}
	 */
	@SuppressWarnings("unchecked")
	private <ResourceClass extends AbstractBean<PrimaryKey>, PrimaryKey extends Serializable> ResourceClass daoRead(
		Class<ResourceClass> resourceClass, PrimaryKey id) {

		// Special case as their URIs contain the username
		// (We do the assignable-test the other way so that this should work
		// also for Workers)
		if (User.class.isAssignableFrom(resourceClass)) {
			return resourceClass.cast(daoFactory.getUserDAO().readByUsername(
				(String) id));
		}

		Method daoMethod = daoMethods.get(resourceClass);
		if (daoMethod == null) {
			logger.error("Unknown resource class " + resourceClass);
			throw new IllegalArgumentException("Unknown resource class "
				+ resourceClass);
		}
		GenericDao<ResourceClass, PrimaryKey> dao;
		try {
			dao =
				(GenericDao<ResourceClass, PrimaryKey>) daoMethod.invoke(
					daoFactory, new Object[0]);
		} catch (IllegalArgumentException e) {
			// Note: These three exceptions generally shouldn't happen
			logger.error("Invalid arguments for DAO method " + daoMethod, e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			logger.error("Illegal access for DAO method " + daoMethod, e);
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			logger.error("Could not invoke DAO method " + daoMethod,
				e.getCause());
			throw new RuntimeException(e);
		}
		logger.debug("Finding " + id + " from dao " + dao);
		return dao.read(id);
	}

	/**
	 * Identify all the get**DAO() methods in the current factory.
	 * <p>
	 * The {@link DAOFactory} methods must be annotated using {@link DAO}.
	 * 
	 * @return A Map from DAO class (such as {@link Job}) to a {@link Method}
	 *         on the daoFactory class.
	 */
	@SuppressWarnings("unchecked")
	private Map<Class<?>, Method> findDAOs() {
		Map<Class<?>, Method> daoMethods = new HashMap<Class<?>, Method>();
		for (Method method : daoFactory.getClass().getMethods()) {
			DAO annotation = method.getAnnotation(DAO.class);
			if (annotation == null) {
				logger.debug("Skipping non-DAO method" + method.getName());
				continue;
			}
			if (method.getParameterTypes().length > 0) {
				logger.warn("Ignoring DAO method " + method
					+ ", requires parameters");
				continue;
			}
			daoMethods.put(annotation.value(), method);
			logger.debug("Registering DAO method for " + annotation.value()
				+ ": " + method);
		}
		return daoMethods;
	}
}
