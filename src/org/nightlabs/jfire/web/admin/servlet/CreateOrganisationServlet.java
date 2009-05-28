package org.nightlabs.jfire.web.admin.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.web.admin.NotAuthenticatedException;
import org.nightlabs.jfire.web.admin.ServerSetupUtil;
import org.nightlabs.jfire.web.admin.SessionLogin;
import org.nightlabs.jfire.web.admin.Util;
import org.nightlabs.jfire.web.admin.ServerSetupUtil.ServerState;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateOrganisationServlet extends BaseServlet
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
		setContent(req, "/jsp/createorganisation.jsp");

		SessionLogin login;
		boolean loginAsNewlyCreatedUser = false;
		try {
			login = SessionLogin.getLogin(req.getSession());
		} catch(NotAuthenticatedException e) {
			// one amy create an organisation when there is no organisation yet.
			// Try bogo login.
			if(ServerSetupUtil.getServerState() == ServerState.NEED_ORGANISATION) {
				login = ServerSetupUtil.getBogoLogin();
				loginAsNewlyCreatedUser = true;
			} else
				throw e;
		}

		if(Util.haveParameterValue(req, "action", "createorganisation")) {
			String organisationId = Util.getParameter(req, "organisationId");
			String organisationName = Util.getParameter(req, "organisationName");
			String userName = Util.getParameter(req, "username");
			String password = Util.getParameter(req, "password");
			String password2 = Util.getParameter(req, "password2");
			boolean serverAdmin = Util.getParameterAsBoolean(req, "serveradmin");

			if(!password.equals(password2))
				throw new IllegalArgumentException("The passwords don't match");

			OrganisationManagerRemote manager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, login.getInitialContextProperties());
			manager.createOrganisation(organisationId, organisationName, userName, password, serverAdmin);

			if(loginAsNewlyCreatedUser)
				SessionLogin.login(req.getSession(), organisationId, userName, password);
			
			redirect(req, resp, "/organisationlist");
			return;
		}
	}
}
