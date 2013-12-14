package au.com.suncoastpc.auth.spring.base;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import au.com.suncoastpc.auth.annotations.ForbidsLogin;
import au.com.suncoastpc.auth.filter.InputSanitizerFilter;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.EscapeUtility;

/**
 * Provides a handful of default method implementations that need to be accessible through any 
 * MultiActionController instance, namely login and indexPage.
 * 
 * @author Adam
 */
public abstract class BaseMethods extends MultiActionController implements MessageSourceAware {
	protected MessageSource localizedStrings;
	private static MessageSource staticLocalizedStrings;  //XXX:  minor hack
	
	/**
	 * Forward the user to the default landing page, depending upon whether or not they are currently logged in.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the index page if the user currently holds a valid session, or the login page otherwise.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public ModelAndView indexPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//if the user is logged in, send them to the post-login landing page, otherwise send them to the login page
		if (isUserLoggedIn(request)) {
			return new ModelAndView("index");
		}
		//no current user, go to login page
		return login(request, response);
	}
	
	/**
	 * Display the login page, or the use user is already logged in, go to the default landing page.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the login page, or the default landing page if the user is already logged in.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return new ModelAndView("login");
	}
	
	//utilities
	protected boolean isUserLoggedIn(HttpServletRequest request) {
		return request.getSession().getAttribute(Constants.SESSION_USER_KEY) != null;
	}
	
	protected void echoParamsAsAttribs(HttpServletRequest request, String... paramNames) {
		for (String name : paramNames) {
			request.setAttribute(name, EscapeUtility.escapeMarkupChars(request.getParameter(name)));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void echoQuarantinedParamsAsAttribs(HttpServletRequest request, String... paramNames) {
		Map<String, String> quarantine = (Map<String, String>)request.getAttribute(InputSanitizerFilter.QUARANTINE_ATTRIBUTE_NAME);
		for (String name : paramNames) {
			String value = quarantine.get(name);
			if (value != null) {
				request.setAttribute(name, EscapeUtility.escapeMarkupChars(value));
			}
		}
	}
	
	@Override
	public void setMessageSource(MessageSource source) {
		this.localizedStrings = source;
		staticLocalizedStrings = source;
	}
	
	public static MessageSource getMessageSource() {
		return staticLocalizedStrings;
	}
}
