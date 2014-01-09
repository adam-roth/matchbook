package au.com.suncoastpc.auth.spring;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import au.com.suncoastpc.auth.annotations.LogsServiceTime;
import au.com.suncoastpc.auth.annotations.RequiresTrustLevel;
import au.com.suncoastpc.auth.db.DatabaseUtil;
import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.spring.base.BaseMethods;
import au.com.suncoastpc.auth.util.Configuration;
import au.com.suncoastpc.auth.util.Constants;

@LogsServiceTime
@RequiresTrustLevel(minimumTrust = Constants.TRUST_LEVEL_ADMIN)  //admin access only
public class AdminMethods extends BaseMethods {
	private static final Logger LOG = Logger.getLogger(AdminMethods.class);
	
	/**
	 * Displays a list of all current users.
	 *
	 * @param request the http request object.
	 * @param response the http response object.
	 *
	 * @return a list containing all of the current users in the system.
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public ModelAndView listUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		Query query = em.createNamedQuery("User.findAll");
		
		List<User> users = query.getResultList();
		request.setAttribute("users", users);
		return new ModelAndView("listUsers");
	}
	
	public ModelAndView promoteUserToAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EntityManager em = DatabaseUtil.getEntityManager(request);
		long userId = Long.parseLong(request.getParameter("userId"));
		User user = em.find(User.class, userId);
		
		em.getTransaction().begin();
		user.setTrustLevel(Constants.TRUST_LEVEL_ADMIN);
		em.merge(user);
		em.getTransaction().commit();
		
		return listUsers(request, response);
	}
	
	public ModelAndView adminLinks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return new ModelAndView("adminLinks");
	}
	
	public ModelAndView manageConfiguration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<Method> settableFields = new ArrayList<Method>();
		for (Method method : Configuration.class.getMethods()) {
			if (method.getName().startsWith("set")) {
				settableFields.add(method);
			}
		}
		
		//for each setter exposed by the Configuration class, get its full name, the name of its backing field, and the current value of that field
		List<String> fieldNames = new ArrayList<String>();
		List<String> fieldValues = new ArrayList<String>();
		List<String> fieldLabels = new ArrayList<String>();
		for (Method method : settableFields) {
			fieldNames.add(method.getName());
			fieldLabels.add(method.getName().substring(3));
			try {
				Method getter = Configuration.class.getMethod("g" + method.getName().substring(1), (Class<?>[])null);
				fieldValues.add(getter.invoke(null, (Object[])null).toString());
			} 
			catch (Exception ignored) {
			} 
		}
		
		request.setAttribute("fieldNames", fieldNames);
		request.setAttribute("fieldValues", fieldValues);
		request.setAttribute("fieldLabels", fieldLabels);
		return new ModelAndView("manageConfiguration");
	}
	
	//@SuppressWarnings("unchecked")
	public ModelAndView submitManageConfiguration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("resetConfiguration") != null) {
			//set all settable Configuration fields back to null to restore their initial default value
			LOG.warn("Resetting configuration to default values...");
			for (Method method : Configuration.class.getMethods()) {
				if (method.getName().startsWith("set")) {
					Object[] param = {(String)null};
					try {
						method.invoke(null, param);
					} catch (Exception e) {
						LOG.error("Unexpected exception when resetting configuration settings to defaults:  " + e.getMessage(), e);
					}
				}
			}
		}
		else {
			//for each submitted parameters, invoke its corresponding setter method in the Configuration class
			LOG.warn("Updating server configuration settings...");
			Enumeration<String> paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();
				if (name.startsWith("set")) {
					String value = request.getParameter(name);
					try {
						Object[] param = {value};
						Method setter = Configuration.class.getMethod(name, String.class);
						setter.invoke(null, param);
					} catch (Exception e) {
						LOG.warn("Unexpected exception when trying to update configuration:  field=" + name + ", value=" + value, e);
					}
				}
			}
		}
		
		return manageConfiguration(request, response);
	}
}
