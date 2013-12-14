package au.net.iapps.auth.util;

/**
 * Holds constant values reference throughout the application.
 * 
 * @author Adam
 */
public class Constants {
	public static final String SESSION_USER_KEY = "iapps.auth.user";
	
	public static final String ENTITY_MANAGER_REQUEST_KEY = "iapps.auth.entity.manager";
	
	public static final String REGISTRATION_EMAIL_SUBJECT = "Please confirm your iApps account";
	
	public static final String RESET_PASSWORD_EMAIL_SUBJECT = "Your iApps account password";
	
	public static final int TRUST_LEVEL_USER = 1;
	
	public static final int TRUST_LEVEL_ADMIN = 10000000;  //10-million
	
	public static final String DEFAULT_REQUEST_METHOD = "indexPage";
	
	public static final String PERSISTENT_LOGIN_COOKIE_NAME = "iapps.auth.login.token";
	
	public static final long PERSISTENT_LOGIN_COOKIE_LIFETIME = 1000 * 60 * 60 * 24 * 14; //2 weeks
	
	public static final String POST_LOGIN_URL_KEY = "nextUrl";
	
	public static final String ERROR_STATUS = "error";
	
	public static final String SUCCESS_STATUS = "success";
}
