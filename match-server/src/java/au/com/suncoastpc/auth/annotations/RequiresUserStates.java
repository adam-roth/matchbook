package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.AccessUtils;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.types.UserState;

/**
 * Denies access to the annotated API method to any authenticated user that does not have at least one 
 * of the specified states.  Note that if no user is currently logged in, this annotation 
 * will default to blocking access.  
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically require the specified user state(s) unless they explicitly override the annotation.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresUserStates {
	UserState[] states() default {};
	
	public static class Processor implements AnnotationProcessor {
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof RequiresUserStates)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			RequiresUserStates annotation = (RequiresUserStates)theAnnotation;
			User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
			if (! AccessUtils.doesUserHaveRequiredState(user, annotation.states())) {
				//user does not possess the required state, validation failed
				return false;
			}
			
			return user != null;
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
