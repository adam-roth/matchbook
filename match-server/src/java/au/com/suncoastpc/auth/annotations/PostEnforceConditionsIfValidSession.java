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
 * Performs post-request validation to ensure that any/all specified conditions are met 
 * by the user.  The checks will only be executed if an authenticated user exists at 
 * the time that the post-request validation is performed.  
 * 
 * This annotation can be used for tasks such as validating that the user that just logged in
 * is in the UserState.STATE_CONFIRMED state, and invalidating their session if they are not.
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically enforce the specified post-request constraints unless they explicitly override the annotation.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PostEnforceConditionsIfValidSession {
	UserState[] requiredStates() default {};
	UserState[] forbiddenStates() default {};
	int minimumTrust() default Constants.TRUST_LEVEL_USER;
	
	public static class Processor implements AnnotationProcessor {
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof PostEnforceConditionsIfValidSession)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
			PostEnforceConditionsIfValidSession annotation = (PostEnforceConditionsIfValidSession)theAnnotation;
			if (user != null) {
				if (user.getTrustLevel() < annotation.minimumTrust()) {
					//user does not meet the minimum required trust requirements, validation failed
					return false;
				}
				if (! AccessUtils.doesUserHaveRequiredState(user, annotation.requiredStates())) {
					//user does not possess the required state, validation failed
					return false;
				}
				if (AccessUtils.doesUserHaveForbiddenState(user, annotation.forbiddenStates())) {
					//user is set to an invalid state, validation failed
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public boolean validatesBeforeExecution() {
			return false;
		}

		@Override
		public boolean validatesAfterExecution() {
			return true;
		}
	}
}
