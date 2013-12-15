package au.com.suncoastpc.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

public class OverridableHttpRequest implements HttpServletRequest {
	
private static final Logger LOG = Logger.getLogger(OverridableHttpRequest.class);
	
	private HttpServletRequest wrappedRequest;
	private Map<String, String> newParams;
	private Set<String> removedParams;
	private ResettableServletInputStream wrappedStream;
	
	public OverridableHttpRequest(HttpServletRequest requestToWrap) throws IOException {
		this.wrappedRequest = requestToWrap;
		this.newParams = new HashMap<String, String>();
		this.removedParams = new HashSet<String>();
		this.wrappedStream = null;//new ResettableServletInputStream(requestToWrap.getInputStream());
	}
	
	//these things we add so that params can be overridden
	public void setParameter(String name, String value) {
		this.removedParams.remove(name);
		this.newParams.put(name, value);
	}
	
	public void removeParameter(String name) {
		this.newParams.remove(name);
		this.removedParams.add(name);
	}
	
	
	//these things we need to override so that the correct state is exposed through the standard API
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getParameterNames() {
		Set<String> result = new HashSet<String>();
		Enumeration requestParams = this.wrappedRequest.getParameterNames();
		while(requestParams.hasMoreElements()) {
			Object param = requestParams.nextElement();
			if (! removedParams.contains(param)) {
				result.add((String)param);
			}
		}
		result.addAll(newParams.keySet());
		
		return Collections.enumeration(result);
	}

	@Override
	public String[] getParameterValues(String arg0) {
		String[] result = new String[1];
		result[0] = this.getParameter(arg0);
		
		return result;
	}
	
	@Override
	public String getParameter(String arg0) {
		if (removedParams.contains(arg0)) {
			return null;
		}
		if (newParams.containsKey(arg0)) {
			return newParams.get(arg0);
		}
		return this.wrappedRequest.getParameter(arg0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getParameterMap() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		for (Object key : this.wrappedRequest.getParameterMap().keySet()) {
			result.put((String)key, (String[])this.wrappedRequest.getParameterMap().get(key));
		}
		for (String key : this.newParams.keySet()) {
			result.put(key, new String[] {this.newParams.get(key)});
		}
		for (String key : this.removedParams) {
			result.remove(key);
		}
		
		return result;
	}
	
	
	//these things we should probably override but don't right now
	@Override
	public String getRequestURI() {
		//FIXME:  should return a modified URI based upon current state
		return this.wrappedRequest.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		//FIXME:  should return a modified URL based upon current state
		return this.wrappedRequest.getRequestURL();
	}
	
	@Override
	public String getQueryString() {
		//FIXME:  should return a modified String based upon current state
		return this.wrappedRequest.getQueryString();
	}
	
	
	//everything else just passes through
	@Override
	public Object getAttribute(String arg0) {
		return this.wrappedRequest.getAttribute(arg0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getAttributeNames() {
		return this.wrappedRequest.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding() {
		return this.wrappedRequest.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return this.wrappedRequest.getContentLength();
	}

	@Override
	public String getContentType() {
		return this.wrappedRequest.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.wrappedStream == null) {
			this.wrappedStream = new ResettableServletInputStream(this.wrappedRequest.getInputStream());
		}
		return this.wrappedStream;//this.wrappedRequest.getInputStream();
	}

	@Override
	public String getLocalAddr() {
		return this.wrappedRequest.getLocalAddr();
	}

	@Override
	public String getLocalName() {
		return this.wrappedRequest.getLocalName();
	}

	@Override
	public int getLocalPort() {
		return this.wrappedRequest.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		return this.wrappedRequest.getLocale();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getLocales() {
		return this.wrappedRequest.getLocales();
	}

	@Override
	public String getProtocol() {
		return this.wrappedRequest.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return this.wrappedRequest.getReader();//new BufferedReader(new InputStreamReader(this.getInputStream()));//this.wrappedRequest.getReader();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getRealPath(String arg0) {
		return this.wrappedRequest.getRealPath(arg0);
	}

	@Override
	public String getRemoteAddr() {
		return this.wrappedRequest.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return this.wrappedRequest.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return this.wrappedRequest.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return this.wrappedRequest.getRequestDispatcher(arg0);
	}

	@Override
	public String getScheme() {
		return this.wrappedRequest.getScheme();
	}

	@Override
	public String getServerName() {
		return this.wrappedRequest.getServerName();
	}

	@Override
	public int getServerPort() {
		return this.wrappedRequest.getServerPort();
	}

	@Override
	public boolean isSecure() {
		return this.wrappedRequest.isSecure();
	}

	@Override
	public void removeAttribute(String arg0) {
		this.wrappedRequest.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.wrappedRequest.setAttribute(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		this.wrappedRequest.setCharacterEncoding(arg0);
	}

	@Override
	public String getAuthType() {
		return this.wrappedRequest.getAuthType();
	}

	@Override
	public String getContextPath() {
		return this.wrappedRequest.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return this.wrappedRequest.getCookies();
	}

	@Override
	public long getDateHeader(String arg0) {
		return this.wrappedRequest.getDateHeader(arg0);
	}

	@Override
	public String getHeader(String arg0) {
		return this.wrappedRequest.getHeader(arg0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getHeaderNames() {
		return this.wrappedRequest.getHeaderNames();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getHeaders(String arg0) {
		return this.wrappedRequest.getHeaders(arg0);
	}

	@Override
	public int getIntHeader(String arg0) {
		return this.wrappedRequest.getIntHeader(arg0);
	}

	@Override
	public String getMethod() {
		return this.wrappedRequest.getMethod();
	}

	@Override
	public String getPathInfo() {
		return this.wrappedRequest.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return this.wrappedRequest.getPathTranslated();
	}

	@Override
	public String getRemoteUser() {
		return this.wrappedRequest.getRemoteUser();
	}

	@Override
	public String getRequestedSessionId() {
		return this.wrappedRequest.getRequestedSessionId();
	}

	@Override
	public String getServletPath() {
		return this.wrappedRequest.getServletPath();
	}

	@Override
	public HttpSession getSession() {
		return this.wrappedRequest.getSession();
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return this.wrappedRequest.getSession(arg0);
	}

	@Override
	public Principal getUserPrincipal() {
		return this.wrappedRequest.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.wrappedRequest.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.wrappedRequest.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return this.wrappedRequest.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.wrappedRequest.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return this.wrappedRequest.isUserInRole(arg0);
	}

	@Override
	public AsyncContext getAsyncContext() {
		//LOG.warn("Async contexts are not wrapped; modified/sanitized parameters will not be present!");
		return this.wrappedRequest.getAsyncContext();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return this.wrappedRequest.getDispatcherType();
	}

	@Override
	public ServletContext getServletContext() {
		return this.wrappedRequest.getServletContext();
	}

	@Override
	public boolean isAsyncStarted() {
		return this.wrappedRequest.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		//XXX:  an AsyncContext includes a direct reference back to the original request, meaning it will not reflect any modifications made in the wrapped; so we should not allow the use of aync contexts through a wrapped request
		//return this.wrappedRequest.isAsyncSupported();
		return false;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		LOG.warn("Async contexts are not wrapped (1); modified/sanitized parameters will not be present!");
		throw new IllegalStateException("Async operations is not supported using wrapped requests!");
		//return this.wrappedRequest.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		LOG.warn("Async contexts are not wrapped (2); modified/sanitized parameters will not be present!");
		throw new IllegalStateException("Async operations is not supported using wrapped requests!");
		//return this.wrappedRequest.startAsync(arg0, arg1);
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
		return this.wrappedRequest.authenticate(arg0);
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		LOG.warn("Multipart parameters are not wrapped (1); please use commons-fileupload instead!");
		throw new ServletException("Multipart parameters cannot be directly accessed through a wrapped request; consider using commons-fileupload to parse the request instead!");
		//return this.wrappedRequest.getPart(arg0);
	}

	@Override
	public Collection<javax.servlet.http.Part> getParts() throws IOException, ServletException {
		LOG.warn("Multipart parameters are not wrapped (2); please use commons-fileupload instead!");
		throw new ServletException("Multipart parameters cannot be directly accessed through a wrapped request; consider using commons-fileupload to parse the request instead!");
		//return this.wrappedRequest.getParts();
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		this.wrappedRequest.login(arg0, arg1);
	}

	@Override
	public void logout() throws ServletException {
		this.wrappedRequest.logout();
	}

}
