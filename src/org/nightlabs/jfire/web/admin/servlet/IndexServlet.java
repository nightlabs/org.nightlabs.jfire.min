package org.nightlabs.jfire.web.admin.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.web.admin.ServerSetupUtil;
import org.nightlabs.jfire.web.admin.SessionLogin;

/**
 * The index servlet. This will redirect to another page depending on the
 * current state.
 * If a user is logged in, the overview page will be shown.
 * If this is a uninitialized server the server initialization will be started.
 * Otherwise, the login page will be shown.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class IndexServlet extends HttpServlet 
{
	//private static final Logger log = Logger.getLogger(IndexServlet.class);
	
	/**
	 * The serial version of this class. 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		try {
			String redirect;
			if(SessionLogin.haveLogin(request.getSession()))
				redirect = "/overview";
			else if(ServerSetupUtil.isNewServerNeedingSetup())
				redirect = "/serverinitialize";
			else
				redirect = "/login";
			response.sendRedirect(getServletContext().getContextPath()+redirect);
		} catch(Exception e) {
			throw new ServletException("Error in IndexServlet", e);
		}
	}
}
