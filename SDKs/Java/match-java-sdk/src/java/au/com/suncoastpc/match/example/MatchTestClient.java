package au.com.suncoastpc.match.example;

import org.json.simple.JSONObject;

import au.com.suncoastpc.match.api.LocalTestMatchmaker;
import au.com.suncoastpc.match.api.Match;
import au.com.suncoastpc.match.api.MatchmakerClient;

public class MatchTestClient implements MatchmakerClient {
	private static final String GAME = "test.test";
	private static final String GAME_PRIVATE = "c7b47f88eadd4ba0";//"ab832e87aaec478a";//"7a7c948228164a85";//"9896bbadf52b4525";
	private static final String UUID = "11111";
	
	private static final String ALTERNATE_UUID = "22222";
	private static final String THIRD_UUID = "33333";
	
	public static void main(String[] args) throws Exception {
		new MatchTestClient().runTest();
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void runTest() throws Exception {
		//initialize the matchmaker
		//Matchmaker.initialize(UUID, GAME, GAME_PRIVATE);
		LocalTestMatchmaker matchmaker = new LocalTestMatchmaker(UUID, GAME, GAME_PRIVATE, this);//Matchmaker.getMatchmaker(this);
		
		//Step 1:  auto join a match; will create it if it does not exist
		matchmaker.setCustomUuid(UUID);
		Match match = matchmaker.autoJoinMatch(3);
		
		//Step 3:  Now automatch should work for the second UUID
		matchmaker.setCustomUuid(ALTERNATE_UUID);
		Match secondMatch = matchmaker.autoJoinMatch(3, false, null);
		
		Thread.sleep(1000);
		matchmaker.setCustomUuid(THIRD_UUID);
		Match thirdMatch = matchmaker.autoJoinMatch(3, false, null);
		
		Thread.sleep(1000);
		
		JSONObject message = new JSONObject();
		message.put("text", "Server broadcasts hello...");
		match.broadcastMessage(message);
		
		message = new JSONObject();
		message.put("text", "Server says hello to " + ALTERNATE_UUID);
		match.sendMessageToPlayer(message, ALTERNATE_UUID);
		
		message = new JSONObject();
		message.put("text", "Client says hi to server");
		secondMatch.sendMessageToPlayer(message, UUID);
		
		message = new JSONObject();
		message.put("text", "Client broadcasts hi to world...");
		secondMatch.broadcastMessage(message);
		
		Thread.sleep(5000);
		
		//Step 4:  The second UUID joins the match
		//reply = matchmaker.playerJoined(ALTERNATE_UUID, matchId);
		
		//Step 5:  The second UUID leaves the match
		//reply = matchmaker.playerLeft(ALTERNATE_UUID, matchId);
		
		//Step 6:  Ping the match
		//reply = matchmaker.pingMatch(matchId);
		
		//Step 7:  Cancel the match
		matchmaker.setCustomUuid(UUID);
		matchmaker.cancelMatch(match);
		
		//Step 8:  create a private match
		match = matchmaker.hostPrivateMatch(2);
		String password = match.getPassword();
		
		//Step 9:  Now automatch should *not* find the private match
		matchmaker.setCustomUuid(ALTERNATE_UUID);
		secondMatch =  matchmaker.autoJoinMatch(-1, false, null);
		
		//Step 10:  Should be able to join the secret match manually using the password
		secondMatch = matchmaker.joinPrivateMatch(password, null);
		
		//Step 11:  The second UUID joins the match
		//reply = matchmaker.playerJoined(ALTERNATE_UUID, matchId);
		
		//Finally, start the match
		matchmaker.startMatch(secondMatch);
	}

	@Override
	public boolean shouldAcceptJoinFromPlayer(String uuid, JSONObject playerData) {
		return true;
	}

	@Override
	public void playerJoined(String uuid, JSONObject playerData) {
		System.out.println("playerJoined():  uuid=" + uuid + ", playerData=" + playerData.toJSONString());
	}

	@Override
	public void playerLeft(String uuid) {
		System.out.println("playerLeft():  uuid=" + uuid);
	}

	@Override
	public void dataReceivedFromPlayer(String uuid, JSONObject data) {
		System.out.println("dataReceivedFromPlayer():  uuid=" + uuid + ", data=" + data.toJSONString());
	}

	@Override
	public JSONObject getPlayerDetails() {
		return null;
	}
}
