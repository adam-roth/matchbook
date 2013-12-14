package au.com.suncoastpc.auth.db;

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
}
