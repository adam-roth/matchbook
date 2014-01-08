package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.Configuration;

/**
 * This annotation can be applied to any webservice method to cause it to record call 
 * durations in the server logs.  This is useful for debugging and profiling of the 
 * system, but should be used sparingly (if at all) in production.  
 * 
 * @author Adam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogsServiceTime {
	String methodParamName() default "method";
	int logLevel() default Level.DEBUG_INT;
	
	
	public static class Processor implements AnnotationProcessor {
		private static final String TIME_KEY = "__requestTime";
		private static final Logger LOG = Logger.getLogger(LogsServiceTime.class);
		
		
		@Override
		public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response) {
			if (! Configuration.getProfilingEnabled()) {
				//profiling not enabled, bail out
				return true;
			}
			
			if (! (theAnnotation instanceof LogsServiceTime)) {
				//someone made an invalid call, just return true
				return true;
			}
			
			LogsServiceTime annotation = (LogsServiceTime)theAnnotation;
			if (request.getAttribute(TIME_KEY) == null) {
				request.setAttribute(TIME_KEY, System.currentTimeMillis());
			}
			else {
				long startTime = (Long)request.getAttribute(TIME_KEY);
				long elapsed = System.currentTimeMillis() - startTime;
				
				String logMessage = "Total processing time for " + request.getParameter(annotation.methodParamName()) + ":  " + elapsed + " ms";
				switch (annotation.logLevel()) {
				case Level.TRACE_INT:
					LOG.trace(logMessage);
					break;
				case Level.DEBUG_INT:
					LOG.debug(logMessage);
					break;
				case Level.INFO_INT:
					LOG.info(logMessage);
					break;
				case Level.WARN_INT:
					LOG.warn(logMessage);
					break;
				case Level.ERROR_INT:
					LOG.error(logMessage);
					break;
				case Level.FATAL_INT:
					LOG.fatal(logMessage);
					break;

				default:
					LOG.warn("The provided logging level, '" + annotation.logLevel() + "', is not valid!");
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
			return true;
		}
	}
}
