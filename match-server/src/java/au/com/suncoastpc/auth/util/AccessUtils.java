package au.com.suncoastpc.auth.util;

import javax.servlet.http.HttpServletRequest;

import au.com.suncoastpc.auth.db.User;
import au.com.suncoastpc.auth.util.types.UserState;

public class AccessUtils {
	private static final CircularArray<Long> LOCKS = new CircularArray<Long>(Configuration.getNumAccountLocks());
	
	public static Object getAccountLock(HttpServletRequest request) {
		User account = getCurrentAccount(request);
		Long lock = new Long(account.getId());
		if (! LOCKS.contains(lock)) {
			LOCKS.add(lock);
		}
		
		return LOCKS.instanceEqualTo(lock);
	}
	
	public static boolean doesUserHaveForbiddenState(User user, UserState[] states) {
		for (UserState forbiddenState : states) {
			if (user != null && user.getStatus() == forbiddenState) {
				//user is set to an invalid state, validation failed
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean doesUserHaveRequiredState(User user, UserState[] states) {
		if (user == null && states.length > 0) {
			//user is not logged in and thus cannot have the required state, validation failed
			return false;
		}
		if (states.length == 0) {
			//no required states were specified, validation passed
			return true;
		}
		
		boolean hasRequiredState = false;
		for (UserState requiredState : states) {
			if (user.getStatus() == requiredState) {
				hasRequiredState = true;
				break;
			}
		}
		return hasRequiredState;
	}
	
	private static User getCurrentAccount(HttpServletRequest request) {
		return getCurrentUser(request);
	}
	
	public static User getCurrentUser(HttpServletRequest request) {
		 return (User)request.getSession().getAttribute(Constants.SESSION_USER_KEY);
	}
}
