package net.sf.taverna.service.rest.utils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.UUIDResource;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.GenericDao;
import net.sf.taverna.service.datastore.dao.DAOFactory.DAO;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;

public class URItoDAO {
	private static Logger logger = Logger.getLogger(URItoDAO.class);

	private static URItoDAO instance;

	private URIFactory uriFactory = URIFactory.getInstance();

	private DAOFactory daoFactory = DAOFactory.getFactory();

	private Map<Class<?>, Method> daoMethods = findDAOs();

	public static synchronized URItoDAO getInstance() {
		if (instance == null) {
			instance = new URItoDAO();
		}
		return instance;
	}

	/**
	 * Use singleton access {@link #getInstance()}
	 */
	private URItoDAO() {
	}

	public <ResourceClass extends UUIDResource> ResourceClass getResource(
		Reference uri, Class<ResourceClass> resourceClass) {
		return getResource(uri.getTargetRef().toString(), resourceClass);
	}

	public <ResourceClass extends UUIDResource> ResourceClass getResource(
		String uri, Class<ResourceClass> resourceClass) {
		String prefix = uriFactory.getURI(resourceClass) + "/";
		logger.debug("Prefix for " + resourceClass + ": " + prefix);
		if (!uri.startsWith(prefix)) {
			logger.warn("Invalid resource URI " + uri);
			throw new IllegalArgumentException("Invalid resource URI " + uri);
		}
		String id = uri.substring(prefix.length());
		return daoRead(resourceClass, id);
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
	private <ResourceClass, PrimaryKey extends Serializable> ResourceClass daoRead(
		Class<ResourceClass> resourceClass, PrimaryKey id) {
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
