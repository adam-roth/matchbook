package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.filter.InputSanitizerFilter;
import au.com.suncoastpc.auth.util.OverridableHttpRequest;

/**
 * Use this to annotate any API method which has one or more parameters which should not be subject 
 * to input sanitization and quarantine rules.  Any named parameters will not be quarantined, even if 
 * they are found to contain potentially malicious content.
 * 
 * Code making use of such parameters needs to be careful in how it handles them, particularly with
 * respect to ensuring that they are not echoed directly without first escaping or removing any 
 * unsafe data such as quotation marks, script tags, and the like.
 * 
 * Can also be applied to a controller class, in which case all API methods in that controller will 
 * automatically get the specified behavior unless they explicitly override it.
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface BypassesQuarantine {
	String[] paramNames() default {};
	public static class Processor implements AnnotationProcessor {
		private static final Logger LOG = Logger.getLogger(BypassesQuarantine.class);
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (request instanceof OverridableHttpRequest) || (! (theAnnotation instanceof BypassesQuarantine))) {
				//can't do anything, just return true
				return true;
			}
			BypassesQuarantine annotation = (BypassesQuarantine)theAnnotation;
			OverridableHttpRequest editableRequest = (OverridableHttpRequest)request;
			Map<String, String> quarantine = (Map<String, String>)request.getAttribute(InputSanitizerFilter.QUARANTINE_ATTRIBUTE_NAME);
			if (quarantine != null) {
				for (String name : annotation.paramNames()) {
					if (quarantine.containsKey(name)) {
						LOG.warn("Removing paramater from quarantine for request:  " + name + "=" + quarantine.get(name));
						editableRequest.setParameter(name, quarantine.get(name));
					}
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
