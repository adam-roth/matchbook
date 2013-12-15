package au.com.suncoastpc.auth.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import au.com.suncoastpc.auth.annotations.AnnotationProcessor;
import au.com.suncoastpc.auth.annotations.BypassesQuarantine;
import au.com.suncoastpc.auth.annotations.ForbidsLogin;
import au.com.suncoastpc.auth.annotations.ForbidsUserStates;
import au.com.suncoastpc.auth.annotations.PostEnforceConditionsIfValidSession;
import au.com.suncoastpc.auth.annotations.RequiresLogin;
import au.com.suncoastpc.auth.annotations.RequiresParameters;
import au.com.suncoastpc.auth.annotations.RequiresTrustLevel;
import au.com.suncoastpc.auth.annotations.RequiresUserStates;

public class AnnotationUtils {
	private static final Logger LOG = Logger.getLogger(AnnotationUtils.class);
	
	/**
	 * Ordered pre-request annotations.  These annotations are validated before a request is processed by the 
	 * API.
	 * 
	 * Annotations may be specified in processing order, this is important, as certain annotations may fail 
	 * to validate if other annotations are not run first.  For example, 'BypassesQuarantine' might 
	 * recover a parameter required by 'RequiresParameters', so it should always be run first.
	 */
	private static final List<Class<? extends Annotation>> PRE_REQUEST_ANNOTATIONS;
	
	/**
	 * Ordered post-request annotations.  These annotations are validated after a request is processed by the 
	 * API.
	 */
	private static final List<Class<? extends Annotation>> POST_REQUEST_ANNOTATIONS;
	
	static {
		/**
		 * Annotations may be specified in processing order, this is important, as certain annotations may fail 
		 * to validate if other annotations are not run first.  For example, 'BypassesQuarantine' might 
		 * recover a parameter required by 'RequiresParameters', so it should always be run first.
		 * 
		 * When adding a new annotation that requires ordered per-request processing, insert it into an appropriate 
		 * position in this list.
		 */
		PRE_REQUEST_ANNOTATIONS = new ArrayList<Class<? extends Annotation>>();
		PRE_REQUEST_ANNOTATIONS.add(RequiresLogin.class);
		PRE_REQUEST_ANNOTATIONS.add(ForbidsLogin.class);
		PRE_REQUEST_ANNOTATIONS.add(RequiresTrustLevel.class);
		PRE_REQUEST_ANNOTATIONS.add(ForbidsUserStates.class);
		PRE_REQUEST_ANNOTATIONS.add(RequiresUserStates.class);
		PRE_REQUEST_ANNOTATIONS.add(BypassesQuarantine.class);
		PRE_REQUEST_ANNOTATIONS.add(RequiresParameters.class);
		
		POST_REQUEST_ANNOTATIONS = new ArrayList<Class<? extends Annotation>>();
		POST_REQUEST_ANNOTATIONS.add(PostEnforceConditionsIfValidSession.class);
	}
	

	/**
	 * Evaluate any pre-request annotations (i.e. any annotations that do *not* begin with "Post") that are present on the specified controller class and method.
	 * 
	 * @param methodClass the controller class that processed the request.
	 * @param methodName the name of the controller method that will serve as the initial entry-point for the request.
	 * @param request the HTTP request.
	 * 
	 * @return true if all pre-request annotations pass validation, false otherwise.
	 */
	public static boolean validatePreRequestAnnotations(Class<? extends MultiActionController> methodClass, String methodName, HttpServletRequest request, HttpServletResponse response) {
		try {
			Method targetMethod = methodClass.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			Map<Class<? extends Annotation>, Annotation> annotations = mergeConstraintsFromClassAndMethod(methodClass, targetMethod);
			if (! processOrderedAnnotations(PRE_REQUEST_ANNOTATIONS, annotations, request, response)) {
				return false;
			}
			
			//process any un-ordered annotations that are left in the map
			return processPreRequestAnnotations(annotations, request, response);
			
		}
		catch (Throwable e) {
			//error during validation, validation failed
			LOG.error("Unexpected exception when validating pre-request annotations for method=" + methodName, e);
			return false;
		}
	}
	
	/**
	 * Evaluate any post-request annotations (i.e. annotations that begin with "Post") that are present on the specified controller class and method.
	 * 
	 * @param methodClass the controller class that processed the request.
	 * @param methodName the name of the controller method that served as the initial entry-point for the request.
	 * @param request the HTTP request.
	 * 
	 * @return true if all post-request annotations pass validation, false otherwise.
	 */
	public static boolean validatePostRequestAnnotations(Class<? extends MultiActionController> methodClass, String methodName, HttpServletRequest request, HttpServletResponse response) {
		try {
			Method targetMethod = methodClass.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			Map<Class<? extends Annotation>, Annotation> annotations = mergeConstraintsFromClassAndMethod(methodClass, targetMethod);
			if (! processOrderedAnnotations(POST_REQUEST_ANNOTATIONS, annotations, request, response)) {
				return false;
			}
			
			//process any un-ordered annotations that are left in the map
			return processPostRequestAnnotations(annotations, request, response);
		}
		catch (Throwable e) {
			//error during validation, validation failed
			LOG.error("Unexpected exception when validating post-request annotations for method=" + methodName, e);
			return false;
		}
	}
	
	/**
	 * Tests to see if the given method has the given annotation applied to it.  This check will evaluate 
	 * inheritance from the method's enclosing class, such that the method inherits any annotations that 
	 * are applied to the class.
	 * 
	 * @param method the method to check.
	 * @param annotationClass the annotation to check for.
	 * 
	 * @return true if the annotation is found, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static boolean doesMethodHaveAnnotation(Method method, Class<?> annotationClass) {
		if (method == null || annotationClass == null) {
			return false;
		}
		
		Map<Class<? extends Annotation>, Annotation> inheritedAnnotations = mergeConstraintsFromClassAndMethod((Class<? extends MultiActionController>)method.getDeclaringClass(), method);
		
		return inheritedAnnotations.containsKey(annotationClass);
	}
	
	/**
	 * Tests to see if the given class has the given annotation applies to it.
	 * 
	 * @param objectClass the class to check.
	 * @param annotationClass the annotation to check for.
	 * 
	 * @return true if the annotation is found, false otherwise.
	 */
	public static boolean doesClassHaveAnnotation(Class<?> objectClass, Class<?> annotationClass) {
		if (objectClass == null || annotationClass == null) {
			return false;
		}
		
		for (Annotation annotation : objectClass.getAnnotations()) {
			if (annotation.annotationType().equals(annotationClass)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets an annotation from the provided class.
	 * 
	 * @param objectClass the class to check.
	 * @param annotationClass the annotation to check for.
	 * 
	 * @return the specified annotation, or null if it is not found on the class provided.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotationFromClass(Class<?> objectClass, Class<T> annotationClass) {
		T result = null;
		for (Annotation annotation : objectClass.getAnnotations()) {
			if (annotation.annotationType().equals(annotationClass)) {
				result = (T)annotation;
			}
		}
		return result;
	}
	
	private static boolean processPreRequestAnnotations(Map<Class<? extends Annotation>, Annotation> annotations, HttpServletRequest request, HttpServletResponse response) {
		for (Class<? extends Annotation> key : annotations.keySet()) {
			if (! processAnnotation(annotations.get(key), request, response, true, false)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean processPostRequestAnnotations(Map<Class<? extends Annotation>, Annotation> annotations, HttpServletRequest request, HttpServletResponse response) {
		for (Class<? extends Annotation> key : annotations.keySet()) {
			if (! processAnnotation(annotations.get(key), request, response, false, true)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean processOrderedAnnotations(List<Class<? extends Annotation>> annotationsToProcess, Map<Class<? extends Annotation>, Annotation> annotations, HttpServletRequest request, HttpServletResponse response) {
		for (Class<? extends Annotation> key : annotationsToProcess) {
			if (! processAnnotation(annotations.get(key), request, response, true, true)) {
				return false;
			}
			annotations.remove(key);
		}
		
		return true;
	}
	
	private static boolean processAnnotation(Annotation annotation, HttpServletRequest request, HttpServletResponse response, boolean preRequest, boolean postRequest) {
		if (annotation == null) {
			//not there, return true
			return true;
		}
		AnnotationProcessor processor = null;
		for (Class<?> processorClass : annotation.annotationType().getDeclaredClasses()) {
			if (AnnotationProcessor.class.isAssignableFrom(processorClass)) {
				try {
					processor = (AnnotationProcessor)processorClass.newInstance();
					break;
				}
				catch (Exception ignored) {
					//couldn't create it, but maybe there is another inner class that also implements the required interface that we can construct, so keep going
				}
			}
		}
		if (processor != null) {
			if ((preRequest && processor.validatesBeforeExecution()) || (postRequest && processor.validatesAfterExecution())) {
				return processor.processRequest(annotation, request, response);
			}
		}
		
		//couldn't get a a processor and thus can't process the annotation, return true
		return true;
	}
	
	private static Map<Class<? extends Annotation>, Annotation> mergeConstraintsFromClassAndMethod(Class<? extends MultiActionController> methodClass, Method method) {
		Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
		
		//first load all the annotations that are on the class
		for (Annotation annotation : methodClass.getAnnotations()) {
			annotationMap.put(annotation.annotationType(), annotation);
		}
		
		//now merge with any annotations that are on the method; method annotations override class annotations if both objects contain annotations of the same type
		for (Annotation annotation : method.getAnnotations()) {
			annotationMap.put(annotation.annotationType(), annotation);
		}
		
		return annotationMap;
	}
}
