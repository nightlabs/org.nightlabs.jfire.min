package org.nightlabs.jfire.base.security;

import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * 
 * @author Marco Schulze
 */
public class SecurityReflectorClient
extends SecurityReflector
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SecurityReflectorClient.class);

	public UserDescriptor _getUserDescriptor() {
		if (logger.isDebugEnabled())
			logger.debug("_getUserDescriptor: enter"); //$NON-NLS-1$

		Login l = Login.sharedInstance();
		return new UserDescriptor(l.getOrganisationID(), l.getUserID(), l.getSessionID());
	}

	public InitialContext _createInitialContext() {
		try {
			return Login.getLogin().createInitialContext();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Properties _getInitialContextProperties() {
		try {
			return Login.getLogin().getInitialContextProperties();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
