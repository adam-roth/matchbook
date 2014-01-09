package au.com.suncoastpc.matchmaking.example;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import au.com.suncoastpc.match.api.Matchmaker;
import au.com.suncoastpc.match.api.MatchmakerClient;

/**
 * Can be used to provide Matchmaking services for Android applications.  
 * 
 * Note that this will use http://192.168.1.35:8080/ap/ as its matchmaking API server.  You will need 
 * to set up/find your own server and register your application on it (using a web browser) 
 * in order to get correct 'secret' key to pass to the matchmaker constructor.
 * 
 * Most of the functionality of this class is inherited from the core Java matchmaking SDK.
 * 
 * @author Adam
 */
public class AndroidMatchmaker extends Matchmaker {
	private static final String SERVER_ADDRESS = "http://192.168.1.35:8080/ap/";  //XXX:  we could also use a the 'au.com.suncoastpc.matchbook.server' property to specify this
	
	private TelephonyManager tm;
	
	/**
	 * Creates a new matchmaker instance.
	 * 
	 * @param tm a TelephonyManager instance obtained by doing '(TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);' from 
	 *           within an activity.  For more details, see here:  http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
	 * @param app the app identifier to find matches for.
	 * @param secret the 'secret' key generated by the matchmaking server when you registered your app identifier.
	 * @param client the client to notify about match-related events, may not be null.  
	 */
	public AndroidMatchmaker(TelephonyManager tm, String app, String secret, MatchmakerClient client) {
		super(null, app, secret, client, "".equals(System.getProperty("au.com.suncoastpc.matchbook.server", "")) ? SERVER_ADDRESS : System.getProperty("au.com.suncoastpc.matchbook.server"));
		this.tm = tm;
	}
	
	public AndroidMatchmaker(TelephonyManager tm, String app, String secret, String serverAddress, MatchmakerClient client) {
		super(null, app, secret, client, serverAddress);
		this.tm = tm;
	}
	
	@Override
	protected String getLocalIp() {
		try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                	String address = inetAddress.getHostAddress();
	                	if (address.contains("%")) {
	                		address = address.substring(0, address.indexOf("%"));
	                	}
	                	
	                	System.out.println("My local IP address is:  " + address);
	                    return address;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("getLocalIp", ex.toString());
	    }
	    return null;
	}
	
	@Override
	protected String getUuid() {
		String uuid = UUID.randomUUID().toString();
		if (Settings.Secure.ANDROID_ID != null && ! "android_id".equalsIgnoreCase(Settings.Secure.ANDROID_ID) && ! "unknown".equalsIgnoreCase(Settings.Secure.ANDROID_ID)) {
			uuid = Settings.Secure.ANDROID_ID;
		}
		else if (android.os.Build.SERIAL != null && ! "android_id".equals(android.os.Build.SERIAL) && ! "unknown".equals(android.os.Build.SERIAL)) {
			uuid = android.os.Build.SERIAL; 
		}
		else if (tm != null) {
			uuid = tm.getDeviceId();
		}
		
		System.out.println("My uuid is:  " + uuid);
		return uuid;
	}
}
