package au.com.suncoastpc.match.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import au.com.suncoastpc.auth.spring.base.BaseSpringController;

public class UserController extends BaseSpringController {
	private static final Map<String, String> PAGE_NAMES;
	
	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		PAGE_NAMES = new HashMap<String, String>();
		PAGE_NAMES.put("index", 			"Home");
		PAGE_NAMES.put("login", 			"Log In");
		PAGE_NAMES.put("addGame", 			"Add Game");
	}
	
	@Override
	protected Class<? extends MultiActionController> getControllerClass() {
		return UserMethods.class;
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
