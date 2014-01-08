package au.com.suncoastpc.auth.annotations;

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a generic interface for annotation validator implementations.  The recommended usage 
 * pattern is that an AnnotationProcessor implementation should be provided as an inline class 
 * inside of each validation that the server is expected to validate at runtime.  
 * 
 * So long as this pattern is followed, the server will correctly and automatically validate any 
 * new annotations that are added.
 * 
 * @author aroth
 */
public interface AnnotationProcessor {
	/**
	 * Performs the validation task(s) associated with the annotation.  Returns true if validation
	 * succeeds, and false otherwise.
	 * 
	 * @param theAnnotation the annotation being validated.
	 * @param request the request to validate.
	 * 
	 * @return true if validation succeeds, false otherwise.
	 */
	public boolean processRequest(Annotation theAnnotation, HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * @return true if this annotation performs validation prior to request execution, false otherwise.
	 */
	public boolean validatesBeforeExecution();
	
	/**
	 * @return true if this annotation performs validation after request execution, false otherwise.
	 */
	public boolean validatesAfterExecution();
}

