package au.com.suncoastpc.match.db;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.StringUtilities;

public class MatchRequestDAO {
	private static final Logger LOG = Logger.getLogger(MatchRequestDAO.class);
	
	public static List<MatchRequest> findByGameWithNoPassword(Game game, String gameOptions, EntityManager em) {
		if (! StringUtilities.isEmpty(gameOptions)) {
			return findByGameAndOptionsWithNoPassword(game, gameOptions, em);
		}
		else {
			return findByGameWithNoPasswordAndNoOptions(game, em);
		}
	}
	
	public static List<MatchRequest> findByGameAndMaxPlayersWithNoPassword(Game game, int maxPlayers, String gameOptions, EntityManager em) {
		if (! StringUtilities.isEmpty(gameOptions)) {
			return findByGameAndMaxPlayersAndOptionsWithNoPassword(game, maxPlayers, gameOptions, em);
		}
		else {
			return findByGameAndMaxPlayersWithNoPasswordAndNoOptions(game, maxPlayers, em);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<MatchRequest> findRequestsUpdatedBeforeTime(long time, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findRequestsUpdatedBeforeTime");
			query.setParameter("time", time);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches older than time=" + time, unexpected);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static List<MatchRequest> findByHostAndToken(String host, String token, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByHostAndToken");
			query.setParameter("host", host);
			query.setParameter("token", token);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for host=" + host, unexpected);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static MatchRequest findByGameAndPassword(Game game, String password, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByGameAndPassword");
			query.setParameter("game", game);
			query.setParameter("password", password);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for game=" + game, unexpected);
		}
		
		return result.isEmpty() ? null : result.get(0);
	}
	
	@SuppressWarnings("unchecked")
	private static List<MatchRequest> findByGameWithNoPasswordAndNoOptions(Game game, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByGameWithNoPasswordAndNoOptions");
			query.setParameter("game", game);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for game=" + game, unexpected);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static List<MatchRequest> findByGameAndOptionsWithNoPassword(Game game, String gameOptions, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByGameAndOptionsWithNoPassword");
			query.setParameter("game", game);
			query.setParameter("options", gameOptions);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for game=" + game, unexpected);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static List<MatchRequest> findByGameAndMaxPlayersWithNoPasswordAndNoOptions(Game game, int maxPlayers, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByGameAndMaxPlayersWithNoPasswordAndNoOptions");
			query.setParameter("game", game);
			query.setParameter("maxPlayers", maxPlayers);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for game=" + game, unexpected);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static List<MatchRequest> findByGameAndMaxPlayersAndOptionsWithNoPassword(Game game, int maxPlayers, String gameOptions, EntityManager em) {
		List<MatchRequest> result = Collections.emptyList();
		
		try {
			Query query = em.createNamedQuery("MatchRequest.findByAndMaxPlayersGameAndOptionsWithNoPassword");
			query.setParameter("game", game);
			query.setParameter("maxPlayers", maxPlayers);
			query.setParameter("options", gameOptions);
			result = query.getResultList();
		}
		catch (Exception unexpected) {
			LOG.error("Unexpected exception when attempting to find matches for game=" + game, unexpected);
		}
		
		return result;
	}
}
