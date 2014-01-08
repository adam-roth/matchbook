package au.com.suncoastpc.auth.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import au.com.suncoastpc.auth.util.Configuration;

public class HttpSessionTimeoutFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(HttpSessionTimeoutFilter.class);

	@Override
	public void destroy() {
		//nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//FIXME:  ugly hack; LoginFromCookieFilter seems to do very bizarre and unspecified things, so we run it manually from here
		new LoginFromCookieFilter().doFilter(request, response, new FilterChain() {
			@Override
			public void doFilter(ServletRequest arg0, ServletResponse arg1) throws IOException, ServletException {
			}
		});
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest)request;
			req.getSession().setMaxInactiveInterval(Configuration.getHttpTimeoutMinutes() * 60); //XXX:  inactivity interval is specified in seconds
		}
		else {
			LOG.warn("Got a request that isn't an HttpServletRequest...not sure what's going on; request type is '" + request.getClass().getName() + "'");
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		//nothing to do
	}

}
