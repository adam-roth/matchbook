package au.com.suncoastpc.auth.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.Configuration;
import au.com.suncoastpc.auth.util.Constants;

/**
 * Static utility class to help with database-related tasks, such as getting the  
 * EntityManager, etc..
 * 
 * @author aroth
 */
public class DatabaseUtil {
	private static final Logger LOG = Logger.getLogger(DatabaseUtil.class);
	
	private static Map<String, EntityManagerFactory> factories = null;
	
	static {
		String persistenceUnit = Configuration.PERSISTENCE_UNIT;
		factories = Collections.synchronizedMap(new HashMap<String, EntityManagerFactory>());
		factories.put(persistenceUnit, Persistence.createEntityManagerFactory(persistenceUnit));
		
		LOG.info("Initialized using persistence unit:  " + persistenceUnit);
	}
	
	/**
	 * Gets an EntityManager that can be used within the specified persistence unit.
	 * 
	 * @param request the current HTTP request.
	 * 
	 * @return an EntityManager instance, bound to the specified persistence unit.
	 */
	public static EntityManager getEntityManager(HttpServletRequest request) {
		if (request.getAttribute(Constants.ENTITY_MANAGER_REQUEST_KEY) != null) {
			return (EntityManager)request.getAttribute(Constants.ENTITY_MANAGER_REQUEST_KEY);
		}
		String persistenceUnit = Configuration.PERSISTENCE_UNIT;
		EntityManagerFactory factory = factories.get(persistenceUnit);
		if (factory == null) {
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
			factories.put(persistenceUnit, factory);
		}
		EntityManager result = factory.createEntityManager();
		request.setAttribute(Constants.ENTITY_MANAGER_REQUEST_KEY, result);
		return result;
	}
	
	//not for use by any web-based methods
	public static EntityManager getEntityManager() {
		String persistenceUnit = Configuration.PERSISTENCE_UNIT;
		EntityManagerFactory factory = factories.get(persistenceUnit);
		if (factory == null) {
			factory = Persistence.createEntityManagerFactory(persistenceUnit);
			factories.put(persistenceUnit, factory);
		}
		return factory.createEntityManager();
	}
}
