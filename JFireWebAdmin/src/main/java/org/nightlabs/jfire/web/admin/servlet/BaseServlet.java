package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.web.admin.NotAuthenticatedException;
import org.nightlabs.jfire.web.admin.SessionLogin;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class BaseServlet extends HttpServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(BaseServlet.class);
	
	private static final String KEY_PREFIX = "internal_";
	private static final String KEY_PAGETITLE = KEY_PREFIX + "pagetitle";
	private static final String KEY_FORWARDS = KEY_PREFIX + "forwards";
	private static final String KEY_FORWARD_REQUESTS = KEY_PREFIX + "forwardrequests";
	private static final String KEY_ERRORS = KEY_PREFIX + "errors";
	private static final String KEY_INTERNALREDIRECT = KEY_PREFIX + "internalredirect";
	private static final String KEY_LOGIN = KEY_PREFIX + "login";

	protected abstract void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequestSafe(req, resp);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequestSafe(req, resp);
	}
	
	private void handleRequestSafe(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		// provide the login as information for the jsps
		if(SessionLogin.haveLogin(req.getSession())) {
			try {
				req.setAttribute(KEY_LOGIN, SessionLogin.getLogin(req.getSession()));
			} catch (NotAuthenticatedException e) {
				// ignore
			}
		}
		try {
			handleRequest(req, resp);
		} catch(NotAuthenticatedException e) {
			redirect(req, resp, "/login");
			return;
		} catch(Throwable e) {
			addError(req, e);
		}
		if(req.getAttribute(KEY_INTERNALREDIRECT) == null)
			doForward(req, resp);
	}

	@SuppressWarnings("unchecked")
	protected void addError(HttpServletRequest request, Throwable e)
	{
		List<Throwable> errors = (List<Throwable>) request.getAttribute(KEY_ERRORS);
		if(errors == null) {
			errors = new LinkedList<Throwable>();
			request.setAttribute(KEY_ERRORS, errors);
		}
		log("Error in servlet execution", e);
		errors.add(e);
	}
	
	protected void addContent(HttpServletRequest request, String... forwards)
	{
		List<String> forwardsList = getContent(request);
		if(forwardsList == null) {
			forwardsList = new LinkedList<String>();
			request.setAttribute(KEY_FORWARDS, forwardsList);
		}
		Collections.addAll(forwardsList, forwards);
	}

	protected void addContent(HttpServletRequest request, String forward, HttpServletRequest forwardRequest)
	{
		List<String> forwardsList = getContent(request);
		if(forwardsList == null) {
			forwardsList = new LinkedList<String>();
			request.setAttribute(KEY_FORWARDS, forwardsList);
		}
		int newIdx = forwardsList.size();
		forwardsList.add(forward);
		
		if(forwardRequest != null) {
			List<HttpServletRequest> forwardRequestsList = getForwardRequests(request);
			if(forwardRequestsList == null) {
				forwardRequestsList = new LinkedList<HttpServletRequest>();
				request.setAttribute(KEY_FORWARD_REQUESTS, forwardRequestsList);
			}
			int newSize = newIdx;
			while(forwardRequestsList.size() < newSize)
				forwardRequestsList.add(null);
			forwardRequestsList.add(forwardRequest);
		}
	}
	
	protected void setContent(HttpServletRequest request, String... forwards)
	{
		clearContent(request);
		addContent(request, forwards);
	}
	
	protected void clearContent(HttpServletRequest request)
	{
		request.setAttribute(KEY_FORWARDS, null);
	}

	@SuppressWarnings("unchecked")
	protected List<String> getContent(HttpServletRequest request)
	{
		return (List<String>) request.getAttribute(KEY_FORWARDS);		
	}

	@SuppressWarnings("unchecked")
	protected List<HttpServletRequest> getForwardRequests(HttpServletRequest req)
	{
		return (List<HttpServletRequest>) req.getAttribute(KEY_FORWARD_REQUESTS);
	}
	
	protected HttpServletRequest getForwardRequest(HttpServletRequest request, int idx)
	{
		List<HttpServletRequest> forwardRequests = getForwardRequests(request);
		if(forwardRequests == null || forwardRequests.size() <= idx)
			return request;
		HttpServletRequest forwardRequest = forwardRequests.get(idx);
		if(forwardRequest == null)
			return request;
		return forwardRequest;
	}
	
	private void doForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		req.getRequestDispatcher("/jsp/pageHeader.jsp").include(req, resp);
		List<String> content = getContent(req);
		if(content != null && !content.isEmpty()) {
			log.info("Forwarding to: "+content);
			int idx = 0;
			for (String c : content) {
				req.getRequestDispatcher(c).include(getForwardRequest(req, idx), resp);
				idx++;
			}
		} else {
			req.getRequestDispatcher("/jsp/error.jsp");
		}
		req.getRequestDispatcher("/jsp/pageFooter.jsp").include(req, resp);
	}
	
	protected void redirect(HttpServletRequest req, HttpServletResponse resp, String webAppRelativePath) throws IOException
	{
		if(!webAppRelativePath.startsWith("/"))
			throw new IllegalArgumentException("Invalid redirect");
		req.setAttribute(KEY_INTERNALREDIRECT, true);
		resp.sendRedirect(getServletContext().getContextPath()+webAppRelativePath);
	}
	
	protected void setPageTitle(HttpServletRequest req, String title)
	{
		req.setAttribute(KEY_PAGETITLE, title);
	}
}
