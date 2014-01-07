package au.com.suncoastpc.auth.filter;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.Constants;
import au.com.suncoastpc.auth.util.CookieUtil;
import au.com.suncoastpc.auth.util.types.UserState;

@WebFilter("/*")
public class LoginFromCookieFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(LoginFromCookieFilter.class);

	@Override
	public void destroy() {
		//no work needed
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest)request;
		HttpServletResponse httpResp = (HttpServletResponse)response;
		
		String cookieValue = CookieUtil.getCookieValue(Constants.PERSISTENT_LOGIN_COOKIE_NAME, httpReq);
		if (cookieValue != null && httpReq.getSession().getAttribute(Constants.SESSION_USER_KEY) == null) {
			try {
				String[] parts = cookieValue.split("\\:");
				String email = parts[0];
				EntityManager em = DatabaseUtil.getEntityManager(httpReq);
				Query query = em.createNamedQuery("User.findByEmail");
				query.setParameter("email", email);
				
				User user = (User)query.getSingleResult();
				if (user.getStatus() == UserState.STATE_CONFIRMED && user.getAuthToken() != null 
						&& user.getAuthToken().equals(parts[1]) && httpReq.getSession().getAttribute(Constants.SESSION_USER_KEY) == null) {
					//valid request, make sure they are logged in and refresh their cookie
					em.getTransaction().begin();
					user.generateNewAuthToken();
					em.merge(user);
					em.getTransaction().commit();
					
					String newValue = user.getEmail() + ":" + user.getAuthToken();
					CookieUtil.setCookie(Constants.PERSISTENT_LOGIN_COOKIE_NAME, newValue, Constants.PERSISTENT_LOGIN_COOKIE_LIFETIME, httpResp);
					httpReq.getSession().setAttribute(Constants.SESSION_USER_KEY, user);
				}
				else if (user.getAuthToken() == null || ! user.getAuthToken().equals(parts[1])) {
					//the values in the cookie are no longer valid, get rid of it and end the user's session
					httpReq.getSession().removeAttribute(Constants.SESSION_USER_KEY);
					CookieUtil.removeCookie(Constants.PERSISTENT_LOGIN_COOKIE_NAME, httpReq, httpResp);
				}
			}
			catch (Throwable e) {
				LOG.warn("Unexpected error when attempting to log in using cookie data:  " + cookieValue, e);
			}
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		//no work needed
	}

}
