package au.com.suncoastpc.match.api;

import org.json.simple.JSONObject;

/**
 * Implement this interface to receive state-change notifications and enable network 
 * communication.
 * 
 * @author Adam
 */
public interface MatchmakerClient {
	/**
	 * Asks the client whether or not the given player should be permitted to join the game.
	 * 
	 * @param uuid the unique-identifier of the connecting player.
	 * @param playerData any additional data that the connecting player sent as part of their connection attempt.
	 * @return true if the player should be allowed to connect
	 *         false if the player should be rejected
	 */
	public boolean shouldAcceptJoinFromPlayer(String uuid, JSONObject playerData);
	
	/**
	 * Informs the client that the given player has joined the game.  
	 * 
	 * @param uuid the unique-identifier of the player that joined.
	 * @param playerData any additional data that the connecting player sent as part of their connection attempt.
	 */
	public void playerJoined(String uuid, JSONObject playerData);
	
	/**
	 * Informs the client that the given player has left the game.
	 * 
	 * @param uuid the unique-identifier of the player that joined.
	 */
	public void playerLeft(String uuid);
	
	/**
	 * Notifies the client that data was received from the given player.
	 * 
	 * @param uuid the player that sent the data. 
	 * @param data the data that was sent.
	 */
	public void dataReceivedFromPlayer(String uuid, JSONObject data);
	
	/**
	 * Implement this method to return a custom JSONObject containing extended information about the local player.  
	 * The information returned from this method will be exchanged between clients whenever a new connection is made, 
	 * and will be sent to the shouldAcceptJoinFromPlayer() and playerJoined() callbacks when new players join the 
	 * match (if the current player is acting as the host).
	 * 
	 * If you do not wish to pass around any custom/extended information about the local player, then simply return 
	 * null from this method.
	 * 
	 * @return
	 */
	public JSONObject getPlayerDetails();
}
