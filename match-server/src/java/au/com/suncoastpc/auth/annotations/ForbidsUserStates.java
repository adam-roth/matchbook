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
 * Denies access to the annotated API method to any authenticated user that has any 
 * of the specified states.  Note that if no user is currently logged in, this annotation 
 * will default to granting access.  Use in conjunction with the @RequiresLogin annotation 
 * to enforce that a user must be logged in and not in any of the specified states.
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically forbid the specified user states explicitly override the annotation.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ForbidsUserStates {
	UserState[] states() default {};
	
	public static class Processor implements AnnotationProcessor {
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof ForbidsUserStates)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			ForbidsUserStates annotation = (ForbidsUserStates)theAnnotation;
			User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
			if (AccessUtils.doesUserHaveForbiddenState(user, annotation.states())) {
				//user is set to an invalid state, validation failed
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
