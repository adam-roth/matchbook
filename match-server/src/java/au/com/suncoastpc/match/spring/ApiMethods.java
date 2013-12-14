package au.com.suncoastpc.match.spring;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.MatchExpirationThread;
import au.com.suncoastpc.auth.spring.base.BaseMethods;
import au.com.suncoastpc.auth.util.Configuration;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.StringUtilities;
import au.com.suncoastpc.match.db.Game;
import au.com.suncoastpc.match.db.GameDAO;
import au.com.suncoastpc.match.db.MatchRequest;
import au.com.suncoastpc.match.db.MatchRequestDAO;

//FIXME:  need a thread that times out inactive matches periodically
public class ApiMethods extends BaseMethods {
	static final Logger LOG = Logger.getLogger(ApiMethods.class);
	
	private static final MatchExpirationThread expirationThread;
	private static ProxyServer proxy;
	
	static {
		expirationThread = new MatchExpirationThread();
		expirationThread.start();
		
		try {
			proxy = new ProxyServer();
			proxy.start();
		}
		catch (Exception e) {
			LOG.error("Unexpected exception when starting proxy server; proxied connections will not be available!", e);
			proxy = null;
		}
	}
	
	//autoMatch:  		join an existing game if one is available; if none available recieve a status code stating so
	//hostMatch:  		register as a host for a game, can be used after automatch fails to create a new game; the game created may be public or private
	//joinMatch:  		join a specific match, identified by password
	//pingMatch:		notify the server that the given match is still active and should not be timed out; should be called while the match is waiting for players (can only be called by the match creator)
	//startMatch:		notify the server that the given match is starting and no more players can join (can only be called by the match creator)
	//cancelMatch:		notify the server that the given match has been canceled and should be abandoned
	//playerJoined:  	notifies the server that a player has successfully joined the match
	//playerLeft:		notifies the server the a player has left the match
	//requestProxiedConnection:		request a server-proxied connection to a game; requires app, secret, and matchId
	//listWaitingProxies:			request the number of proxied connections waiting for achnowledgement from the server; requires app, secret, matchId, and uuid
	
	//tells the server that someone has left a match, only the match creator can do this
	public ModelAndView playerLeft(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//FIXME:  should synchronize this on the match
		
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		String leftPlayer = request.getParameter("playerUuid").replaceAll("\\|", "~~");
		long matchId = Long.parseLong(request.getParameter("matchId"));
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		MatchRequest match = em.find(MatchRequest.class, matchId);
		if (match != null && match.getDeviceToken().equals(hostToken) && match.getDeviceAddress().equals(hostIp) && ! match.getDeviceToken().equals(leftPlayer)) {
			//valid request, refresh the match
			em.getTransaction().begin();
			if (match.getJoinedPlayers() != null && match.getJoinedPlayers().contains(leftPlayer)) {
				String newPlayers = "";
				String[] players = match.getJoinedPlayers().split("\\|");
				for (String player : players) {
					if (! player.equals(leftPlayer)) {
						if (! "".equals(newPlayers)) {
							newPlayers += "|";
						}
						newPlayers += player;
					}
				}
				match.setJoinedPlayers("".equals(newPlayers) ? null : newPlayers);
				match.setCurrentNumPlayers(match.getCurrentNumPlayers() - 1);
				match.setLastUpdated(System.currentTimeMillis());
			}
			else {
				request.setAttribute("error", "Player already left");
			}
			
			em.merge(match);
			em.getTransaction().commit();
		}
		else {
			request.setAttribute("error", "Invalid match");
		}
		
		return new ModelAndView("ping");  //XXX:  reuse the view because no real data needs to go back for this
	}
	
	//tells the server that someone has joined a match (if enough players join, the server will stop assigning new players to the match), only the match creator can do this
	public ModelAndView playerJoined(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//FIXME:  should synchronize this on the match
		
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		String joinedPlayer = request.getParameter("playerUuid").replaceAll("\\|", "~~");
		long matchId = Long.parseLong(request.getParameter("matchId"));
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		MatchRequest match = em.find(MatchRequest.class, matchId);
		if (match != null && match.getDeviceToken().equals(hostToken) && match.getDeviceAddress().equals(hostIp) && ! hostToken.equals(joinedPlayer)) {
			//valid request, refresh the match
			em.getTransaction().begin();
			if (match.getJoinedPlayers() == null) {
				match.setJoinedPlayers(joinedPlayer);
				match.setCurrentNumPlayers(match.getCurrentNumPlayers() + 1);
				match.setLastUpdated(System.currentTimeMillis());
			}
			else if (! match.getJoinedPlayers().contains(joinedPlayer)) {
				match.setJoinedPlayers(match.getJoinedPlayers() + "|" + joinedPlayer);
				match.setCurrentNumPlayers(match.getCurrentNumPlayers() + 1);
				match.setLastUpdated(System.currentTimeMillis());
			}
			else {
				request.setAttribute("error", "Player already joined");
			}
			
			em.merge(match);
			em.getTransaction().commit();
		}
		else {
			request.setAttribute("error", "Invalid match");
		}
		
		return new ModelAndView("ping");  //XXX:  reuse the view because no real data needs to go back for this
	}
	
	//tells the server that a match has been canceled (deletes it from the server), only the match creator can do this
	public ModelAndView cancelMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		long matchId = Long.parseLong(request.getParameter("matchId"));
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		MatchRequest match = em.find(MatchRequest.class, matchId);
		if (match != null && match.getDeviceToken().equals(hostToken) && match.getDeviceAddress().equals(hostIp)) {
			//valid request, refresh the match
			em.getTransaction().begin();
			em.remove(match);
			em.getTransaction().commit();
			
			//XXX:  do we notify the connected clients?  In theory we should not have to, because the host should close all of their connections.
		}
		else {
			request.setAttribute("error", "Invalid match");
		}
		
		return new ModelAndView("ping");  //XXX:  reuse the view because no real data needs to go back for this
	}
	
	//tell the server that a match has started and no more players can join (just deletes the match from the server), only the match creator can do this
	public ModelAndView startMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		long matchId = Long.parseLong(request.getParameter("matchId"));
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		MatchRequest match = em.find(MatchRequest.class, matchId);
		if (match != null && match.getDeviceToken().equals(hostToken) && match.getDeviceAddress().equals(hostIp)) {
			//valid request, refresh the match
			em.getTransaction().begin();
			em.remove(match);
			em.getTransaction().commit();
		}
		else {
			request.setAttribute("error", "Invalid match");
		}
		
		return new ModelAndView("ping");  //XXX:  reuse the view because no real data needs to go back for this
	}
	
	//tell the server not to time out a match just yet; the caller must specify their match-id and device token, if there is a mismatch then the match is not updated
	//XXX:  could just have this happen automatically as a consequence of making other API calls
	public ModelAndView pingMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		long matchId = Long.parseLong(request.getParameter("matchId"));
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		MatchRequest match = em.find(MatchRequest.class, matchId);
		if (match != null && match.getDeviceToken().equals(hostToken) && match.getDeviceAddress().equals(hostIp)) {
			//valid request, refresh the match
			em.getTransaction().begin();
			match.setLastUpdated(System.currentTimeMillis());
			em.merge(match);
			em.getTransaction().commit();
		}
		else {
			request.setAttribute("error", "Invalid match");
		}
		
		return new ModelAndView("ping");
	}
	
	//join a private match by password
	public ModelAndView joinMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String app = request.getParameter("app");
		String privateKey = request.getParameter("secret");
		String password = request.getParameter("pass");
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		Game game = GameDAO.findByAppAndPrivateKey(app, privateKey, em);
		if (game != null) {
			//valid app, see if the game exists
			MatchRequest match = MatchRequestDAO.findByGameAndPassword(game, password, em);
			
			if (match != null) {
				//we don't actually update any state here, we send the match address to the caller; it is the host's responsibility to call back when the caller actually joins their match 
				request.setAttribute("match", match);
			}
			else {
				request.setAttribute("error", "No match available; please create one");
			}
		}
		else {
			//invalid app
			request.setAttribute("error", "Game not found");
		}
		
		return new ModelAndView("joinMatch");
	}
	
	//send an app name, private key, and game settings (optional), and receive back details on a match (or a message indicating that no appropriate matches exist)
	public ModelAndView autoMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//FIXME:  should synchronize on game/match?
		String app = request.getParameter("app");
		String hostIp = ipAddress(request);
		String hostToken = request.getParameter("uuid");
		String privateKey = request.getParameter("secret");
		String options = request.getParameter("gameOptions");
		String maxPlayers = request.getParameter("maxPlayers");
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		//make sure the user is not already hosting a match for this game
		MatchRequest match = null;
		List<MatchRequest> candidates = MatchRequestDAO.findByHostAndToken(hostIp, hostToken, em);
		for (MatchRequest candidate : candidates) {
			if (candidate.getGame().getAppIdentifier().equals(app)) {
				match = candidate;
			}
		}
		if (match != null) {
			request.setAttribute("error", "You are already hosting a match for this game, please cancel it first");
			return new ModelAndView("autoMatch");
		}
		
		Game game = GameDAO.findByAppAndPrivateKey(app, privateKey, em);
		if (game != null) {
			//valid app, see if there are any waiting games
			List<MatchRequest> matches;
			if (StringUtilities.isEmpty(maxPlayers)) {
				matches = MatchRequestDAO.findByGameWithNoPassword(game, options, em);
			}
			else {
				matches = MatchRequestDAO.findByGameAndMaxPlayersWithNoPassword(game, Integer.parseInt(maxPlayers), options, em);
			}
			if (matches.size() > 0) {
				MatchRequest result = matches.get(0);
				//we don't actually update any state here, we send the match address to the caller; it is the host's responsibility to call back when the caller actually joins their match 
				request.setAttribute("match", result);
			}
			else {
				request.setAttribute("error", "No match available; please create one");
			}
		}
		else {
			//invalid app
			request.setAttribute("error", "Game not found");
		}
		
		return new ModelAndView("autoMatch");
	}
	
	public ModelAndView requestProxiedConnection(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//FIXME:  implement a limit to the number of concurrent proxied connections allowed
		String app = request.getParameter("app");
		String privateKey = request.getParameter("secret");
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		Game game = GameDAO.findByAppAndPrivateKey(app, privateKey, em);
		if (game != null) {
			String matchId = request.getParameter("matchId");
			MatchRequest match = em.find(MatchRequest.class, Long.parseLong(matchId));
			if (proxy != null && match != null) {
				request.setAttribute("status", Constants.SUCCESS_STATUS);
				request.setAttribute("address", Configuration.getServerHostName() + ":" + proxy.getPort());
			}
			else {
				request.setAttribute("status", Constants.ERROR_STATUS);
				request.setAttribute("error", "Proxy service unavailable.");
			}
		}
		else {
			request.setAttribute("status", Constants.ERROR_STATUS);
			request.setAttribute("error", "Invalid request.");
		}
		
		return new ModelAndView("requestProxiedConnection");
	}
	
	public ModelAndView listWaitingProxies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String app = request.getParameter("app");
		String privateKey = request.getParameter("secret");
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		Game game = GameDAO.findByAppAndPrivateKey(app, privateKey, em);
		if (game != null) {
			String hostIp = ipAddress(request);
			String hostToken = request.getParameter("uuid");
			String matchId = request.getParameter("matchId");
			
			MatchRequest match = em.find(MatchRequest.class, Long.parseLong(matchId));
			if (match != null && match.getDeviceAddress().equals(hostIp) && match.getDeviceToken().equals(hostToken)) {
				int numWaiting = proxy.numberOfConnections(hostToken);
				request.setAttribute("status", Constants.SUCCESS_STATUS);
				request.setAttribute("address", Configuration.getServerHostName() + ":" + proxy.getPort());
				request.setAttribute("numWaiting", numWaiting);
			}
			else {
				request.setAttribute("status", Constants.ERROR_STATUS);
				request.setAttribute("error", "Match not found.");
			}
		}
		else {
			request.setAttribute("status", Constants.ERROR_STATUS);
			request.setAttribute("error", "Invalid request.");
		}
		
		return new ModelAndView("listWaitingProxies");
	}
	
	//create/register a new game
	public ModelAndView hostMatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//FIXME:  should synchronize on host IP
		String hostIp = ipAddress(request);
		String localIp = request.getParameter("internalIp");  //in case someone is running  the game on a local network behind a firewall or NAT, allow them to specify a network IP address that can be used to connect to them
		String hostToken = request.getParameter("uuid");
		String options = request.getParameter("gameOptions");
		int hostPort = Integer.parseInt(request.getParameter("port"));
		int maxPlayers = Integer.parseInt(request.getParameter("maxPlayers"));
		String app = request.getParameter("app");
		String privateKey = request.getParameter("secret");
		boolean privateMatch = request.getParameter("privateMatch") != null;
		EntityManager em = DatabaseUtil.getEntityManager(request);
		
		//can only host a match if no other matches are pending for this host; requiring a device UUID allows for connectivity in the case of a NAT environment
		Collection<MatchRequest> existingGames = MatchRequestDAO.findByHostAndToken(hostIp, hostToken, em);
		if (! existingGames.isEmpty()) {
			//FIXME:  allow existing games, if the app is different?
			request.setAttribute("games", existingGames);
			return new ModelAndView("existingGamesError");
		}
		
		//set up a new match
		Game game = GameDAO.findByAppAndPrivateKey(app, privateKey, em);
		if (game != null) {
			//valid app, add the match
			em.getTransaction().begin();
			
			MatchRequest match = new MatchRequest();
			match.setCurrentNumPlayers(1);
			match.setMaxNumPlayers(maxPlayers);
			match.setDeviceAddress(hostIp);
			match.setDevicePort(hostPort);
			match.setDeviceToken(hostToken);
			match.setDeviceLocalAddress(localIp);
			match.setGame(game);
			match.setGameOptions(options);
			
			if (privateMatch) {
				//if it's private, assign a randomly generated password (and ensure that nobody else is using the same password in the game)
				do {
					match.setPassword(StringUtilities.randomStringOfLength(6));
				}
				while (MatchRequestDAO.findByGameAndPassword(game, match.getPassword(), em) != null);
			}
			
			em.persist(match);
			em.getTransaction().commit();
			
			request.setAttribute("match", match);
		}
		else {
			//invalid app
			request.setAttribute("error", "Game not found");
		}
		
		return new ModelAndView("hostMatch");
	}
	
	private String ipAddress(HttpServletRequest request) {
		return request.getRemoteAddr();
	}
}
