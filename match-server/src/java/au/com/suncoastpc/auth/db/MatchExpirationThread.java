package au.com.suncoastpc.auth.db;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import au.com.suncoastpc.match.db.MatchRequest;
import au.com.suncoastpc.match.db.MatchRequestDAO;

public class MatchExpirationThread extends Thread {
	private static final Logger LOG = Logger.getLogger(MatchExpirationThread.class);
	
	private static final long MAX_DURATION_MILLIS = 1000 * 60 * 3;  //a match will be discarded if it sits idle for more than 3 minutes;

	@Override
	public void run() {
		//FIXME:  should be possible to gracefully stop thread
		while (true) {
			try {
				Thread.sleep(MAX_DURATION_MILLIS / 6);
				EntityManager em = DatabaseUtil.getEntityManager();
				List<MatchRequest> expired = MatchRequestDAO.findRequestsUpdatedBeforeTime(System.currentTimeMillis() - MAX_DURATION_MILLIS, em);
				
				em.getTransaction().begin();
				for (MatchRequest expire : expired) {
					LOG.info("Expiring request:  id=" + expire.getId());
					em.remove(expire);
				}
				em.getTransaction().commit();
			}
			catch (Exception e) {
				LOG.warn("Unexpected exception in match expiration thread!", e);
			}
		}
	}
}