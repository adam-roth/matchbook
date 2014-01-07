package au.com.suncoastpc.auth.spring.base;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

import au.com.suncoastpc.auth.annotations.SynchronizedPerAccount;
import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.AccessUtils;
import au.com.suncoastpc.auth.util.AnnotationUtils;
import au.com.suncoastpc.auth.util.Configuration;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.OverridableHttpRequest;
import au.com.suncoastpc.auth.util.StringUtilities;

/**
 * Provides a common starting-point for adding additional spring controllers to the webapp.  This class 
 * provides default behavior supporting pre- and post-request authorization and other commonly required 
 * tasks.  Subclasses only need to specify their desired MultiActionController, its configured mapping 
 * parameter, and (optionally) user-friendly names for each view made accessible view their associated 
 * MultiActionController. 
 * 
 * @author Adam
 */
public abstract class BaseSpringController extends DispatcherServlet {
	private static final Logger LOG = Logger.getLogger(BaseSpringController.class);
	
	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<Locale, Map<String, String>> LOCALIZED_STRINGS = new HashMap<Locale, Map<String, String>>();
	
	static {
		//make log4j work
		BasicConfigurator.resetConfiguration();
		Layout layout = new PatternLayout("%d{HH:mm:ss} [%C{1}] %p:  %m%n");
		Appender appender = new ConsoleAppender(layout);
		appender.addFilter(new LogFilter());
		BasicConfigurator.configure(appender);
	}
	
	@Override
	public void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//do any required pre-processing here, expose any session-wide state that the application needs to process a request
		if (Configuration.getServerRequiresSecureConnection() && ! request.isSecure()) {
			//if our configuration requires HTTPS and the request is not using HTTPS, redirect it
			String url = request.getRequestURL().substring(request.getRequestURL().indexOf("://") + "://".length());
			String path = url.contains("/") ? url.substring(url.indexOf("/")) : "";
			String secureUrl = Configuration.getServerAddress() + path + "?" + request.getQueryString();
			LOG.warn("Redireting insecure request to:  " + secureUrl);
			response.sendRedirect(secureUrl);
			return;
		}
		
		//validate any pre-request authorization constraints, dump the user to the default landing page if they fail
		OverridableHttpRequest overriddenRequest = new OverridableHttpRequest(request);
		String method = request.getParameter(this.getMethodMappingParamName());
		if (! AnnotationUtils.validatePreRequestAnnotations(this.getControllerClass(), method, overriddenRequest, response)) {
			if (request.getSession().getAttribute(Constants.SESSION_USER_KEY) == null) {
				//user is not logged in, so remember their desired URL so that we can redirect them to it if/when they log in
				overriddenRequest.setAttribute(Constants.POST_LOGIN_URL_KEY, getUrl(request));
			}
			overriddenRequest.setParameter(this.getMethodMappingParamName(), Constants.DEFAULT_REQUEST_METHOD);
		}
		
		//ensure that the user in the session is always up to date
		User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
		if (user != null) {
			EntityManager em = DatabaseUtil.getEntityManager(overriddenRequest);
			user = em.find(User.class, user.getId());
			request.getSession().setAttribute(Constants.SESSION_USER_KEY, user);
		}
		
		request = overriddenRequest;
		
		try {
			Method serviceMethod = this.getControllerClass().getMethod(method, HttpServletRequest.class, HttpServletResponse.class);
			if (AnnotationUtils.doesMethodHaveAnnotation(serviceMethod, SynchronizedPerAccount.class)) {
				
				//this will cause the remainder of the request processing to be synchronized
				synchronized(AccessUtils.getAccountLock(request)) {
					super.doService(request, response);
				}
			}
			else {
				//don't need to execute synchronously
				super.doService(request, response);
			}
		}
		catch (Exception ignored) {
			//couldn't find the target API method or something else went wrong, just try to proceed normally
			super.doService(request, response);
		}
	}
	
	@Override
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		//do any required post-processing here, expose any commonly-needed params to the destination view
		
		//validate any post-request authorization constraints, dump the user to the login page and end their session if they fail
		String method = request.getParameter(this.getMethodMappingParamName());
		if (! AnnotationUtils.validatePostRequestAnnotations(this.getControllerClass(), method, request, response)) {
			request.setAttribute("error", "Your request could not be processed, please try again");
			request.getSession().removeAttribute(Constants.SESSION_USER_KEY);
			mv = new ModelAndView("login");
		}
		
		//if the client requested a specific data format, redirect to the correct view here
		String format = request.getParameter("format");
		String viewPrefix = format == null ? "" : format + "/";
		mv.setViewName(viewPrefix + mv.getViewName());
		
		//IE stupidly caches AJAX requests, so set some headers to tell it not to
		if ("ajax".equals(format) || "json".equals(format)) {
			response.addHeader("Cache-Control", "max-age=0,no-cache,no-store,post-check=0,pre-check=0");
			response.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		}
		
		//if the user is logged in, ensure that they are always in sync with the database before we try to render
		EntityManager em = DatabaseUtil.getEntityManager(request);
		User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
		if (user != null) {
			user = em.find(User.class, user.getId());  //ensure that the user is always up to date
		}
		
		//expose the localized strings appropriate to the current locale
		Locale locale = RequestContextUtils.getLocale(request);
		Map<String, String> bundle = LOCALIZED_STRINGS.get(locale);
		if (bundle == null) {
			//we haven't processed this locale yet
			bundle = new HashMap<String, String>();
			ResourceBundle resources = ResourceBundle.getBundle("localization.messages_en_US");
			MessageSource localizer = BaseMethods.getMessageSource();
			Enumeration<String> keys = resources.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				try {
					bundle.put(key, localizer.getMessage(key, null, locale));
				}
				catch (Throwable ignored) {
					//exceptions may be thrown if a given bundle does not contain all the keys defined in the en_US one, and for any string that requires params, it is safe to ignore these
				}
			}
			LOCALIZED_STRINGS.put(locale, bundle);
		}
		request.setAttribute("messages", bundle);
		
		request.setAttribute("pageName", this.getPageNameForView(mv));							  	  //expose the user-friendly name for the current view
		request.setAttribute("user", user);  //expose the current user, if one exists
		request.setAttribute("serverHome", Configuration.getServerAddress());						  //expose the server's home address
		
		Exception rethrow = null;
		try {
			super.render(mv, request, response);
		}
		catch (Exception ignored) {
			//nothing we can do at this point, just remember the exception to re-throw later
			rethrow = ignored;
		}
		finally {
			//finally, close the entity-manager for this request, if one is open
			if (em != null) {
				em.close();
			}
		}
		
		//now the the entity-manager is closed, re-throw the exception if we triggered one earlier so that Spring can deal with it
		if (rethrow != null) {
			throw rethrow;
		}
	}
	
	//interface for subclasses to implement
	/**
	 * @return the MultiActionController class that should be used to handle requests that are mapped through this DispatcherServlet.  For 
	 *         instance 'AdminMethods.class' for the admin controller.
	 */
	protected abstract Class<? extends MultiActionController> getControllerClass();
	
	/**
	 * @return the name of the request parameter used to specify which MultiActionController method to invoke.  This must match what has been 
	 *         configured for this dispatcher in spring-servlet.xml.  The standard value is "method".  
	 */
	protected abstract String getMethodMappingParamName();
	
	/**
	 * Used to look up a user-friendly display name for the destination view.
	 * 
	 * @param mv the ModelAndView that will be rendered.
	 * 
	 * @return a String describing the destination view in a user-friendly way, like "Log In", "Register", "Reset Password", etc.. 
	 */
	protected abstract String getPageNameForView(ModelAndView mv);
	
	private String getUrl(HttpServletRequest req) {
		String paramString = "";
		for (String paramName : req.getParameterMap().keySet()) {
			if (! "".equals(paramString)) {
				paramString += "&";
			}
			paramString += paramName + "=" + req.getParameter(paramName);
		}
		
	    String reqUrl = req.getRequestURL().toString();  //base url
	    //String queryString = req.getQueryString();       //query params		//FIXME:  only returns the first parameter for some reason; possible bug in OverridableHttpServletRequest?
	    if (! StringUtilities.isEmpty(paramString)) {
	    	reqUrl += "?" + paramString;
	    }
	    return reqUrl;
	}
	
	private static class LogFilter extends Filter {
		/**
		 * Constructor
		 */
		public LogFilter() {
			//no initialization needed
		}
		
		@Override
		public int decide(LoggingEvent event) {
			//modify this to exclude different types/levels of logger messages
			if (event.getLocationInformation().getClassName().contains("au.com.suncoastpc")) {
				//log all internal messages
				return ACCEPT;
			}
			if (event.getLevel().toInt() >= Level.WARN_INT) {
				//log warning and above from all classes
				return ACCEPT;
			}
			
			//ignore any other messages
			return DENY;
		}
	}
}
