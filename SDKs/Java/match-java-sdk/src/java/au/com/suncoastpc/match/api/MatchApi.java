package au.com.suncoastpc.match.api;

import java.util.Map;

import au.com.suncoastpc.match.util.StringUtilities;

//XXX:  package access intentional
enum MatchApi {
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
	AUTO_MATCH("autoMatch"),
	CANCEL_MATCH("cancelMatch"),
	HOST_MATCH("hostMatch"),
	JOIN_MATCH("joinMatch"),
	LIST_PROXIES("listWaitingProxies"),
	PING_MATCH("pingMatch"),
	PLAYER_JOINED("playerJoined"),
	PLAYER_LEFT("playerLeft"),
	REQUEST_PROXY("requestProxiedConnection"),
	START_MATCH("startMatch");
	
	
	private String methodName;
	
	private MatchApi(String name) {
		this.methodName = name;
	}

	public String getMethodName() {
		return methodName;
	}
	
	public String getApiUrl(String serverBase) {
		return serverBase + (serverBase.endsWith("/") ? "" : "/") + this.getMethodName() + "?format=json";
	}
	
	public String getApiUrl(String serverBase, String queryParams) {
		if (StringUtilities.isEmpty(queryParams)) {
			return this.getApiUrl(serverBase);
		}
		return serverBase + (serverBase.endsWith("/") ? "" : "/") + this.getMethodName() + "?format=json&" + queryParams;
	}
	
	public String getApiUrl(String serverBase, Map<String, String> queryParams) {
		String queryString = "";
		for (String key : queryParams.keySet()) {
			if (! StringUtilities.isEmpty(queryParams.get(key))) {
				if (! "".equals(queryString)) {
					queryString += "&";
				}
				queryString += key + "=" + queryParams.get(key);
			}
		}
		
		return getApiUrl(serverBase, queryString);
	}
}
