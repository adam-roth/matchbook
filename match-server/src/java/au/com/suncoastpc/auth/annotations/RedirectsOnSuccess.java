package au.com.suncoastpc.auth.annotations;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RedirectsOnSuccess {
	String redirectTo();
	boolean copyParamaters() default true;
	String errorAttribName() default "error";
	String messageAttribName() default "message";
	String[] excludedParams() default {"method"};
	
	public static class Processor implements AnnotationProcessor {
		private static final Logger LOG = Logger.getLogger(RedirectsOnSuccess.class);
		
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! (theAnnotation instanceof RedirectsOnSuccess)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			RedirectsOnSuccess annotation = (RedirectsOnSuccess)theAnnotation;
			
			List<String> exclusions = Arrays.asList(annotation.excludedParams());
			
			//build the full redirect URL
			String redirectUrl = annotation.redirectTo();
			if (annotation.copyParamaters()) {
				for (Object paramKey : request.getParameterMap().keySet()) {
					String paramName = (String)paramKey;
					if (! exclusions.contains(paramName)) {
						if (! redirectUrl.contains("?")) {
							redirectUrl += "?";
						}
						else {
							redirectUrl += "&";
						}
						
						redirectUrl += paramName + "=" + request.getParameter(paramName);
					}
				}
			}
			if (request.getAttribute(annotation.messageAttribName()) != null) {
				if (! redirectUrl.contains("?")) {
					redirectUrl += "?";
				}
				else {
					redirectUrl += "&";
				}
				
				//redirectUrl += "__" + annotation.messageAttribName() + "=" + request.getAttribute(annotation.messageAttribName());
				request.getSession().setAttribute("__" + annotation.messageAttribName(), request.getAttribute(annotation.messageAttribName()));
			}
			
			//redirect if there was no error processing this request
			if (request.getAttribute(annotation.errorAttribName()) == null) {
				try {
					response.sendRedirect(redirectUrl);
				} catch (IOException e) {
					LOG.warn("Unable to process redirect to url=" + redirectUrl, e);
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
