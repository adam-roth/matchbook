package au.com.suncoastpc.match.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * This class represents a match request.  Match request entities are stored until the 
 * specified max number of players have joined.  Each match request tracks the IP address 
 * and port number of the user who initiated the match, and the address of each person who 
 * is confirmed as having joined the game.  
 * 
 * The connection details are handed out to new users as they attempt to join a match.  A 
 * match may be password-protected, in which case only people who know the password can join.  
 * This is to allow people to match up their friends, without having any formal support mapping 
 * between users in the data model.
 * 
 * Match requests will automatically timeout and die if left inactive.  
 * 
 * @author Adam
 */
@Entity
@Table(name="matchrequests")
@NamedQueries({
	@NamedQuery(name="MatchRequest.findByHostAndToken", query="SELECT m FROM MatchRequest m WHERE m.deviceAddress = :host AND m.deviceToken = :token"),
	@NamedQuery(name="MatchRequest.findByGameAndPassword", query="SELECT m FROM MatchRequest m WHERE m.game = :game AND m.password = :password"),
	@NamedQuery(name="MatchRequest.findByGameWithNoPasswordAndNoOptions", query="SELECT m FROM MatchRequest m WHERE m.game = :game AND m.currentNumPlayers < m.maxNumPlayers AND m.password IS NULL AND m.gameOptions IS NULL ORDER BY m.lastUpdated DESC"),  	//XXX:  the sort order prefers most recently active games over older games that have been inactive for longer; hopefully this ensures that joining is always fast, but we need to test this in the field to make sure
	@NamedQuery(name="MatchRequest.findByGameAndOptionsWithNoPassword", query="SELECT m FROM MatchRequest m WHERE m.game = :game AND m.gameOptions = :options AND m.currentNumPlayers < m.maxNumPlayers AND m.password IS NULL ORDER BY m.lastUpdated DESC"),  		//XXX:  the sort order prefers most recently active games over older games that have been inactive for longer; hopefully this ensures that joining is always fast, but we need to test this in the field to make sure
	@NamedQuery(name="MatchRequest.findByGameAndMaxPlayersWithNoPasswordAndNoOptions", query="SELECT m FROM MatchRequest m WHERE m.game = :game AND m.maxNumPlayers = :maxPlayers AND m.currentNumPlayers < m.maxNumPlayers AND m.password IS NULL AND m.gameOptions IS NULL ORDER BY m.lastUpdated DESC"),  	//XXX:  the sort order prefers most recently active games over older games that have been inactive for longer; hopefully this ensures that joining is always fast, but we need to test this in the field to make sure
	@NamedQuery(name="MatchRequest.findByGameAndMaxPlayersAndOptionsWithNoPassword", query="SELECT m FROM MatchRequest m WHERE m.game = :game AND m.maxNumPlayers = :maxPlayers AND m.gameOptions = :options AND m.currentNumPlayers < m.maxNumPlayers AND m.password IS NULL ORDER BY m.lastUpdated DESC"),  		//XXX:  the sort order prefers most recently active games over older games that have been inactive for longer; hopefully this ensures that joining is always fast, but we need to test this in the field to make sure
	@NamedQuery(name="MatchRequest.findRequestsUpdatedBeforeTime", query="SELECT m FROM MatchRequest m WHERE m.lastUpdated < :time")
})
public class MatchRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String deviceAddress;
	private String deviceLocalAddress;
	private String deviceToken;
	private int devicePort;
	private long lastUpdated;
	private String password;
	private int currentNumPlayers;
	private int maxNumPlayers;
	private String joinedPlayers;
	private String gameOptions;
	
	//relationships
	private Game game;
	
	public MatchRequest() {
		this.deviceAddress = null;
		this.devicePort = -1;
		this.lastUpdated = System.currentTimeMillis();
		this.password = null;
		this.currentNumPlayers = 1;
		this.maxNumPlayers = 1;
		this.joinedPlayers = null;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	//FIXME:  this should probably be randomly generated instead of ascending so that people can't guess match id's
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@Column(nullable = false)
	public String getDeviceAddress() {
		return deviceAddress;
	}
	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
	
	@Column(nullable = true)
	public String getDeviceLocalAddress() {
		return deviceLocalAddress;
	}
	public void setDeviceLocalAddress(String deviceLocalAddress) {
		this.deviceLocalAddress = deviceLocalAddress;
	}

	@Column(nullable = false)
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	@Column(nullable = false)
	public int getDevicePort() {
		return devicePort;
	}
	public void setDevicePort(int devicePort) {
		this.devicePort = devicePort;
	}

	@Column(nullable = false)
	public long getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Column(nullable = true)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Column(nullable = false)
	public int getCurrentNumPlayers() {
		return currentNumPlayers;
	}
	public void setCurrentNumPlayers(int currentNumPlayers) {
		this.currentNumPlayers = currentNumPlayers;
	}

	@Column(nullable = false)
	public int getMaxNumPlayers() {
		return maxNumPlayers;
	}
	public void setMaxNumPlayers(int maxNumPlayers) {
		this.maxNumPlayers = maxNumPlayers;
	}

	@Column(nullable = true)
	public String getJoinedPlayers() {
		return joinedPlayers;
	}
	public void setJoinedPlayers(String joinedPlayers) {
		this.joinedPlayers = joinedPlayers;
	}
	
	@Column(nullable = true)
	public String getGameOptions() {
		return gameOptions;
	}
	public void setGameOptions(String gameOptions) {
		this.gameOptions = gameOptions;
	}

	@ManyToOne(optional = false)
	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}
}
