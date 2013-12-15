package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.suncoastpc.auth.util.OverridableHttpRequest;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EchoesParameters {
	String[] paramNames() default {};
	String[] excludedParams() default {};
	boolean clearsOnSuccess() default false;
	String statusAttribName() default "message";
	String successString() default "success";
	
	public static class Processor implements AnnotationProcessor {
		//private static final Logger LOG = Logger.getLogger(EchoesParameters.class);
		
		@Override
		//@SuppressWarnings("unchecked")
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (request instanceof OverridableHttpRequest) || (! (theAnnotation instanceof EchoesParameters))) {
				//can't do anything, just return true
				return true;
			}
			EchoesParameters annotation = (EchoesParameters)theAnnotation;
			
			
			String[] includedParams = annotation.paramNames();
			String[] excludedParams = annotation.excludedParams();
			boolean echoesAllParams = includedParams.length == 0;
			boolean clearParams = annotation.clearsOnSuccess() && request.getAttribute(annotation.statusAttribName()) != null 
				&& request.getAttribute(annotation.statusAttribName()).toString().contains(annotation.successString());
			
			Set<String> excludedSet = new HashSet<String>();
			for (String excluded : excludedParams) {
				excludedSet.add(excluded);
			}
			
			//echo things that have been explicitly included; so long as they are not also explicitly excluded
			for (String includeParam : includedParams) {
				if (! excludedSet.contains(includeParam)) {
					if (request.getAttribute(includeParam) == null) {
						request.setAttribute(includeParam, request.getParameter(includeParam));
					}
					else if (clearParams) {
						request.removeAttribute(includeParam);
					}
				}
			}
			
			//echo everything in the request, if we didn't have an explicit list of params to exlcude
			if (echoesAllParams) {
				Set<String> paramNames = request.getParameterMap().keySet();
				for (String name : paramNames) {
					if (! excludedSet.contains(name)) {
						if (request.getAttribute(name) == null) {
							request.setAttribute(name, request.getParameter(name));
						}
						else if (clearParams) {
							request.removeAttribute(name);
						}
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
			return true;
		}
	}
}
