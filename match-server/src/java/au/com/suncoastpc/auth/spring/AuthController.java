package au.com.suncoastpc.auth.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import au.com.suncoastpc.auth.spring.base.BaseSpringController;

/**
 * The entry and exit point for servicing auth-related requests.  This class can perform various pre- and post-processing tasks 
 * as desired.  For instance by ensuring that the current user (or any other bits of session-wide state) is exposed 
 * to the destination view in a uniform and consistent way.
 * 
 * @author Adam
 */
public class AuthController extends BaseSpringController {
	private static final Map<String, String> PAGE_NAMES;
	
	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		PAGE_NAMES = new HashMap<String, String>();
		PAGE_NAMES.put("index", 			"Home");
		PAGE_NAMES.put("login", 			"Log In");
		PAGE_NAMES.put("forgotPass", 		"Forgot Password");
		PAGE_NAMES.put("forgotPassConfirm",	"Password Email Sent");
		PAGE_NAMES.put("newPass", 			"Reset Password");
		PAGE_NAMES.put("register", 			"Register");
	}
	
	@Override
	protected Class<? extends MultiActionController> getControllerClass() {
		return AuthMethods.class;
	}
	
	@Override
	protected String getMethodMappingParamName() {
		return "method";
	}
	
	@Override
	protected String getPageNameForView(ModelAndView mv) {
		return PAGE_NAMES.get(mv.getViewName());
	}	
}
