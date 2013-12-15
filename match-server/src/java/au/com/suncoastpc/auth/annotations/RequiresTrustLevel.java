package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.Constants;

/**
 * Denies access to the annotated API method unless the user is 
 * logged in and in possession of a trust score that is greater than 
 * or equal to the specified minimum level of trust.  
 * 
 * This can be used to deny non-privileged users access to various portions 
 * of the API.
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically require the specified trust level unless they explicitly override the annotation.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresTrustLevel {
	int minimumTrust() default Constants.TRUST_LEVEL_ADMIN;
	
	public static class Processor implements AnnotationProcessor {
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof RequiresTrustLevel)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			RequiresTrustLevel annotation = (RequiresTrustLevel)theAnnotation;
			User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
			if (user == null || user.getTrustLevel() < annotation.minimumTrust()) {
				//not logged in or insufficient privileges, validation failed
				return false;
			}
			return true;
		}
		
		@Override
		public boolean validatesBeforeExecution() {
			return true;
		}

		@Override
		public boolean validatesAfterExecution() {
			return false;
		}
	}
}
