package au.com.suncoastpc.match.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import au.com.suncoastpc.match.api.net.DataUtils;
import au.com.suncoastpc.match.util.StringUtilities;


public class Matchmaker {
	protected static final String SERVER_ADDRESS;
	
	static {
		SERVER_ADDRESS = System.getProperty("au.com.suncoastpc.matchbook.server", "http://localhost:8080/ap/");
	}
	
	protected String customServerAddress;
	
	protected String uuid;
	protected String app;
	protected String secret;
	protected MatchmakerClient client;
	
	protected Map<String, MatchPingThread> pingers;
	
	@SuppressWarnings("unused")
	private Matchmaker() {
		//default constructor, should not be used
	}
	
	//things for platform-specific subclasses to override
	protected String getLocalIp() {
		return null;
	}
	
	protected String getUuid() {
		return null;
	}
	
	public Matchmaker(String uuid, String app, String secret, MatchmakerClient client, String serverAddress) {
		this(uuid, app, secret, client);
		
		this.customServerAddress = serverAddress;
	}
	
	public Matchmaker(String uuid, String app, String secret, MatchmakerClient client) {
		this.uuid = this.getUuid() == null ? uuid : this.getUuid();
		this.app = app;
		this.secret = secret;
		this.client = client;
		
		this.pingers = new HashMap<String, MatchPingThread>();
	}
	
	//high-level API methods; these are what external users should call
	public synchronized boolean startMatch(Match match) {
		JSONObject reply = this.startMatch(match.getId());
		if (this.successfulRequest(reply)) {
			MatchPingThread pinger = pingers.get(match.getId());
			if (pinger != null) {
				pinger.stopRunning();
			}
		}
		return this.successfulRequest(reply);
	}
	
	public synchronized boolean cancelMatch(Match match) {
		match.disconnect();
		if (match.amITheServer()) {
			MatchPingThread pinger = pingers.get(match.getId());
			if (pinger != null) {
				pinger.stopRunning();
			}
		}
		
		JSONObject reply = this.cancelMatch(match.getId());
		return this.successfulRequest(reply);
	}
	
	public synchronized Match joinPrivateMatch(String password) {
		return this.joinPrivateMatch(password, null);
	}
	
	public synchronized Match joinPrivateMatch(String password, String gameOptions) {
		Match result = null;
		
		JSONObject serverMatch = this.joinMatch(password, gameOptions);
		if (this.successfulRequest(serverMatch)) {
			//set up the match
			//{"port":"24","id":"11","token":"12345","status":"success","addr":"127.0.0.1","localAddr":""}
			boolean connected = false;
			result = new Match(this.uuid, this, serverMatch.get("id").toString());
			result.setPassword(password);
			
			String port = serverMatch.get("port").toString();
			String serverUuid = serverMatch.get("token").toString();
			if (! StringUtilities.isEmpty(serverMatch.get("localAddr").toString())) {
				connected = result.connectToServer(serverMatch.get("localAddr").toString(), port, serverUuid);
			}
			if (! connected) {
				connected = result.connectToServer(serverMatch.get("addr").toString(), port, serverUuid);
			}
			if (! connected) {
				JSONObject proxyDetails = this.requestProxiedConnection(result.getId());
				if (this.successfulRequest(proxyDetails)) {
					System.out.println("Attempting proxied connection...");
					String serverInfo = proxyDetails.get("address").toString();
					String[] parts = serverInfo.split("\\:");
					connected = result.connectToServer(parts[0], parts[1], serverUuid);
					System.out.println("Proxied connection result:  " + connected);
				}
			}
			
			//if still not connected, then we can't join the match; return null
			result = connected ? result : null;
		}
		
		return result;
	}
	
	public synchronized Match hostPrivateMatch(int numPlayers) {
		return hostPrivateMatch(numPlayers, null);
	}
	
	public synchronized Match hostPrivateMatch(int numPlayers, String gameOptions) {
		if (numPlayers < 2) {
			numPlayers = 2;
		}
		Match result = new Match(this.uuid, this);
		try {
			int port = result.becomeServer();
			JSONObject serverReply = this.hostMatch(port, numPlayers, true, gameOptions, this.getLocalIp());
			if (this.successfulRequest(serverReply)) {
				//{"status":"success","matchId":"11","password":""}
				result.setId(serverReply.get("matchId").toString());
				
				//XXX: there should *always* be a password at this point
				if (! StringUtilities.isEmpty(serverReply.get("password").toString())) {
					result.setPassword(serverReply.get("password").toString());
				}
				
				pingers.put(result.getId(), new MatchPingThread(result, this));
				pingers.get(result.getId()).start();
			}
			else {
				//could not create the match
				//FIXME:  should check to see if the response includes other matches that need to be canceled, and cancel them if so
				result.terminateServer();
				result = null;
			}
		}
		catch (Exception e) {
			result = null;
			e.printStackTrace();
		}
		
		return result;
	}
	
	public synchronized Match autoJoinMatch() {
		return autoJoinMatch(-1, true);
	}
	
	public synchronized Match autoJoinMatch(int numPlayers) {
		return autoJoinMatch(numPlayers, true);
	}
	
	public synchronized Match autoJoinMatch(int numPlayers, boolean createIfNecessary) {
		return autoJoinMatch(numPlayers, createIfNecessary, null);
	}
	
	public synchronized Match autoJoinMatch(int numPlayers, boolean createIfNecessary, String gameOptions) {
		Match result = null;
		
		JSONObject autoMatch = this.autoMatch(numPlayers, gameOptions);
		if (this.successfulRequest(autoMatch)) {
			//set up the match
			//{"port":"24","id":"11","token":"12345","status":"success","addr":"127.0.0.1","localAddr":""}
			boolean connected = false;
			String port = autoMatch.get("port").toString();
			String serverUuid = autoMatch.get("token").toString();
			result = new Match(this.uuid, this, autoMatch.get("id").toString());
			if (! StringUtilities.isEmpty(autoMatch.get("localAddr").toString())) {
				connected = result.connectToServer(autoMatch.get("localAddr").toString(), port, serverUuid);
			}
			if (! connected) {
				//couldn't connect over local network, try over WAN
				connected = result.connectToServer(autoMatch.get("addr").toString(), port, serverUuid);
			}
			if (! connected) {
				//couldn't connect over WAN, try a proxied connection
				JSONObject proxyDetails = this.requestProxiedConnection(result.getId());
				if (this.successfulRequest(proxyDetails)) {
					//FIXME:  actually getting a proxied connection may take awhile; should we spawn a new thread here?
					System.out.println("Attempting proxied connection...");
					String serverInfo = proxyDetails.get("address").toString();
					String[] parts = serverInfo.split("\\:");
					connected = result.connectToServer(parts[0], parts[1], serverUuid);
					System.out.println("Proxied connection result:  " + connected);
				}
			}
			
			//if still not connected, then we can't join the match; return null
			result = connected ? result : null;
		}
		else if (createIfNecessary) {
			//host a match
			result = new Match(this.uuid, this);
			try {
				int port = result.becomeServer();
				autoMatch = this.hostMatch(port, numPlayers, false, gameOptions, this.getLocalIp());
				if (this.successfulRequest(autoMatch)) {
					//{"status":"success","matchId":"11","password":""}
					result.setId(autoMatch.get("matchId").toString());
					
					//XXX: there should never actually be a password at this point
					if (! StringUtilities.isEmpty(autoMatch.get("password").toString())) {
						result.setPassword(autoMatch.get("password").toString());
					}
					
					pingers.put(result.getId(), new MatchPingThread(result, this));
					pingers.get(result.getId()).start();
				}
				else {
					//could not create the match
					result.terminateServer();
					result = null;
				}
			}
			catch (Exception e) {
				result = null;
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	//low-level API methods; external users aren't expected to call these directly
	synchronized JSONObject requestProxiedConnection(String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.REQUEST_PROXY, params);
	}
	
	synchronized JSONObject listWaitingProxies(String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.LIST_PROXIES, params);
	}
	
	synchronized JSONObject joinMatch(String pass) {
		return joinMatch(pass, null);
	}
	
	synchronized JSONObject joinMatch(String pass, String gameOptions) {
		Map<String, String> params = getDefaultParams();
		params.put("pass", pass);
		params.put("gameOptions", gameOptions);
		
		return doRequest(MatchApi.JOIN_MATCH, params);
	}
	
	synchronized JSONObject autoMatch(int numPlayers) {
		return autoMatch(numPlayers, null);
	}
	
	synchronized JSONObject autoMatch(int numPlayers, String gameOptions) {
		Map<String, String> params = getDefaultParams();
		params.put("gameOptions", gameOptions);
		if (numPlayers > 1) {
			params.put("maxPlayers", Integer.toString(numPlayers));
		}
		
		return doRequest(MatchApi.AUTO_MATCH, params);
	}
	
	synchronized JSONObject hostMatch(Integer port, Integer maxPlayers, boolean privateMatch) {
		return hostMatch(port, maxPlayers, privateMatch, null);
	}
	
	synchronized JSONObject hostMatch(Integer port, Integer maxPlayers, boolean privateMatch, String gameOptions) {
		return hostMatch(port, maxPlayers, privateMatch, gameOptions, null);
	}
	
	synchronized JSONObject hostMatch(Integer port, Integer maxPlayers, boolean privateMatch, String gameOptions, String internalIp) {
		if (maxPlayers < 2) {
			maxPlayers = 2;
		}
		Map<String, String> params = getDefaultParams();
		params.put("port", port.toString());
		params.put("maxPlayers", maxPlayers.toString());
		if (privateMatch) {
			params.put("privateMatch", "true");
		}
		if (gameOptions != null) {
			params.put("gameOptions", gameOptions);
		}
		if (internalIp != null) {
			params.put("internalIp", internalIp);
		}
		
		return doRequest(MatchApi.HOST_MATCH, params);
	}
	
	synchronized JSONObject playerJoined(String playerUuid, String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("playerUuid", playerUuid);
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.PLAYER_JOINED, params);
	}
	
	synchronized JSONObject playerLeft(String playerUuid, String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("playerUuid", playerUuid);
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.PLAYER_LEFT, params);
	}
	
	synchronized JSONObject pingMatch(String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.PING_MATCH, params);
	}
	
	synchronized JSONObject cancelMatch(String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.CANCEL_MATCH, params);
	}
	
	synchronized JSONObject startMatch(String matchId) {
		Map<String, String> params = getDefaultParams();
		params.put("matchId", matchId);
		
		return doRequest(MatchApi.START_MATCH, params);
	}
	
	MatchmakerClient getClient() {
		return this.client;
	}
	
	//utility method
	protected boolean successfulRequest(JSONObject response) {
		return response != null && response.get("status") != null && response.get("status").equals("success");
	}
	
	protected Map<String, String> getDefaultParams() {
		Map<String, String> result = new HashMap<String, String>();
		
		result.put("app", app);
		result.put("uuid", uuid);
		result.put("secret", secret);
		
		return result;
	}
	
	protected JSONObject doRequest(MatchApi method, Map<String, String> params) {
		try {
			String url = method.getApiUrl(serverAddress(), params); 
			InputStream response = new URL(url).openStream();
			JSONObject result =  readFully(response);
			
			//FIXME:  remove
			System.out.println(method.getMethodName() + "(url=" + url + ") returned:  " + result.toJSONString());
			
			return result;
		}
		catch (Exception ignored) {
			ignored.printStackTrace();
		}
		return null;
	}
	
	private String serverAddress() {
		String address = customServerAddress != null ? customServerAddress : SERVER_ADDRESS;
		if (! address.endsWith("/ap/")) {
			System.out.println("WARN:  The provided Matchbook server URL does not appear valid; url=" + address); 
			if (address.endsWith("/ap")) {
				address += "/";
			}
			else {
				address += "/ap/";
			}
			System.out.println("WARNL  Natchbook will attempt to use the following server URL:  " + address);
		}
		
		return address;
	}
	
	private static JSONObject readFully(InputStream stream) {
		JSONObject result = null;
		
		try {
			int numRead = 0;
			ByteArrayOutputStream accumulator = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			while ((numRead = stream.read(buffer)) != -1)  {
				accumulator.write(buffer, 0, numRead);
			}
			
			String temp = new String(accumulator.toByteArray());
			result = (JSONObject)JSONValue.parse(temp);
		}
		catch (Exception ignored) {
			ignored.printStackTrace();
		}
		
		return result;
	}
	
	private static class MatchPingThread extends Thread {
		private static final long PING_INTERVAL = 1000 * 30; //ping every 30 seconds
		
		private Match match;
		private Matchmaker matchmaker;
		private boolean stop;
		
		public MatchPingThread(Match match, Matchmaker matchmaker) {
			this.match = match;
			this.matchmaker = matchmaker;
			this.stop = false;
		}
		
		public void stopRunning() {
			this.stop = true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			while (! stop) {
				try {
					Thread.sleep(PING_INTERVAL);
					if (! matchmaker.successfulRequest(matchmaker.pingMatch(match.getId()))) {
						this.stopRunning();
					}
					JSONObject waitingProxies = matchmaker.listWaitingProxies(match.getId());
					if (matchmaker.successfulRequest(waitingProxies) && ! "0".equals(waitingProxies.get("numWaiting"))) {
						String[] serverInfo = waitingProxies.get("address").toString().split("\\:");
						int numWaiting = Integer.parseInt(waitingProxies.get("numWaiting").toString());
						for (int connection = 0; connection < numWaiting; connection++) {
							Socket remote = new Socket(serverInfo[0], Integer.parseInt(serverInfo[1]));
							
							//send our player info so that the proxy can identify us
							JSONObject myDetails = matchmaker.getClient().getPlayerDetails();
							if (myDetails == null) {
								myDetails = new JSONObject();
							}
							myDetails.put(Match.INTERNAL_DATA_KEY, match.getMatchData());
							DataUtils.writeJsonToStream(myDetails, remote.getOutputStream());
							
							//bridge the socket we have to the server with our local server thread
							Socket local = new Socket("localhost", match.getPort());
							new LocalSocketBridge(remote, local); 		//creating this will start the DataPipe threads
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			matchmaker.pingers.remove(match.getId());
		}
	}
	
	private static class LocalSocketBridge {
		private Socket server;
		private Socket client;
		
		private DataPipe dataIn;
		private DataPipe dataOut;
		
		public void close() {
			try {
				this.server.close();
				this.client.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public LocalSocketBridge(Socket server, Socket client) throws IOException {
			this.server = server;
			this.client = client;
			
			dataIn = new DataPipe(server.getInputStream(), client.getOutputStream(), this);
			dataOut = new DataPipe(client.getInputStream(), server.getOutputStream(), this);
			
			dataIn.start();
			dataOut.start();
		}
		
	}
	
	private static class DataPipe extends Thread {
		private InputStream input;
		private OutputStream output;
		private LocalSocketBridge connection;
		
		public DataPipe(InputStream in, OutputStream out, LocalSocketBridge parent) {
			this.input = in;
			this.output = out;
			this.connection = parent;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					JSONObject packet = DataUtils.readJsonFromStream(input);
					DataUtils.writeJsonToStream(packet, output);
				}
				catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			
			connection.close();
		}
		
	}
}
