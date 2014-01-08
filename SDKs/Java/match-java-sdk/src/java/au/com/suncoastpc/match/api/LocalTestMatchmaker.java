package au.com.suncoastpc.match.api;

import java.util.Map;

import org.json.simple.JSONObject;

import au.com.suncoastpc.match.util.StringUtilities;

/**
 * A Matchmaker that allows multiple distinct client UUID's within a single running JVM instance.  This can be used to
 * test multi-client communication and networking without needing multiple test devices or running JVM instances.
 * 
 * This class is NOT for use in any production application.  It is for testing only.
 * 
 * @author Adam
 */
public class LocalTestMatchmaker extends Matchmaker {

	private String customUuid;
	
	public LocalTestMatchmaker(String uuid, String app, String secret, MatchmakerClient client) {
		super(uuid, app, secret, client);
		this.customUuid = null;
	}
	
	public LocalTestMatchmaker(String uuid, String app, String secret, MatchmakerClient client, String customUuid) {
		super(uuid, app, secret, client);
		this.customUuid = customUuid;
	}
	
	public String getCustomUuid() {
		return customUuid;
	}

	public void setCustomUuid(String customUuid) {
		this.customUuid = customUuid;
	}

	@Override
	public synchronized Match joinPrivateMatch(String password, String gameOptions) {
		if (this.uuid.equalsIgnoreCase(this.customUuid) || this.customUuid == null) {
			return super.joinPrivateMatch(password, gameOptions);
		}
		
		Match result = null;
		
		JSONObject serverMatch = this.joinMatch(password, gameOptions);
		if (this.successfulRequest(serverMatch)) {
			//set up the match
			//{"port":"24","id":"11","token":"12345","status":"success","addr":"127.0.0.1","localAddr":""}
			boolean connected = false;
			result = new Match(customUuid, this, serverMatch.get("id").toString());
			result.setPassword(password);
			
			String port = serverMatch.get("port").toString();
			String serverUuid = serverMatch.get("token").toString();
			if (! StringUtilities.isEmpty(serverMatch.get("localAddr").toString())) {
				connected = result.connectToServer(serverMatch.get("localAddr").toString(), port, serverUuid);
			}
			if (! connected) {
				connected = result.connectToServer(serverMatch.get("addr").toString(), port, serverUuid);
			}
			
			//if still not connected, then we can't join the match; return null
			result = connected ? result : null;
		}
		
		return result;
	}
	
	@Override
	synchronized JSONObject joinMatch(String pass, String gameOptions) {
		if (this.uuid.equalsIgnoreCase(this.customUuid) || this.customUuid == null) {
			return super.joinMatch(pass, gameOptions);
		}
		
		Map<String, String> params = getDefaultParams();
		params.put("pass", pass);
		params.put("uuid", customUuid);
		params.put("gameOptions", gameOptions);
		
		return doRequest(MatchApi.JOIN_MATCH, params);
	}
	
	
	@Override
	synchronized JSONObject autoMatch(int numPlayers, String gameOptions) {
		if (this.uuid.equalsIgnoreCase(this.customUuid) || this.customUuid == null) {
			return super.autoMatch(numPlayers, gameOptions);
		}
		
		Map<String, String> params = getDefaultParams();
		params.put("gameOptions", gameOptions);
		params.put("uuid", customUuid);
		
		if (numPlayers > 1) {
			params.put("maxPlayers", Integer.toString(numPlayers));
		}
		
		return doRequest(MatchApi.AUTO_MATCH, params);
	}
	
	@Override
	public synchronized Match autoJoinMatch(int numPlayers, boolean createIfNecessary, String gameOptions) {
		if (this.uuid.equalsIgnoreCase(this.customUuid) || this.customUuid == null) {
			return super.autoJoinMatch(numPlayers, createIfNecessary, gameOptions);
		}
		
		Match result = null;
		
		JSONObject autoMatch = this.autoMatch(numPlayers, gameOptions);
		if (this.successfulRequest(autoMatch)) {
			//set up the match
			//{"port":"24","id":"11","token":"12345","status":"success","addr":"127.0.0.1","localAddr":""}
			boolean connected = false;
			String port = autoMatch.get("port").toString();
			String serverUuid = autoMatch.get("token").toString();
			result = new Match(customUuid, this, autoMatch.get("id").toString());
			if (! StringUtilities.isEmpty(autoMatch.get("localAddr").toString())) {
				connected = result.connectToServer(autoMatch.get("localAddr").toString(), port, serverUuid);
			}
			if (! connected) {
				connected = result.connectToServer(autoMatch.get("addr").toString(), port, serverUuid);
			}
			
			//if still not connected, then we can't join the match; return null
			result = connected ? result : null;
		}
		
		return result;
	}
}
