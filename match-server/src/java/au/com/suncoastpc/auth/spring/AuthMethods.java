package au.com.suncoastpc.auth.spring;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import au.com.suncoastpc.auth.annotations.BypassesQuarantine;
import au.com.suncoastpc.auth.annotations.ForbidsLogin;
import au.com.suncoastpc.auth.annotations.LogsServiceTime;
import au.com.suncoastpc.auth.annotations.PostEnforceConditionsIfValidSession;
import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.spring.base.BaseMethods;
import au.com.suncoastpc.auth.util.Configuration;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.CookieUtil;
import au.com.suncoastpc.auth.util.EmailUtility;
import au.com.suncoastpc.auth.util.StringUtilities;
import au.com.suncoastpc.auth.util.types.UserState;

/**
 * This class contains implementation/controller logic for all external-facing entry points/API calls.
 * 
 * @author Adam
 */
@LogsServiceTime
public class AuthMethods extends BaseMethods {
	private static final Logger LOG = Logger.getLogger(AuthMethods.class);
	
	/**
	 * Attempt to activate a new user.  Transitions their account to the "Confirmed" state if the operation succeeds.
	 * 
	 * @param request
	 * @param response
	 * 
	 * @return the login page if the operation fails, or the default post-login page if it succeeds.
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView activateUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		String email = request.getParameter("user");
		String token = request.getParameter("auth");
		
		User user = getUserForEmail(email, em);
		if (user == null || user.getStatus() != UserState.STATE_INACTIVE) {
			//no matching user found, or user already activated, go to login page
			return indexPage(request, response);
		}
		
		em.getTransaction().begin();
		if (! user.activateUserWithToken(token)) {
			//invalid token
			em.getTransaction().rollback();
			return indexPage(request, response);
		}
		
		//successful activation, log the user in
		em.merge(user);
		em.getTransaction().commit();
		request.getSession().setAttribute(Constants.SESSION_USER_KEY, user);
		return indexPage(request, response);
	}
	
	/**
	 * Processes a login submission.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the login page with an appropriate error message if the request fails, or the default landing page if the request is successful.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	@PostEnforceConditionsIfValidSession(requiredStates = {UserState.STATE_CONFIRMED})
	@BypassesQuarantine(paramNames = {"pass", Constants.POST_LOGIN_URL_KEY})
	public ModelAndView submitLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String pass = request.getParameter("pass");
		
		echoParamsAsAttribs(request, "email", "remember", Constants.POST_LOGIN_URL_KEY);
		echoQuarantinedParamsAsAttribs(request, "email");
		
		EntityManager em = DatabaseUtil.getEntityManager(request);
		User user = getUserForEmail(email, em);
		if (user == null || user.getStatus() == UserState.STATE_INACTIVE) {
			//invalid username
			request.setAttribute("error", "User not found");
			return login(request, response);
		}
		
		try {
			if (user.checkPasswordForLogin(pass)) {
				//valid login
				request.getSession().setAttribute(Constants.SESSION_USER_KEY, user);
				
				em.getTransaction().begin();
				if (request.getParameter("remember") != null) {
					//user wants their login details remembered automatically
					user.generateNewAuthToken();
					String cookieValue = user.getEmail() + ":" + user.getAuthToken();
					CookieUtil.setCookie(Constants.PERSISTENT_LOGIN_COOKIE_NAME, cookieValue, Constants.PERSISTENT_LOGIN_COOKIE_LIFETIME, response);
				}
				else {
					//they don't want to be remembered, so ensure that their auth token is null
					user.setAuthToken(null);
				}
				
				if (user.getStatus() == UserState.STATE_REQUESTED_PASSWORD_RESET) {
					//they're logging in, must have remembered their password
					user.setStatus(UserState.STATE_CONFIRMED);
				}
				
				em.merge(user);
				em.getTransaction().commit();
				
				if (request.getParameter(Constants.POST_LOGIN_URL_KEY) != null) {
					//redirect the user to a specific post-login URL
					String nextUrl = request.getParameter(Constants.POST_LOGIN_URL_KEY);
					LOG.debug("Redirecting user to:  " + nextUrl);
					response.sendRedirect(nextUrl);
					return null;
				}
				
				//send the user to the default page
				response.sendRedirect("/r/indexPage");
				return null;
			}
			else {
				//invalid password
				request.setAttribute("error", "Incorrect password");
				return login(request, response);
			}
		}
		catch (Exception e) {
			LOG.error("Login processing failed!", e);
			request.setAttribute("error", "Cannot generate password hash?!?!?");
			return login(request, response);
		}
	}
	
	/**
	 * Logs the user out.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the the login page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
		if (user != null && user.getAuthToken() != null) {
			//clear the token
			EntityManager em = DatabaseUtil.getEntityManager(request);
			em.getTransaction().begin();
			user.setAuthToken(null);
			em.merge(user);
			em.getTransaction().commit();
		}
		
		request.getSession().removeAttribute(Constants.SESSION_USER_KEY);  //remove the user from the session
		CookieUtil.removeCookie(Constants.PERSISTENT_LOGIN_COOKIE_NAME, request, response);  //kill the login cookie if they had one
		return login(request, response);
	}
	
	/**
	 * Displays the registration page, unless the user is already logged in, in which case displays the default landing page.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the registration page if the user is currently logged out, otherwise the default landing page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return new ModelAndView("register");
	}
	
	/**
	 * Processes a registration submission.  Logs the user in if the registration is successful.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the registration page with an appropriate error message if the request fails, otherwise the default landing page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	@BypassesQuarantine(paramNames = {"pass", "conf"})
	public ModelAndView submitRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String pass = request.getParameter("pass");
		String conf = request.getParameter("conf");
		String name = request.getParameter("name");
		
		echoParamsAsAttribs(request, "email", "name");
		echoQuarantinedParamsAsAttribs(request, "email", "name");
		
		if (StringUtilities.isEmpty(email)) {
			//required field is missing
			request.setAttribute("error", "Invalid e-mail address");
			return register(request, response);
		}
		if (StringUtilities.isEmpty(name)) {
			//required field is missing
			request.setAttribute("error", "Invalid name");
			return register(request, response);
		}
		
		User user = getUserForEmail(email, DatabaseUtil.getEntityManager(request));
		if (user != null) {
			//invalid username
			request.setAttribute("error", "User already exists");
			return register(request, response);
		}
		
		if (StringUtilities.isEmpty(pass) || ! pass.equals(conf)) {
			//passwords don't match
			request.setAttribute("error", "Passwords don't match");
			return register(request, response);
		}
		
		//everything looks okay, so create the new user
		try {
			EntityManager em = DatabaseUtil.getEntityManager(request);
			
			user = new User();
			user.setName(name);
			user.setEmail(email);
			user.setPassword(pass);
			user.setStatus(UserState.STATE_INACTIVE);
			user.generateNewAuthToken();
			
			em.getTransaction().begin();
			em.persist(user);
			
			//user created successfully, e-mail them an activation link
			String message = "Link:  " + Configuration.getServerAddress() + "/r/activate/" + user.getAuthToken() + "?user=" + user.getEmail();
			boolean validEmail = EmailUtility.sendEmail(Configuration.getAdminEmailAddress(), user.getEmail(), Constants.REGISTRATION_EMAIL_SUBJECT, message, null, user.getEmail());
			
			if (validEmail) {
				em.getTransaction().commit();
				request.removeAttribute("email");
				request.setAttribute("message", "Thank you for registering, we have sent you an e-mail containing an activation link.  You must activate your account by clicking on the link before you can log in.");
				return indexPage(request, response);
			}
			
			//invalid e-mail/couldn't send e-mail
			em.getTransaction().rollback();
			request.setAttribute("error", "Invalid e-mail address");
			return register(request, response);
		}
		catch (Exception e) {
			LOG.error("User registration failed!", e);
			request.setAttribute("error", "Could not add user?!?!?");
			return register(request, response);
		}
	}
	
	/**
	 * Displays the reset-password page, unless the user is already logged in, then takes them to the default landing page.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the reset-password page if the user is currently logged out, otherwise the default landing page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView forgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return new ModelAndView("forgotPass");
	}
	
	/**
	 * Processes a reset-password attempt if the user is logged out, otherwise goes to the default landing page.  If a valid username is 
	 * entered than an authToken will be generated for that user and a link will be sent to them to allow them to reset their password.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return a confirmation page if the request is successful, otherwise the reset password page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView submitForgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		String email = request.getParameter("email");
		
		echoParamsAsAttribs(request, "email");
		echoQuarantinedParamsAsAttribs(request, "email");
		
		User user = getUserForEmail(email, em);
		if (user == null) {
			//user does not exist, or has not been activated yet
			request.setAttribute("error", "Invalid user");
			return forgotPassword(request, response);
		}
		if (user.getAuthToken() != null && user.getStatus() == UserState.STATE_REQUESTED_PASSWORD_RESET) {
			//an e-mail has already been sent for this user, do not send one again
			request.setAttribute("error", "Email already sent, please check your e-mail account");
			return forgotPassword(request, response);
		}
		
		em.getTransaction().begin();
		user.generateNewAuthToken();
		String message = "Reset password link:  " + Configuration.getServerAddress() + "/r/newPass/" + user.getAuthToken() + "?user=" + user.getEmail();
		boolean emailSent = EmailUtility.sendEmail(Configuration.getAdminEmailAddress(), user.getEmail(), Constants.RESET_PASSWORD_EMAIL_SUBJECT, message, null, user.getEmail());
		
		if (! emailSent) {
			//couldn't send the email, rollback the changes
			em.getTransaction().rollback();
			request.setAttribute("error", "Failed to send e-mail, please try again in a few minutes");
			return forgotPassword(request, response);
		}
		
		//message sent, save changes
		user.setStatus(UserState.STATE_REQUESTED_PASSWORD_RESET);
		em.merge(user);
		em.getTransaction().commit();
		return new ModelAndView("forgotPassConfirm");
		
	}
	
	/**
	 * Displays a page that lets the user choose a new password if they are not logged in and if they provide a valid auth token. 
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return a page for choosing a new password if the user's request is valid, otherwise the default landing page.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	public ModelAndView newPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		String email = request.getParameter("user");
		String token = request.getParameter("auth");
		
		User user = getUserForEmail(email, em);
		if (user == null) {
			//invalid request
			return indexPage(request, response);
		}
		
		em.getTransaction().begin();
		if (! user.activateUserWithToken(token)) {
			//incorrect token
			em.getTransaction().rollback();
			return indexPage(request, response);
		}
		
		//valid token, the user can provide a new password
		user.generateNewFormAuthToken();
		em.merge(user);
		em.getTransaction().commit();
		
		request.setAttribute("email", user.getEmail());
		request.setAttribute("userId", user.getId());
		request.setAttribute("token", user.getFormAuthToken());
		return new ModelAndView("newPass");
	}
	
	/**
	 * Processes the submission of a new password from the user. 
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return the post-login page if the user's request is successful, othersise the choose password page with an appropriate error message.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@ForbidsLogin
	@BypassesQuarantine(paramNames = {"pass", "conf"})
	public ModelAndView submitNewPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		String userIdString = request.getParameter("userId");
		String formToken = request.getParameter("token");
		String pass = request.getParameter("pass");
		String conf = request.getParameter("conf");
		
		echoParamsAsAttribs(request, "email", "userId", "token");
		
		if (pass == null || ! pass.equals(conf)) {
			//invalid password
			request.setAttribute("error", "Password don't match");
			return new ModelAndView("newPass");
		}
		
		User user = null;
		try {
			Long userId = Long.parseLong(userIdString);
			user = em.find(User.class, userId);
			user.getEmail();  //will throw a NPE if the user is null, triggering the exception handler below
		}
		catch (Exception e) {
			//invalid user
			return indexPage(request, response);
		}
		
		em.getTransaction().begin();
		if (! user.validateRequestWithToken(formToken)) {
			//invalid token
			em.getTransaction().rollback();
			return indexPage(request, response);
		}
		
		//valid request, store the new password
		try {
			user.setPassword(pass);
		}
		catch (Exception e) {
			LOG.error("Unexpected error when trying to set user password!", e);
			em.getTransaction().rollback();
			request.setAttribute("error", "Unrecoverable error when updating password, please try again in a few minutes");
			return new ModelAndView("newPass");
		}
		
		//everything is good, persist the modifications and log the user in
		user.setStatus(UserState.STATE_CONFIRMED);
		em.merge(user);
		em.getTransaction().commit();
		request.getSession().setAttribute(Constants.SESSION_USER_KEY, user);
		return indexPage(request, response);
	}
	
	//utility methods
	private User getUserForEmail(String email, EntityManager em) {
		User user = null;
		try {
			Query query = em.createNamedQuery("User.findByEmail");
			query.setParameter("email", email.toLowerCase());
			user = (User)query.getSingleResult();
		}
		catch (Throwable ignored) { }
		
		return user;
	}
}
