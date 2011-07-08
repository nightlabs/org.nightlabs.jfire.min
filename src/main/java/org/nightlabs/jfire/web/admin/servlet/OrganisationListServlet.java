package org.nightlabs.jfire.web.admin.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.web.admin.SessionLogin;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class OrganisationListServlet extends BaseServlet
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
		setContent(req, "/jsp/organisationlist.jsp");
		SessionLogin login = SessionLogin.getLogin(req.getSession());
		OrganisationManagerRemote manager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, login.getInitialContextProperties());
		List<OrganisationCf> organisationCfs = manager.getOrganisationCfs(true);
		req.setAttribute("organisationCfs", organisationCfs);
	}
}
