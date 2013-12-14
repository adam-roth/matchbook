package au.com.suncoastpc.match.api.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DataUtils {
	private static final int ONE_BYTE = 0x000000FF;
	
	public static byte[] serializeInt32(int num) {
		byte[] result = new byte[4];
		result[0] = (byte)((num >> 24) & ONE_BYTE);
		result[1] = (byte)((num >> 16) & ONE_BYTE);
		result[2] = (byte)((num >> 8) & ONE_BYTE);
		result[3] = (byte)(num & ONE_BYTE);
		
		return result;
	}
	
	public static byte[] serializeJson(JSONObject json) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			writeJsonToStream(json, buffer);
			return buffer.toByteArray();
		}
		catch (Exception ignored) {
			ignored.printStackTrace();
		}
		return null;
	}
	
	public static void writeJsonToStream(JSONObject json, OutputStream out) throws IOException {
		if (json == null) {
			return;
		}
		
		String data = json.toJSONString();
		
		out.write(serializeInt32(data.length()));
		out.write(data.getBytes());
		out.flush();
	}
	
	public static JSONObject readJsonFromStream(InputStream in) throws IOException {
		int size = reconstructInt32(in);
		if (size < 1) {
			//invalid size or EOF
			return null;
		}
		byte[] buffer = new byte[size];
		int numRead = fillBuffer(in, buffer);
		if (numRead < buffer.length) {
			//invalid data or EOF
			return null;
		}
		
		//System.out.println("Got Json:  " + new String(buffer));
		return (JSONObject)JSONValue.parse(new String(buffer));
	}
	
	public static int reconstructInt32(byte[] data) {
		return (data[0] & ONE_BYTE) << 24 | (data[1] & ONE_BYTE) << 16 | (data[2] & ONE_BYTE) << 8 | (data[3] & ONE_BYTE);
	}
	
	public static int reconstructInt32(InputStream in) {
		byte[] buffer = new byte[4];
		if (fillBuffer(in, buffer) == 4) {
			return reconstructInt32(buffer);
		}
		
		//couldn't read enough bytes from the stream
		//XXX:  throw exception here?
		return -1;
	}
	
	public static int fillBuffer(InputStream in, byte[] buffer) {
		int totalRead = 0;
		try {
			while (totalRead < buffer.length) {
				int read = in.read(buffer, totalRead, buffer.length - totalRead);
				if (read == -1) {
					//end of stream, cannot fill buffer
					new RuntimeException().printStackTrace();
					break;
				}
				totalRead += read;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return totalRead;
	}
}
