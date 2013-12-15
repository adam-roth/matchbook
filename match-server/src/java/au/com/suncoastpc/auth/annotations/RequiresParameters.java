package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.OverridableHttpRequest;
import au.com.suncoastpc.auth.util.StringUtilities;

/**
 * Can be applied to a method to enforce that the specified parameters are not null or empty.  Will generate 
 * a generic error message and redirect to the specified target method if one or more of the parameters are 
 * missing.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequiresParameters {
	String[] paramNames() default {};
	String methodParamName() default "method";
	String errorMessageKey() default "error";
	String statusKey() default "[null]";
	String redirectTo();
	
	public static class Processor implements AnnotationProcessor {
		private static final Logger LOG = Logger.getLogger(RequiresParameters.class);
		
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof RequiresParameters)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			RequiresParameters annotation = (RequiresParameters)theAnnotation;
			for (String paramName : annotation.paramNames()) {
				if (StringUtilities.isEmpty(request.getParameter(paramName))) {
					LOG.warn("Request does not contain required parameter '" + paramName + "', will redirect to " + annotation.redirectTo() + "!");
					request.setAttribute(annotation.errorMessageKey(), "Validation failed:  " + paramName + " is a required field!"/*BaseMethods.getMessageSource().getMessage("error.validation.failed", new Object[] {paramName}, RequestContextUtils.getLocale(request))*/);
					request.setAttribute(annotation.statusKey(), Constants.ERROR_STATUS);
					 ((OverridableHttpRequest)request).setParameter(annotation.methodParamName(), annotation.redirectTo());
					 break;
				}
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
