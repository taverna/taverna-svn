package net.sf.taverna.service.datastore;

import static javax.persistence.Persistence.createEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

public class EntityManagerUtil {

	public static final String PERSISTENCE_UNIT = "tavernaService";
	
	@PersistenceContext()
	public static EntityManagerFactory factory;
	
	public static synchronized EntityManager createEntityManager() {
		if (factory == null) {
			factory = createEntityManagerFactory(PERSISTENCE_UNIT);
		}
		return factory.createEntityManager();
	}
	
	public static synchronized void close() {
		if (factory != null) {
			factory.close();
		}
		factory = null;
	}
}
