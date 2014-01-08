package au.com.suncoastpc.auth.spring.base;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import au.com.suncoastpc.auth.annotations.ForbidsLogin;
import au.com.suncoastpc.auth.annotations.RequiresParameters;
import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.db.UserDAO;
import au.com.suncoastpc.auth.filter.InputSanitizerFilter;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.EscapeUtility;
import au.com.suncoastpc.auth.util.types.UserState;

/**
 * Provides a handful of default method implementations that need to be accessible through any 
 * MultiActionController instance, namely login and indexPage.
 * 
 * @author Adam
 */
public abstract class BaseMethods extends MultiActionController implements MessageSourceAware {
	protected MessageSource localizedStrings;
	private static MessageSource staticLocalizedStrings;  //XXX:  minor hack
	
	@ForbidsLogin
	public ModelAndView setupAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		if (UserDAO.countAll(em) > 0) {
			//if there's a user already, this method is disabled
			return indexPage(request, response);
		}
		
		return new ModelAndView("setupAdmin");
	}
	
	@ForbidsLogin
	@RequiresParameters(paramNames= {"pass", "conf"}, redirectTo="indexPage")
	public ModelAndView submitAdminPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		if (UserDAO.countAll(em) > 0) {
			//if there's a user already, this method is disabled
			return indexPage(request, response);
		}
		
		String pass = request.getParameter("pass");
		if (pass.equals(request.getParameter("conf"))) {
			//create the admin account
			em.getTransaction().begin();
			
			User admin = new User();
			admin.setEmail("admin");
			admin.setName("Matchbook Admin");
			admin.setStatus(UserState.STATE_CONFIRMED);
			admin.setTrustLevel(Constants.TRUST_LEVEL_ADMIN);
			admin.setPassword(pass);
			
			em.persist(admin);
			
			em.getTransaction().commit();
			
			//log into the admin account
			request.getSession().setAttribute(Constants.SESSION_USER_KEY, admin);
		}
		else {
			request.setAttribute("error", "Passwords must match!");
			return indexPage(request, response);
		}
		
		return indexPage(request, response);
	}
	
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
		EntityManager em = DatabaseUtil.getEntityManager(request);
		if (UserDAO.countAll(em) < 1) {
			//no users yet, so the first order of business is to bootstrap the admin account
			return setupAdmin(request, response);
		}
		
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
