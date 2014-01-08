package au.com.suncoastpc.match.util;

import java.util.UUID;

/**
 * Static utility-class for simple string-manipulation operations.
 * 
 * @author Adam
 */
public class StringUtilities {
	public static String randomStringWithLengthBetween(int minLength, int maxLength) {
		int delta = maxLength - minLength;
		int length = minLength + (int)(Math.random() * delta);
		
		return randomStringOfLength(length);
	}
	
	public static String randomStringOfLength(int length) {
		String buffer = "";
		while (buffer.length() < length) {
			buffer += uuidString();
		}
		
		return buffer.substring(0, length);
	}
	
	public static boolean isEmpty(String test) {
		return test == null || "".equals(test.trim());
	}
	
	private static String uuidString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
