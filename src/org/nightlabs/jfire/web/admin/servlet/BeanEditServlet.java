package org.nightlabs.jfire.web.admin.servlet;

import org.nightlabs.jfire.servermanager.config.DatabaseCf;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class BeanEditServlet extends ConfigModuleEditServlet
{
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.admin.servlet.ConfigModuleEditServlet#getConfigModule()
	 */
	@Override
	protected Object getConfigModule()
	{
		return new DatabaseCf();
	}
}
