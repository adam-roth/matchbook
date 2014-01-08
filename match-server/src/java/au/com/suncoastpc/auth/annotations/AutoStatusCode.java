package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.OverridableHttpRequest;
import au.com.suncoastpc.auth.util.StringUtilities;

/**
 * Automatically computes a status code for a request based upon attributes written into the 
 * response.  The computed code will be based upon the overall request status and any error message 
 * this is present (if one is present), and exposed as a request attribute.  
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AutoStatusCode {
	String responseStatusAttribute() default "status";
	String detailedErrorInfoAttribute() default "message";
	String successStatus() default "success";
	String outputAttribute() default "statusCode";
	
	public static class Processor implements AnnotationProcessor {
		private static final Logger LOG = Logger.getLogger(AutoStatusCode.class);
		
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (request instanceof OverridableHttpRequest) || (! (theAnnotation instanceof AutoStatusCode))) {
				//can't do anything, just return true
				return true;
			}
			AutoStatusCode annotation = (AutoStatusCode)theAnnotation;
			
			boolean isError = false;
			String statusText = request.getAttribute(annotation.responseStatusAttribute()) != null ? request.getAttribute(annotation.responseStatusAttribute()).toString() : null;
			if (! StringUtilities.isEmpty(statusText)) {
				
				isError = ! annotation.successStatus().equalsIgnoreCase(statusText);
				if (isError && request.getAttribute(annotation.detailedErrorInfoAttribute()) != null && ! StringUtilities.isEmpty(request.getAttribute(annotation.detailedErrorInfoAttribute()).toString())) {
					statusText = request.getAttribute(annotation.detailedErrorInfoAttribute()).toString();
				}
			}
			else if (request.getAttribute(annotation.detailedErrorInfoAttribute()) != null && ! StringUtilities.isEmpty(request.getAttribute(annotation.detailedErrorInfoAttribute()).toString())) {
				//if there's an error message but no status, take the error message
				statusText = request.getAttribute(annotation.detailedErrorInfoAttribute()).toString();
			}
			else {
				//default to success if no other info present
				statusText = annotation.successStatus();
			}
			
			request.setAttribute(annotation.outputAttribute(), Integer.toHexString(this.codeFromText(statusText)));
			LOG.debug("The status code for '" + statusText + "' is " + request.getAttribute(annotation.outputAttribute()));
			
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
		
		private Integer codeFromText(String text) {
			int result = -1;
			if (StringUtilities.isEmpty(text)) {
				return result;
			}
			
			for (char letter : text.toCharArray()) {
				result += letter;
			}
			
			return result;
		}
	}
}
