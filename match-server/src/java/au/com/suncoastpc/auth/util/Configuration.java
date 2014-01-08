package au.com.suncoastpc.auth.util;



/**
 * Contains various configuration parameters that may be overridden by system properties 
 * when starting the server.  The 'private static final' fields are used to store 
 * default values set at server startup.  The non-final fields are used to override 
 * configuration values at runtime, via the corresponding setter methods.  An overridden 
 * vale may be restored to its default initial state by invoking its setter with a 
 * value of 'null'.
 * 
 * @author Adam
 */
public class Configuration {
	//server setup
	private static final int SERVER_PORT;
	private static final String SERVER_HOST_NAME;
	private static final boolean REQUIRES_SECURE_CONNECTION;
	
	//email
	private static final String ADMIN_EMAIL_ACCOUNT;
	private static final String ADMIN_EMAIL_NAME;
	private static final boolean LOCALHOST_CAN_SEND_EMAIL;
	
	//database
	public static final String PERSISTENCE_UNIT;
	
	//synchronization
	private static final int NUM_ACCOUNT_LOCKS;
	
	private static String serverHostName = null;
	private static Integer serverPort = null;
	private static String serverProtocol = null;
	private static Boolean serverRequiresSecureConnection = null;
	
	private static String adminEmailAddress = null;
	private static String adminEmailName = null;
	private static Boolean localMailServer = null;
	
	private static Integer numAccountLocks = null;
	
	//profiling
	private static final boolean PROFILING_ENABLED;
	private static Boolean profilingEnabled;
	
	//HTTP sessions
	private static final int HTTP_TIMEOUT_MINUTES;
	private static Integer httpTimeoutMinutes = null;
	
	
	static {
		String host = System.getProperty("au.com.suncoastpc.server.hostname", "localhost").toLowerCase();
		String port = System.getProperty("au.com.suncoastpc.server.port", "8080");
		String protocol = System.getProperty("au.com.suncoastpc.server.protocol", "http").toLowerCase();
		
		String localEmail = System.getProperty("au.com.suncoastpc.server.email.local", "false");
		LOCALHOST_CAN_SEND_EMAIL = "true".equals(localEmail);
		ADMIN_EMAIL_ACCOUNT = System.getProperty("au.com.suncoastpc.server.email.admin", "admin@suncoastpc.com.au").toLowerCase();
		ADMIN_EMAIL_NAME = System.getProperty("au.com.suncoastpc.server.email.admin.name", "Matchbook Admin");
		
		String numLocks = System.getProperty("au.com.suncoastpc.server.num.locks", "4096");
		NUM_ACCOUNT_LOCKS = Integer.parseInt(numLocks);
		
		PERSISTENCE_UNIT = System.getProperty("au.com.suncoastpc.db.name", "match_devel");
		
		SERVER_HOST_NAME = host;
		SERVER_PORT = Integer.parseInt(port);
		REQUIRES_SECURE_CONNECTION = "https".equals(protocol);
		
		String profile = System.getProperty("au.com.suncoastpc.profiling.enabled", "true").toLowerCase();
		PROFILING_ENABLED = "true".equals(profile);
		
		String timeout = System.getProperty("au.com.suncoastpc.server.http.timeout", "120");
		HTTP_TIMEOUT_MINUTES = Integer.parseInt(timeout);
	}
	
	//composite configuration property, not settable directly
	public static String getServerAddress() {
		return getServerProtocol() + "://" + getServerHostName() + ((getServerPort() == 80 || getServerPort() == 443) ? "" : (":" + getServerPort()));
	}

	//server host, configurable
	public static String getServerHostName() {
		return serverHostName == null ? SERVER_HOST_NAME : serverHostName;
	}
	public static void setServerHostName(String serverHostName) {
		Configuration.serverHostName = serverHostName;
	}

	//server port, configurable
	public static int getServerPort() {
		return serverPort == null ? SERVER_PORT : serverPort;
	}
	public static void setServerPort(String serverPort) {
		if (serverPort == null) {
			Configuration.serverPort = null;
			return;
		}
		Configuration.serverPort = Integer.parseInt(serverPort);
	}

	//server protocol (http or https)
	public static String getServerProtocol() {
		return serverProtocol  == null ? (getServerRequiresSecureConnection() ? "https" : "http") : serverProtocol;
	}
	public static void setServerProtocol(String serverProtocol) {
		Configuration.serverProtocol = serverProtocol;
	}

	//whether or not to require a secure connection
	public static boolean getServerRequiresSecureConnection() {
		return serverRequiresSecureConnection == null ? REQUIRES_SECURE_CONNECTION : serverRequiresSecureConnection;
	}
	public static void setServerRequiresSecureConnection(String serverRequiresSecureConnection) {
		if (serverRequiresSecureConnection == null) {
			Configuration.serverRequiresSecureConnection = null;
			return;
		}
		Configuration.serverRequiresSecureConnection = Boolean.valueOf(serverRequiresSecureConnection);
	}
	
	//number of locks to use for synchronizing account accesses
	//FIXME:  runtime modifications do not currently do anything
	public static int getNumAccountLocks() {
		return numAccountLocks == null ? NUM_ACCOUNT_LOCKS : numAccountLocks;
	}
	public static void setNumAccountLocks(String numLocks) {
		if (numLocks == null) {
			Configuration.numAccountLocks = null;
			return;
		}
		Configuration.numAccountLocks = Integer.parseInt(numLocks);
	}

	//the from address to use when sending e-mail
	public static String getAdminEmailAddress() {
		return adminEmailAddress == null ? ADMIN_EMAIL_ACCOUNT : adminEmailAddress;
	}
	public static void setAdminEmailAddress(String adminEmailAddress) {
		Configuration.adminEmailAddress = adminEmailAddress;
	}
	
	//the name to use when sending e-mail
	public static String getAdminEmailName() {
		return adminEmailName == null ? ADMIN_EMAIL_NAME : adminEmailName;
	}
	public static void setAdminEmailName(String adminEmailName) {
		Configuration.adminEmailName = adminEmailName;
	}

	//whether or not localhost is capable of sending e-mail (i.e. whether or not it is running an SMTP server instance)
	public static boolean getLocalMailServer() {
		return localMailServer == null ? LOCALHOST_CAN_SEND_EMAIL : localMailServer;
	}
	public static void setLocalMailServer(String localMailServer) {
		if (localMailServer == null) {
			Configuration.localMailServer = null;
			return;
		}
		Configuration.localMailServer = Boolean.valueOf(localMailServer);
	}
	
	//performance profiling
	public static boolean getProfilingEnabled() {
		return profilingEnabled == null ? PROFILING_ENABLED : profilingEnabled;
	}
	public static void setProfilingEnabled(String profiling) {
		if (profiling == null) {
			Configuration.profilingEnabled = null;
			return;
		}
		Configuration.profilingEnabled = Boolean.valueOf(profiling);
	}
	
	//HTTP session timeout, configurable
	public static int getHttpTimeoutMinutes() {
		return httpTimeoutMinutes == null ? HTTP_TIMEOUT_MINUTES : httpTimeoutMinutes;
	}
	public static void setHttpTimeoutMinutes(String minutes) {
		if (minutes == null) {
			Configuration.httpTimeoutMinutes = null;
			return;
		}
		Configuration.httpTimeoutMinutes = Integer.parseInt(minutes);
	}
}
