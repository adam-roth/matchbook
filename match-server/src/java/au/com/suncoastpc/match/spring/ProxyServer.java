package au.com.suncoastpc.match.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.match.api.net.DataUtils;
import au.com.suncoastpc.match.db.MatchRequest;

public class ProxyServer extends Thread {
	private static final Logger LOG = Logger.getLogger(ProxyServer.class);
	
	private boolean stop;
	private ServerSocket listenSocket;
	private Map<String, List<ProxyServer.ProxiedConnection>> connections;
	
	public ProxyServer() throws IOException {
		this.stop = false;
		listenSocket = new ServerSocket(0);
		
		//FIXME:  should purge/close any pending connections when a game starts, times out, or is canceled
		this.connections = new HashMap<String, List<ProxyServer.ProxiedConnection>>();
	}
	
	public int getPort() {
		return this.listenSocket.getLocalPort();
	}
	
	public int numberOfConnections(String deviceToken) {
		if (connections.get(deviceToken) == null) {
			return 0;
		}
		return connections.get(deviceToken).size();
	}
	
	public void stopRunning() {
		this.stop = true;
		try {
			this.listenSocket.close();
		}
		catch (Exception ignored) { }
	}
	
	@Override
	public void run() {
		while (! stop) {
			try {
				Socket connection = this.listenSocket.accept();
				
				//the client should attempt to handshake; we can grab its handshake packet and use it to determine details about the match
				InputStream in = connection.getInputStream();
				JSONObject handshakePacket = DataUtils.readJsonFromStream(in);
				
				//"__MatchData":{"password":null,"uuid":"6e7927a6-a5e7-4355-b0fa-0bf9a91ed90c","matchId":"6"}
				JSONObject internalData = (JSONObject)handshakePacket.get("__MatchData");
				
				//first, find the match
				EntityManager em = DatabaseUtil.getEntityManager();
				MatchRequest match = em.find(MatchRequest.class, Long.parseLong(internalData.get("matchId").toString()));
				
				//FIXME:  this will need revision if adding support for running multiple games from a single device concurrently
				//next, see if we are talking to the match server or not
				if (match.getDeviceToken().equals(internalData.get("uuid"))) {
					//we're talking to the server; see if there are any pending proxy requests waiting for it
					synchronized(connections) {
						List<ProxyServer.ProxiedConnection> gameProxies = connections.get(match.getDeviceToken());
						if (gameProxies != null && ! gameProxies.isEmpty()) {
							ProxyServer.ProxiedConnection proxy = gameProxies.remove(0);
							proxy.setServer(connection, handshakePacket);
							LOG.debug("Established proxy connection for game:  matchId=" + match.getId() + ", matchData=" + internalData.toJSONString() + ", serverInput=" + proxy.serverHandler.input + ", clientInput=" + proxy.clientHandler.input);
						}
					}
				}
				else {
					//we're dealing with a client; queue up a proxy request
					synchronized(connections) {
						ProxyServer.ProxiedConnection proxy = new ProxiedConnection(connection, handshakePacket, match.getDeviceToken());
						List<ProxyServer.ProxiedConnection> gameProxies = connections.get(match.getDeviceToken());
						if (gameProxies == null) {
							gameProxies = new ArrayList<ProxyServer.ProxiedConnection>();
							connections.put(match.getDeviceToken(), gameProxies);
						}
						gameProxies.add(proxy);
						LOG.debug("Queued proxy request for game:  matchId=" + match.getId() + ", matchData=" + internalData.toJSONString() + ", clientInput=" + proxy.clientHandler.input);
					}
				}
				
				connection.setKeepAlive(true);
			}
			catch (Exception e) {
				LOG.warn("Unexpected exception in proxy-server thread!", e);
			}
		}
	}
	
	private class ProxiedConnection {
		private Socket server;
		private Socket client;
		private ProxyServer.DataHandler serverHandler;
		private ProxyServer.DataHandler clientHandler;
		private JSONObject firstClientPacket;
		private String deviceToken;
		
		public ProxiedConnection(Socket theClient, JSONObject firstPacket, String deviceToken) throws IOException {
			this.client = theClient;
			this.clientHandler = new DataHandler(theClient, theClient.getInputStream(), null, this);
			this.firstClientPacket = firstPacket;
			this.deviceToken = deviceToken;
			
			//start the clientHandler thread
			//this.clientHandler.start();
		}
		
		public void close() {
			try {
				server.close();
				client.close();
			}
			catch (Exception e) {
				LOG.warn("Unexpected error when attempting to close proxied connection!", e);
			}
			
			synchronized(connections) {
				if (connections.get(deviceToken) != null) {
					connections.get(deviceToken).remove(this);
				}
			}
		}
		
		public void setServer(Socket theServer, JSONObject firstPacket) throws IOException {
			if (client.isClosed()) {
				throw new IOException("Client went away...");
			}
			
			this.server = theServer;
			this.serverHandler = new DataHandler(theServer, theServer.getInputStream(), this.client.getOutputStream(), this);
			this.clientHandler.setOutput(theServer.getOutputStream());
			
			//exchange the handshake packets
			DataUtils.writeJsonToStream(this.firstClientPacket, this.server.getOutputStream());
			//DataUtils.writeJsonToStream(firstPacket, this.clientHandler.output);
			
			//start the threads
			this.clientHandler.start();
			this.serverHandler.start();
		}
	}
	
	private class DataHandler extends Thread {
		private InputStream input;
		private OutputStream output;
		private Socket socket;
		private ProxyServer.ProxiedConnection parent;
		//private List<JSONObject> packets;
		
		public DataHandler(Socket socket, InputStream input, OutputStream output, ProxyServer.ProxiedConnection connection) {
			this.socket = socket;
			this.input = input;
			this.output = output;
			this.parent = connection;
			//this.packets = new ArrayList<JSONObject>();
		}
		
		public void setOutput(OutputStream output) {
			this.output = output;
		}
		
		@Override
		public void run() {
			while (! socket.isClosed()) {
				try {
					if (output == null) {
						//defer reading packets until we have a valid connection to echo them to
						Thread.sleep(1000);
						continue;
					}
					
					JSONObject packet = DataUtils.readJsonFromStream(input);
					if (packet == null) {
						//shouldn't happen, kill thread
						break;
					}
					DataUtils.writeJsonToStream(packet, output);
				}
				catch (Throwable e) {
					LOG.error("Exception in DataHandler!", e);
					break;
				}
			}
			
			//destroy the proxy connection if an error occurs or when the socket closes; it needs to be removed from the list of people waiting to connect to the host
			LOG.debug("Closing proxied connection...");
			parent.close();
		}
	}
}