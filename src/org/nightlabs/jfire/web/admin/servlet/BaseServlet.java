package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.web.admin.NotAuthenticatedException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class BaseServlet extends HttpServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String KEY_PREFIX = "internal_";
	private static final String KEY_PAGETITLE = KEY_PREFIX+"pagetitle";
	private static final String KEY_FORWARDS = KEY_PREFIX+"forwards";
	private static final String KEY_ERRORS = KEY_PREFIX+"errors";
	private static final String KEY_INTERNALREDIRECT = KEY_PREFIX+"internalredirect";

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
		errors.add(e);
	}
	
	@SuppressWarnings("unchecked")
	protected void addContent(HttpServletRequest request, String... forwards)
	{
		List<String> forwardsList = (List<String>) request.getAttribute(KEY_FORWARDS);
		if(forwardsList == null) {
			forwardsList = new LinkedList<String>();
			request.setAttribute(KEY_FORWARDS, forwardsList);
		}
		Collections.addAll(forwardsList, forwards);
	}
	
	protected void setContent(HttpServletRequest request, String... forwards)
	{
		clearContent(request);
		addContent(request, forwards);
	}
	
	private void clearContent(HttpServletRequest request)
	{
		request.setAttribute(KEY_FORWARDS, null);
	}

	@SuppressWarnings("unchecked")
	protected List<String> getContent(HttpServletRequest request)
	{
		return (List<String>) request.getAttribute(KEY_FORWARDS);		
	}
	
	private void doForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		req.getRequestDispatcher("/jsp/pageHeader.jsp").include(req, resp);
		List<String> content = getContent(req);
		if(content != null && !content.isEmpty()) {
			for (String c : content)
				req.getRequestDispatcher(c).include(req, resp);
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
