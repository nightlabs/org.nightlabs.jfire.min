package org.nightlabs.jfire.web.admin.servlet;

import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.web.admin.ServerSetupUtil;
import org.nightlabs.jfire.web.admin.SessionLogin;
import org.nightlabs.jfire.web.admin.ServerSetupUtil.ServerState;

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
			String redirect = null;
			String error = null;
			if(SessionLogin.haveLogin(request.getSession()))
				redirect = "/overview";
			else {
				ServerState serverState = ServerSetupUtil.getServerState();
				if(serverState == ServerState.NEED_SETUP)
					redirect = "/serverinitialize";
				else if(serverState == ServerState.NOT_YET_UP_AND_RUNNING)
					error = "The server is not yet up and running.";
				else if(serverState == ServerState.SHUTTING_DOWN)
					error = "The server is shutting down.";
				else
					redirect = "/login";
			}
			if(error != null) {
				request.setAttribute("internal_errors", Collections.singleton(new IllegalStateException(error)));
				request.getRequestDispatcher("jsp/pageHeader.jsp").include(request, response);
				request.getRequestDispatcher("jsp/pageFooter.jsp").include(request, response);
			} else {
				response.sendRedirect(getServletContext().getContextPath()+redirect);
			}
		} catch(Exception e) {
			throw new ServletException("Error in IndexServlet", e);
		}
	}
}
