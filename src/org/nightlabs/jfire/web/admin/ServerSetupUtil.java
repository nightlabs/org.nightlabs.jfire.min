package org.nightlabs.jfire.web.admin;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.server.ServerManagerRemote;

public class ServerSetupUtil
{
	protected static final Logger log = Logger.getLogger(ServerSetupUtil.class);

	public static SessionLogin getBogoLogin()
	{
		try {
			log.debug("Trying bogo login...");
			SessionLogin login = new SessionLogin(
					"__foobar_organisation_for_initial_login__",
					"__foobar_user_for_initial_login__",
					"__foobar_password_for_initial_login__");
			log.debug("Bogo login succeeded...");
			return login;
		} catch(Exception e) {
//			log.info("Bogo login failed.", e);
			throw new IllegalStateException("Bogo login failed", e);
		}
	}

	public static ServerManagerRemote getBogoServerManager()
	{
		// we might already be logged in, so we first try to get the ServerManager normally
		try {
			ServerManagerRemote serverManager = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, SecurityReflector.getInitialContextProperties());
			serverManager.ping("test_authentication");
			return serverManager;
		} catch (Exception x) {
			// silently ignore and try it the bogo way below
		}

		try {
			SessionLogin login = getBogoLogin();
			ServerManagerRemote serverManager = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, login.getInitialContextProperties());
			serverManager.ping("test_authentication");
			return serverManager;
		} catch(Exception e) {
//			log.info("Getting bogo server manager failed.", e);
			throw new IllegalStateException("Getting bogo server manager failed", e);
		}
	}

	public static OrganisationManagerRemote getBogoOrganisationManager()
	{
		try {
			SessionLogin login = getBogoLogin();
			OrganisationManagerRemote manager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, login.getInitialContextProperties());
			manager.ping("test_authentication");
			return manager;
		} catch(Exception e) {
//			log.error("Getting bogo organisation manager failed.", e);
			throw new IllegalStateException("Getting bogo organisation manager failed", e);
		}
	}

	public static boolean isNewServerNeedingSetup()
	{
		try {
			return getBogoServerManager().isNewServerNeedingSetup();
		} catch(Throwable e) {
			// ignore exception and return false
			log.info("Error in isNewServerNeedingSetup()", e);
			return false;
		}
	}
}
