package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.web.admin.AuthenticationFailedException;
import org.nightlabs.jfire.web.admin.SessionLogin;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginServlet extends BaseServlet
{
	/**
	 * The serial version of this class. 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(LoginServlet.class);

	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, AuthenticationFailedException
	{
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		String organisationId = req.getParameter("organisationId");
		
		req.setAttribute("redirect", req.getParameter("redirect"));

		setContent(req, "/jsp/login.jsp");
		
		if(username != null && password != null && organisationId != null) {
			// try to authenticate
			log.info("Trying login");
			SessionLogin.login(req.getSession(), organisationId, username, password);
			log.info("Login successful");
			String redirect = req.getParameter("redirect");
			log.info("redirect: "+redirect);
			if(redirect == null || redirect.isEmpty())
				redirect = "/overview";
			redirect(req, resp, redirect);
		}
	}
}
