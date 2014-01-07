package au.com.suncoastpc.auth.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.OverridableHttpRequest;

public class InputSanitizerFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(InputSanitizerFilter.class);
	
	private static final String BANNED_INPUT_CHARS = ".*[^a-zA-Z0-9\\@\\'\\,\\.\\/\\(\\)\\+\\=\\-\\_\\[\\]\\{\\}\\^\\!\\*\\&\\%\\$\\:\\;\\? \\t]+.*";
	
	public static final String QUARANTINE_ATTRIBUTE_NAME = "suncoast.auth.quarantined.params";
	public static final String SUSPICIOUS_REQUEST_FLAG_NAME = "suncoast.auth.suspicious.request";

	@Override
	public void destroy() {
		//no work necessary
	}

	//@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		OverridableHttpRequest newRequest = new OverridableHttpRequest((HttpServletRequest)request);
		Map<String, String> quarantine = new HashMap<String, String>();
		newRequest.setAttribute(QUARANTINE_ATTRIBUTE_NAME, quarantine);
		
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String value = request.getParameter(name);
			//LOG.debug("Inspecting param:  " + name + "=" + value);
			if (value.matches(BANNED_INPUT_CHARS)) {
				LOG.warn("Removing potentially malicious parameter from request:  " + name);
				quarantine.put(name, value);
				newRequest.removeParameter(name);
				newRequest.setAttribute(SUSPICIOUS_REQUEST_FLAG_NAME, "true");
			}
		}
		
		chain.doFilter(newRequest, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		//no work necessary
	}

}
