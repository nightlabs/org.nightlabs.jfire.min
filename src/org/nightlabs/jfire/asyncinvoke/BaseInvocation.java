/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.asyncinvoke;

import java.io.Serializable;
import java.util.Hashtable;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class BaseInvocation
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private transient JFirePrincipal callerPrincipal;

	public BaseInvocation()
	{
	}

	/**
	 * @return Returns the lookup.
	 */
	public Lookup getLookup()
	{
		return getPrincipal().getLookup();
	}

	/**
	 * @return Returns the callerPrincipal.
	 */
	public JFirePrincipal getPrincipal()
	{
		return callerPrincipal;
	}
	/**
	 * @param callerPrincipal The callerPrincipal to set.
	 */
	public void setPrincipal(JFirePrincipal callerPrincipal)
	{
		this.callerPrincipal = callerPrincipal;
	}
	
//	protected PersistenceManagerFactory getPersistenceManagerFactory()
//	{
//		return getPrincipal().getLookup().getPersistenceManagerFactory();
//	}

	private transient PersistenceManager persistenceManager = null;

	/**
	 * This method is a shortcut to <code>getPrincipal().getLookup().getPersistenceManager()</code>.
	 * <p>
	 * <b>Important:</b> You must call {@link PersistenceManager#close()} at the end of your EJB method!
	 * </p>
	 * 
	 * @return Returns the PersistenceManager assigned to the current user.
	 *
	 * @see getPrincipal()
	 */
	protected PersistenceManager getPersistenceManager()
	{
		persistenceManager = getPrincipal().getLookup().getPersistenceManager();
		return persistenceManager;
	}

	/**
	 * Use this method whenever you want to communicate with another
	 * organisation. This method configures an InitialContext to connect
	 * to the right server and to authenticate correctly according
	 * to the JFire way of organisation@organisation-authentication.
	 * <br/><br/>
	 * This method is a shortcut to
	 * <code>getPrincipal().getLookup().getInitialContext(organisationID)</code>.
	 * 
	 * @param organisationID The organisationID with wich to communicate.
	 * @return Returns an InitialContext that is configured properly to authenticate at and communicate with another organisation (wherever it may be - e.g. on another server).
	 */
	protected InitialContext getInitialContext(String organisationID)
	throws NamingException
	{
		return new InitialContext(getInitialContextProperties(organisationID));
	}

	/**
	 * This method returns the properties that are used by
	 * <code>getInitialContext(String organisationID)</code>. It is meant
	 * to be used with *Util classes that expect a Hashtable/Properties instance
	 * to create a home interface.
	 * <br/><br/>
	 * This method is a shortcut to
	 * <code>getPrincipal().getLookup().getInitialContextProps(organisationID)</code>
	 *
	 * @param organisationID
	 * @return Returns an instance of Properties to be used in <code>new InitialContext(Properties)</code>.
	 * 
	 * @see getInitialContext(String organisationID)
	 */
	protected Hashtable<?, ?> getInitialContextProperties(String organisationID)
	throws NamingException
	{
		boolean managePM = false;
		PersistenceManager pm;
		if (persistenceManager == null || persistenceManager.isClosed()) {
			managePM = true;
			pm = getPersistenceManager();
		}
		else
			pm = persistenceManager;

		try {
			return Lookup.getInitialContextProperties(pm, organisationID);
		} finally {
			if (managePM)
				pm.close();
		}

//		return getPrincipal().getLookup().getInitialContextProps(organisationID);
	}

	protected JFireServerManagerFactory getJFireServerManagerFactory()
	{
		return getPrincipal().getLookup().getJFireServerManagerFactory();
	}
	
	protected JFireServerManager getJFireServerManager()
	{
		return getPrincipal().getLookup().getJFireServerManager();
	}

	protected String getOrganisationID()
	{
		return getPrincipal().getOrganisationID();
	}

	protected String getUserID()
	{
		return getPrincipal().getUserID();
	}

	protected String getSessionID()
	{
		return getPrincipal().getSessionID();
	}
	
	protected boolean userIsOrganisation()
	{
		return getPrincipal().userIsOrganisation();
	}
	
	protected String getPrincipalString()
	{
		return getPrincipal().toString();
	}

	private static String rootOrganisationID = null;

	/**
	 * @see Organisation#getRootOrganisationID(InitialContext)
	 */
	protected String getRootOrganisationID()
	{
		try {
			if (rootOrganisationID == null) {
				InitialContext ctx = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(ctx);
				} finally {
					ctx.close();
				}
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		return rootOrganisationID;
	}
}
