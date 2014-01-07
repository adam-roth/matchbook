package au.com.suncoastpc.auth.db;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

public class UserDAO {
	private static final Logger LOG = Logger.getLogger(UserDAO.class);
	
	public static User findByEmail(String email, EntityManager em) {
		User result = null;
		
		try {
			Query query = em.createNamedQuery("User.findByEmail");
			query.setParameter("email", email.toLowerCase());
			result = (User)query.getSingleResult();
		}
		catch (NoResultException ignored) {
			//expected, the user may not exist
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find user with e-mail=" + email);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<User> findAll(EntityManager em) {
		Collection<User> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("User.findAll");
			result = query.getResultList();
		}
		catch (Exception e) {
			LOG.error("Unexpected database error!", e);
		}
		
		return result;
	}
	
	public static Long countAll(EntityManager em) {
		Long result = 0L;
		
		try {
			Query query = em.createNamedQuery("User.countAll");
			result = (Long)query.getSingleResult();
		}
		catch (NoResultException ignored) {
			//just return 0
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected database error!", unexpected);
		}
		
		return result;
	}
}
