package au.com.suncoastpc.match.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import au.com.suncoastpc.match.api.net.DataUtils;

//FIXME:  a match should have an inactgivity timeout both locally and on the server
//FIXME:  if a PLAYER socket disconnects make sure the server is informed that a palyer has left
//FIXME:  if a SERVER socket disconnected, make sure the game is removed from the server

public class Match {
	protected static final String INTERNAL_DATA_KEY = "__MatchData";
	private static final String BROADCAST = "broadcast";
	private static final String RELAY = "relay";
	private static final String INTERNAL = "internal";
	private static final String ACTION = "action";
	private static final String UUID = "uuid";
	
	private static final String ACTION_PLAYER_JOIN = "join";
	private static final String ACTION_PLAYER_LEFT = "left";
	private static final String ACTION_PING = "ping";
	
	private String id;
	private String myUuid;
	private String serverUuid;
	private String password;
	private Set<String> players;
	private Map<String, JSONObject> playerDetails;
	private Matchmaker matchmaker;
	
	//this is used only if I am not hosting the match
	private Socket matchSocket;
	private DataWriter serverConnection;
	
	//these are used only if I am hosting the match
	private ServerSocket listenSocket;
	private Map<String, DataWriter> playerConnections;		//FIXME:  keep track of the writers
	
	@SuppressWarnings("unused")
	private Match() {
		//default constructor not allowed
	}
	
	//private/protected API
	@SuppressWarnings("unchecked")
	protected JSONObject getMatchData() {
		JSONObject result = new JSONObject();
		result.put(UUID, this.getMyUuid());
		result.put("password", this.getPassword());
		result.put("matchId", this.getId());
		
		return result;
	}
	
	protected int getPort() {
		if (this.amITheServer()) {
			return this.listenSocket.getLocalPort();
		}
		return -1;
	}
	
	//public API
	public Match(String uuid, Matchmaker matchmaker) {
		this.myUuid = uuid;
		this.matchmaker = matchmaker;
		
		this.id = null;
		this.password = null;
		this.players = new HashSet<String>();
		this.playerDetails = new HashMap<String, JSONObject>();
		
		this.matchSocket = null;
		this.serverConnection = null;
		
		this.listenSocket = null;
		this.playerConnections = new HashMap<String, DataWriter>();
	}
	
	public Match(String uuid, Matchmaker matchmaker, String id) {
		this(uuid, matchmaker);
		setId(id);
	}

	public String getId() {
		return id;
	}
	
	public String getPassword() {
		return password;
	}

	public String getMyUuid() {
		return myUuid;
	}

	public String getServerUuid() {
		return serverUuid;
	}

	public Set<String> getPlayers() {
		return players;
	}
	
	public boolean amITheServer() {
		return this.serverUuid.equals(this.myUuid);
	}
	
	private JSONObject getInternalData(JSONObject packet) {
		if (! isPacketValid(packet)) {
			return null;
		}
		
		return (JSONObject)packet.get(INTERNAL_DATA_KEY);
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject configurePacketForBroadcast(JSONObject packet) {
		if (packet == null) {
			packet = new JSONObject();
		}
		if (packet.get(INTERNAL_DATA_KEY) == null) {
			packet.put(INTERNAL_DATA_KEY, getMatchData());
		}
		
		JSONObject internalData = (JSONObject)packet.get(INTERNAL_DATA_KEY);
		internalData.remove(RELAY);
		internalData.put(BROADCAST, "true");
		
		return packet;
	}
	
	private boolean isPacketBroadcast(JSONObject packet) {
		return isPacketValid(packet) && getInternalData(packet).get(BROADCAST) != null;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject configurePacketForRelay(JSONObject packet, String recipientUuid) {
		if (packet == null) {
			packet = new JSONObject();
		}
		if (packet.get(INTERNAL_DATA_KEY) == null) {
			packet.put(INTERNAL_DATA_KEY, getMatchData());
		}
		
		JSONObject internalData = (JSONObject)packet.get(INTERNAL_DATA_KEY);
		internalData.remove(BROADCAST);
		internalData.put(RELAY, recipientUuid);
		
		return packet;
	}
	
	private boolean isPacketRelay(JSONObject packet) {
		return isPacketValid(packet) && getInternalData(packet).get(RELAY) != null;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject configurePacketForInternalUse(JSONObject packet) {
		if (packet == null) {
			packet = new JSONObject();
		}
		if (packet.get(INTERNAL_DATA_KEY) == null) {
			packet.put(INTERNAL_DATA_KEY, getMatchData());
		}
		
		JSONObject internalData = (JSONObject)packet.get(INTERNAL_DATA_KEY);
		internalData.put(INTERNAL, "true");
		
		return packet;
	}
	
	private boolean isPacketInternal(JSONObject packet) {
		return isPacketValid(packet) && getInternalData(packet).get(INTERNAL) != null;
	}
	
	private boolean isPacketValid(JSONObject packet) {
		if (packet == null || ! packet.containsKey(INTERNAL_DATA_KEY)) {
			return false;
		}
		
		JSONObject internalData = (JSONObject)packet.get(INTERNAL_DATA_KEY);
		if (! id.equals(internalData.get("matchId"))) {
			//invalid connection attempt, reject
			return false;
		}
		
		if (password != null && ! password.equals(internalData.get("password"))) {
			//invalid password, reject
			return false;
		}
		
		return internalData.containsKey(UUID);
	}
	
	//FIXME:  add methods for passing data around to specific clients and via broadcast to all players in the match
	public synchronized void broadcastMessage(JSONObject message) {
		synchronized(players) {
			if (this.amITheServer()) {
				//if I'm the server, I have direct access to everyone who should get the message
				for (String player : players) {
					playerConnections.get(player).sendData(message);
				}
			}
			else {
				//otherwise, I need to tell the server that it should broadcast the message to everyone 
				configurePacketForBroadcast(message);
				serverConnection.sendData(message);
			}
		}
	}
	
	public synchronized void sendMessageToPlayer(JSONObject message, String playerUuid) {
		synchronized(players) {
			if (! players.contains(playerUuid)) {
				return;
			}
			if (this.amITheServer()) {
				//can send the message directly to the desired recipient
				playerConnections.get(playerUuid).sendData(message);
			}
			else {
				//need to tell the server to relay the message
				configurePacketForRelay(message, playerUuid);
				serverConnection.sendData(message);
			}
		}
	}
	
	public synchronized void disconnect() {
		if (this.amITheServer()) {
			this.terminateServer();
		}
		else if (serverConnection != null) {
			serverConnection.close();
		}
	}
	
	//FIXME:  refine protocol to include internal-only packets, broadcast packets, player discovery, etc.
	
	//internal API
	@SuppressWarnings("unchecked")
	boolean connectToServer(String ip, String port, String serverUuid) {
		try {
			System.out.println("Attempting connection to:  " + ip + ":" + port);
			matchSocket = new Socket();
			matchSocket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 5000);
			this.serverUuid = serverUuid;
			
			//handshake; note that as we have not spun up any threads to manage the input/output streams independently the server and client handshakes must synchronize the order in which they send/receive data
			InputStream input = matchSocket.getInputStream();
			OutputStream output = matchSocket.getOutputStream();
			
			System.out.println("Connection successful, attempting handshake!");
			
			//send our details to the server
			JSONObject myDetails = matchmaker.getClient().getPlayerDetails();
			if (myDetails == null) {
				myDetails = new JSONObject();
			}
			myDetails.put(INTERNAL_DATA_KEY, getMatchData());
			DataUtils.writeJsonToStream(myDetails, output);
			
			//now the server should send its details back
			JSONObject serverDetails = DataUtils.readJsonFromStream(input);
			JSONObject internalData = (JSONObject)serverDetails.get(INTERNAL_DATA_KEY);
			if (! isPacketValid(serverDetails)) {
				matchSocket.close();
				return false;
			}
			
			String uuid = internalData.get(UUID).toString();
			if (uuid == null || ! uuid.equals(serverUuid)) {
				//invalid uuid, reject
				matchSocket.close();
				return false;
			}
			
			//tell the client about the server that we have connected to
			matchmaker.getClient().playerJoined(serverUuid, serverDetails);
			
			//spawn a thread to manage the socket
			DataWriter playerWriter = new DataWriter(matchSocket, output, serverUuid, matchmaker);
			DataReader playerReader = new DataReader(matchSocket, input, matchmaker, playerWriter);
			serverConnection = playerWriter;
			synchronized(players) {
				players.add(serverUuid);
				playerDetails.put(serverUuid, serverDetails);
			}
			
			playerWriter.start();
			playerReader.start();
			
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	int becomeServer() throws IOException {
		//start listening for connections on an available port, report back with our port number
		this.listenSocket = new ServerSocket(0);
		this.serverUuid = this.myUuid;
		
		//spawn a thread to accept connections
		//FIXME:  also spawn a thread to ping our match to keep it from timing out
		new ServerThread(listenSocket, this.password, this.matchmaker).start();
		
		return this.listenSocket.getLocalPort();
	}
	
	void terminateServer() {
		try {
			//closing this will terminate the ServerThread; we also iterate all the DataWriter's and close them as well
			this.listenSocket.close();
			this.listenSocket = null;
			
			synchronized(players) {
				for (DataWriter writer : playerConnections.values()) {
					writer.close();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void setId(String id) {
		this.id = id;
	}
	
	void setPassword(String pass) {
		this.password = pass;
	}
	
	class ServerThread extends Thread {
		private ServerSocket server;
		//private String connectionPassword;
		private Matchmaker matchmaker;
		
		public ServerThread(ServerSocket socket, String password, Matchmaker matchmaker) {
			this.server = socket;
			//this.connectionPassword = password;
			this.matchmaker = matchmaker;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		//FIXME:  when the thread/socket dies, delete the match from the matchmaking server
		//FIXME:  authenticate the connection password when players join
		public void run() {
			while (! server.isClosed()) {
				try {
					//accept a new connection
					System.out.println("Trying to accept a new connection on port=" + server.getLocalPort());
					Socket newConnection = server.accept();
					
					System.out.println("Accepted connection, attempting handshake...");
					//handshake; note that as we have not spun up any threads to manage the input/output streams independently the server and client handshakes must synchronize the order in which they send/receive data
					InputStream input = newConnection.getInputStream();
					OutputStream output = newConnection.getOutputStream();
					
					//verify authentication details, if required
					JSONObject clientDetails = DataUtils.readJsonFromStream(input);
					JSONObject internalData = (JSONObject)clientDetails.get(INTERNAL_DATA_KEY);
					if (! isPacketValid(clientDetails)) {
						newConnection.close();
						continue;
					}
					
					String playerUuid = internalData.get(UUID).toString();
					if (playerUuid == null || playerUuid.equals(myUuid)) {
						//invalid uuid, reject
						//FIXME:  should also reject players that are already joined
						newConnection.close();
						continue;
					}
					
					//if we get here, everything checks out as far as we're concerned, so see if the client has any objections of its own
					if (! matchmaker.getClient().shouldAcceptJoinFromPlayer(playerUuid, clientDetails)) {
						//the client doesn't want this player to connect, reject
						newConnection.close();
						continue;
					}
					
					//second part of the handshake, we should send our own details back to the player that just connected
					//FIXME:  also send player details for every other connected player?
					JSONObject serverDetails = matchmaker.getClient().getPlayerDetails();
					if (serverDetails == null) {
						serverDetails = new JSONObject();
					}
					serverDetails.put(INTERNAL_DATA_KEY, getMatchData());
					DataUtils.writeJsonToStream(serverDetails, output);
					
					//the client wants to allow this player, so notify the matchmaking service that a player has joined and start up some threads to manage the session I/O
					//FIXME:  cache the player details?
					matchmaker.playerJoined(playerUuid, id);
					matchmaker.getClient().playerJoined(playerUuid, clientDetails);
					DataWriter playerWriter = new DataWriter(newConnection, output, playerUuid, matchmaker);
					DataReader playerReader = new DataReader(newConnection, input, matchmaker, playerWriter);
					synchronized(players) {
						playerConnections.put(playerUuid, playerWriter);
						//send details about all the other players 
						for (String uuid : players) {
							JSONObject packet = playerDetails.get(uuid);
							packet = configurePacketForInternalUse(packet);
							getInternalData(packet).put(ACTION, ACTION_PLAYER_JOIN);
							DataUtils.writeJsonToStream(packet, output);
						}
						
						//tell everyone else about the new player
						configurePacketForInternalUse(clientDetails);
						getInternalData(clientDetails).put(ACTION, ACTION_PLAYER_JOIN);
						broadcastMessage(clientDetails);
						
						players.add(playerUuid);
						playerDetails.put(playerUuid, clientDetails);
					}
					
					playerWriter.start();
					playerReader.start();
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class DataWriter extends Thread {
		private Socket socket;
		private Matchmaker matchmaker;
		private OutputStream output;
		private List<JSONObject> outputBuffer;
		private String playerUuid;		//the id of the player that we are writing the data to
		private boolean open;
		private int numSlept;
		
		public DataWriter(Socket socket, OutputStream output, String playerUuid, Matchmaker matchmaker) {
			this.output = output;
			this.socket = socket;
			this.matchmaker = matchmaker;
			this.playerUuid = playerUuid;
			this.outputBuffer = new ArrayList<JSONObject>();
			open = true;
			numSlept = 0;
		}
		
		public void close() {
			this.open = false;
		}
		
		public void sendData(JSONObject data) {
			if (data == null) {
				return;
			}
			synchronized(outputBuffer) {
				outputBuffer.add(data);
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			while (open) {
				try {
					List<JSONObject> dataToSend = new ArrayList<JSONObject>();
					synchronized(outputBuffer) {
						dataToSend.addAll(outputBuffer);
						outputBuffer.clear();
					}
					for (JSONObject packet : dataToSend) {
						if (packet.get(INTERNAL_DATA_KEY) == null) {
							//if it is not already signed, then sign it with the local uuid
							packet.put(INTERNAL_DATA_KEY, getMatchData());
						}
						DataUtils.writeJsonToStream(packet, output);
						numSlept = 0;
					}
					
					//if we didn't have anything to send, keep track of how long we have been idle and send an internal ping packet occasionally
					//FIXME:  ensure iOS does the same, if needed
					if (dataToSend.isEmpty()) {
						numSlept++;
						if (numSlept % 10 == 0) {
							JSONObject packet = configurePacketForInternalUse(null);
							getInternalData(packet).put(ACTION, ACTION_PING);
							sendData(packet);
						}
					}
					
					Thread.sleep(dataToSend.isEmpty() ? 100 : 10);
				}
				catch (IOException e) {
					e.printStackTrace();
					this.close();
				}
				catch (Exception ignored) {
					ignored.printStackTrace();
				}
			}
			
			try {
				socket.close();
			}
			catch (Exception ignored) {}
			
			//if we are the server, then send a playerLeft notification
			if (! myUuid.equals(playerUuid)) {
				if (amITheServer()) {
					matchmaker.playerLeft(playerUuid, id);
					
					synchronized(players) {
						JSONObject packet = playerDetails.get(playerUuid);
						playerConnections.remove(playerUuid);
						players.remove(playerUuid);
						playerDetails.remove(playerUuid);
						
						//tell all the other players about the disconnection
						packet = configurePacketForInternalUse(packet);
						getInternalData(packet).put(ACTION, ACTION_PLAYER_LEFT);
						getInternalData(packet).put(UUID, playerUuid);
						broadcastMessage(packet);
					}
				}
				else if (playerUuid.equals(serverUuid)) {
					//we lost our connection to the server; all players are gone
					synchronized(players) {
						for (String uuid : players) {
							if (! uuid.equals(playerUuid) && ! uuid.equals(myUuid)) {
								matchmaker.getClient().playerLeft(uuid);
							}
						}
						players.clear();
						playerDetails.clear();
					}
				}
				
				matchmaker.getClient().playerLeft(playerUuid);
			}
		}
	}
	
	class DataReader extends Thread {
		private Matchmaker matchmaker;
		private InputStream input;
		private Socket socket;
		private DataWriter pairedWriter;
		
		public DataReader(Socket socket, InputStream input, Matchmaker matchmaker, DataWriter writer) {
			this.socket = socket;
			this.input = input;
			this.matchmaker = matchmaker;
			this.pairedWriter = writer;
		}
		
		@Override
		public void run() {
			while (! socket.isClosed()) {
				try {
					JSONObject packet = DataUtils.readJsonFromStream(input);
					if (packet != null && isPacketValid(packet)) {
						JSONObject senderDetails = (JSONObject)packet.get(INTERNAL_DATA_KEY);
						String senderUuid = senderDetails.get(UUID).toString();
						if (senderUuid != null && (players.contains(senderUuid) || isPacketInternal(packet))) {
							if (amITheServer()) {
								//if I'm the server, I need to see if the packet is intended for broadcast or relay
								boolean ignorePacket = false;
								if (isPacketBroadcast(packet)) {
									//echo the packet to all the players except the one that sent it
									synchronized(players) {
										for (String playerUuid : players) {
											if (! playerUuid.equals(senderUuid)) {
												playerConnections.get(playerUuid).sendData(packet);
											}
										}
									}
								}
								else if (isPacketRelay(packet)) {
									String recipientUuid = getInternalData(packet).get(RELAY).toString();
									if (! recipientUuid.equals(myUuid)) {
										ignorePacket = true;
										synchronized(players) {
											if (playerConnections.containsKey(recipientUuid)) {
												playerConnections.get(recipientUuid).sendData(packet);
											}
										}
									}
								}
								
								if (! ignorePacket) {
									if (! isPacketInternal(packet)) {
										matchmaker.getClient().dataReceivedFromPlayer(senderUuid, packet);
									}
									else {
										this.handleInternalPacket(packet);
									}
								}
							}
							else {
								//if I'm not the server, any non-internal packet that I get should be passed to the client
								if (! isPacketInternal(packet)) {
									matchmaker.getClient().dataReceivedFromPlayer(senderUuid, packet);
								}
								else {
									this.handleInternalPacket(packet);
								}
							}
						}
					}
					else {
						//if we can't read a valid packet, abandon the connection
						//try {
						//	socket.close();
						//}
						//catch (Exception ignored) {}
						break;
					}
				}
				catch (Exception ignored) {
					ignored.printStackTrace();
				}
			}
			
			//closing the writer and the socket so that the correct event fires
			try {
				pairedWriter.close();
				socket.close();
			}
			catch (Exception ignored) {}
		}
		
		private void handleInternalPacket(JSONObject packet) {
			String action = getInternalData(packet).get(ACTION).toString();
			if (ACTION_PLAYER_JOIN.equals(action)) {
				//send a playerJoined() notification to the client and update our internal state
				String playerUuid = getInternalData(packet).get(UUID).toString();
				synchronized(players) {
					players.add(playerUuid);
					playerDetails.put(playerUuid, packet);
				}
				
				matchmaker.getClient().playerJoined(playerUuid, packet);
			}
			else if (ACTION_PLAYER_LEFT.equals(action)) {
				//send a playerLeft() notification to the client and update our internal state
				String playerUuid = getInternalData(packet).get(UUID).toString();
				synchronized(players) {
					players.remove(playerUuid);
					playerDetails.remove(playerUuid);
				}
				
				matchmaker.getClient().playerLeft(playerUuid);
			}
			else if (ACTION_PING.equals(action)) {
				//System.out.println("Keepalive ping received...");
			}
		}
	}
}
