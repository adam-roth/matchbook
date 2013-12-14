package au.net.iapps.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;

import au.net.iapps.auth.util.Constants;

/**
 * Restricts access to the annotated API method to non-authenticated users only.  For instance, a user with a 
 * current session should not be allowed to register a new account, etc..
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically forbid login unless they explicitly override the annotation.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ForbidsLogin {
	public static class Processor implements AnnotationProcessor {
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request) {
			if (! (theAnnotation instanceof ForbidsLogin)) {
				//someone made an invalid call, just return true
				return true;
			}
			return request.getSession().getAttribute(Constants.SESSION_USER_KEY) == null;
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
