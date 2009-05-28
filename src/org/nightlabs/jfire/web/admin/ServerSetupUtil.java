package org.nightlabs.jfire.web.admin;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.server.ServerManagerRemote;

import com.sun.xml.stream.buffer.sax.Properties;

public class ServerSetupUtil
{
	protected static final Logger log = Logger.getLogger(ServerSetupUtil.class);

	public static SessionLogin getBogoLogin()
	{
		try {
//			log.debug("Trying bogo login...");
			SessionLogin login = new SessionLogin(
					"__foobar.organisation.for.initial.login__",
					"__foobar_user_for_initial_login__",
					"__foobar_password_for_initial_login__");
//			log.debug("Bogo login succeeded...");
			return login;
		} catch(Exception e) {
//			log.info("Bogo login failed.", e);
			throw new IllegalStateException("Bogo login failed", e);
		}
	}

	public static ServerManagerRemote getServerManager(Hashtable<?, ?> initialContextProperties)
	{
		ServerManagerRemote serverManager = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, initialContextProperties);
		serverManager.ping("test_authentication");
		return serverManager;
	}
	
	public static ServerManagerRemote getBogoServerManager()
	{
		try {
			SessionLogin login = getBogoLogin();
			return getServerManager(login.getInitialContextProperties());
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

//	public static boolean isNewServerNeedingSetup()
//	{
//		try {
//			return getBogoServerManager().isNewServerNeedingSetup();
//		} catch(Throwable e) {
//			// ignore exception and return false
//			log.info("Error in isNewServerNeedingSetup()", e);
//			return false;
//		}
//	}
	
	public enum ServerState
	{
		NEED_ORGANISATION,
		UNKNOWN_ERROR,
		SHUTTING_DOWN,
		NOT_YET_UP_AND_RUNNING,
		NEED_SETUP, 
		NEED_LOGIN
	}
	
	public static ServerState getServerState()
	{
		try {
			ServerManagerRemote bogoServerManager = getBogoServerManager();
			if(bogoServerManager.isNewServerNeedingSetup())
				// we can login using bogo data and the server says it needs to be set-up
				return ServerState.NEED_SETUP;
			else
				// there server does not need setup, but we can login using
				// bogo data. Thus, an oraginsation has not yet been created.
				return ServerState.NEED_ORGANISATION;
		} catch(Throwable e) {
			if (findCause(e, LoginException.class, "org.jfire.serverShuttingDown") != null) //$NON-NLS-1$
				return ServerState.SHUTTING_DOWN;
			else if (findCause(e, LoginException.class, "org.jfire.serverNotYetUpAndRunning") != null) //$NON-NLS-1$
				return ServerState.NOT_YET_UP_AND_RUNNING;
			else if (findCause(e, LoginException.class, null) != null) //$NON-NLS-1$
				return ServerState.NEED_LOGIN;
			else {
				log.info("Error in getServerState()", e);
				return ServerState.UNKNOWN_ERROR;
			}
		}
	}
	
	private static Throwable findCause(Throwable e, Class<? extends Throwable> searchedClass, String searchedMessageRegex)
	{
		if (e == null)
			throw new IllegalArgumentException("e must not be null!"); //$NON-NLS-1$

		if (searchedClass == null && searchedMessageRegex == null)
			throw new IllegalArgumentException("searchedClass and searchedMessageRegex are both null! One must be defined!"); //$NON-NLS-1$

		Pattern searchedMessageRegexPattern = searchedMessageRegex == null ? null : Pattern.compile(searchedMessageRegex);

		Throwable cause = e;
		while (cause != null) {
			boolean found = true;

			if (searchedClass != null) {
				if (!searchedClass.isInstance(cause)) {
					found = false;
				}
			}

			if (found && searchedMessageRegexPattern != null) { // if the match already failed, there's no need to search further
				String message = cause.getMessage();
				if (message == null)
					found = false;
				else if (!searchedMessageRegexPattern.matcher(message).matches())
					found = false;
			}

			if (found)
				return cause;

			Throwable newCause = ExceptionUtils.getCause(cause);
			// not strange at all if you get the cause auf e all the time. Marc
//			Throwable newCause = ExceptionUtils.getCause(e);
//			if (cause == newCause) // really strange, but I just had an eternal loop because the cause of an exception was itself.
//				return null;

			cause = newCause;
		}
		return null;
	}
}
