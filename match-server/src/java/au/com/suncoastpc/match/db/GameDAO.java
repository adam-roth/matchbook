package au.com.suncoastpc.match.db;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.db.User;

public class GameDAO {
	private static final Logger LOG = Logger.getLogger(GameDAO.class);
	
	public static Game findByCreatorAndApp(User creator, String app, EntityManager em) {
		Game result = null;
		
		try {
			Query query = em.createNamedQuery("Game.findByCreatorAndApp");
			query.setParameter("creator", creator);
			query.setParameter("app", app);
			result = (Game)query.getSingleResult();
		}
		catch (NoResultException ignored) {
			//expected, the user may not exist
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find game with app=" + app, unexpected);
		}
		
		return result;
	}
	
	public static Game findByAppAndPrivateKey(String app, String privateKey, EntityManager em) {
		Game result = null;
		
		try {
			Query query = em.createNamedQuery("Game.findByAppAndPrivateKey");
			query.setParameter("app", app);
			query.setParameter("privateKey", privateKey);
			result = (Game)query.getSingleResult();
		}
		catch (NoResultException ignored) {
			//expected, the user may not exist
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find game with app=" + app, unexpected);
		}
		
		return result;
	}
}
