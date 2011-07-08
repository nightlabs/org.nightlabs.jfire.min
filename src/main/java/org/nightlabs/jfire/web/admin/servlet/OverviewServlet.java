package org.nightlabs.jfire.web.admin.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class OverviewServlet extends BaseServlet
{
	/**
	 * The serial version of this class. 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.admin.servlet.BaseServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		setContent(req, "/jsp/overview.jsp");
	}
}
