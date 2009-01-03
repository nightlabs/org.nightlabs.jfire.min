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

package org.nightlabs.jfire.base;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Hashtable;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;

/**
 * This class should be used as ancestor of your session beans. It provides some convenience methods making
 * it easier to implement your bean.
 *
 * @author nick at nightlabs dot de
 * @author marco at nightlabs dot de
 */
public class BaseSessionBeanImpl
{
	protected SessionContext sessionContext;

	public void setSessionContext(SessionContext sessionContext)
		throws EJBException, RemoteException
	{
		this.sessionContext = sessionContext;
	}
	public void unsetSessionContext()
	{
		this.sessionContext = null;
	}

	/**
	 * This method returns the JFirePrincipal representing the current user.
	 *
	 * @return the principal representing the current user.
	 */
	public JFirePrincipal getPrincipal()
	{
		Principal pr = sessionContext.getCallerPrincipal();
		if (pr instanceof JFirePrincipal)
			return (JFirePrincipal)pr;
		else if (pr instanceof JFirePrincipalContainer)
			return (JFirePrincipal) ((JFirePrincipalContainer)pr).getJFirePrincipal();
		else
			throw new IllegalStateException("sessionContext.getCallerPrincipal() neither returned a JFirePrincipal, nor a JFirePrincipalContainer: class=" + (pr == null ? null : pr.getClass()) + " instance=" + pr);
	}

	/**
	 * This method is a shortcut to <code>getPrincipal().getLookup()</code>. It might
	 * not work with stateless session beans!
	 *
	 * @return The Lookup instance assigned to the current user.
	 *
	 * @see getPrincipal()
	 */
	protected Lookup getLookup()
	{
		return getPrincipal().getLookup();
	}

//	protected PersistenceManagerFactory getPersistenceManagerFactory()
//	{
//		return getPrincipal().getLookup().getPersistenceManagerFactory();
//	}

//	protected void cache_addDirtyObjectIDs(PersistenceManager persistenceManager, Collection objectIDs)
//	throws ModuleException
//	{
//		getPrincipal().getLookup().getCacheManager().addDirtyObjectIDs(persistenceManager, objectIDs);
//	}
//
//	protected void cache_addDirtyObjectID(PersistenceManager persistenceManager, Object objectID)
//	throws ModuleException
//	{
//		ArrayList l = new ArrayList(1);
//		l.add(objectID);
//		getPrincipal().getLookup().getCacheManager().addDirtyObjectIDs(persistenceManager, l);
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

//	protected InitialContext getInitialContext()
//		throws ModuleException
//	{
//		return getPrincipal().getLookup().getInitialContext();
//	}

	/**
	 * Use this method whenever you want to communicate with another
	 * organisation. This method configures an InitialContext to connect
	 * to the right server and to authenticate correctly according
	 * to the JFire way of organisation@organisation-authentication.
	 * <p>
	 * This method is a shortcut to
	 * <code>getPrincipal().getLookup().getInitialContext(organisationID)</code>.
	 * <p>
	 * <b>You must not forget to close the initial context afterwards!</b>
	 *
	 * @param organisationID The organisationID with wich to communicate.
	 * @return Returns an InitialContext that is configured properly to authenticate at and communicate with another organisation (wherever it may be - e.g. on another server).
	 * @throws NamingException
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
	 * @throws NamingException
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

	/**
	 * Get the workstation-identifier of the currently logged-in user or <code>null</code> if he didn't specify one during login.
	 *
	 * @return the workstationID or <code>null</code>.
	 */
	protected String getWorkstationID()
	{
		return getPrincipal().getWorkstationID();
	}

	protected String getSessionID()
	{
		return getPrincipal().getSessionID();
	}

	protected boolean userIsOrganisation()
	{
		return getPrincipal().userIsOrganisation();
	}

//	protected String getPrincipalString()
//	{
//		return getPrincipal().toString();
//	}

	private static Boolean hasRootOrganisation = null;

	protected boolean hasRootOrganisation()
	{
		if (hasRootOrganisation == null) {
			try {
				InitialContext ctx = new InitialContext();
				try {
					hasRootOrganisation = new Boolean(Organisation.hasRootOrganisation(ctx));
				} finally {
					ctx.close();
				}
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
		return hasRootOrganisation.booleanValue();
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

	/**
	 * Return the parameter as result and thus serve as a ping (test whether a bean proxy is still alive)
	 * for remote clients. In order to make this method available to remote-clients, you must put the following code
	 * into your EJB-subclass:
	 * <code>
	 *&#x9;&#x9;&#x2F;**<br/>
	 *&#x9;&#x9;&nbsp;&#x40;ejb.interface-method<br/>
	 *&#x9;&#x9;&nbsp;&#x40;ejb.transaction type="Supports"<br/>
	 *&#x9;&#x9;&nbsp;&#x40;ejb.permission role-name="_Guest_"<br/>
	 *&#x9;&#x9;*&#x2F;<br/>
	 *&#x9;&#x9;&#x40;Override<br/>
	 *&#x9;&#x9;public String ping(String message) {<br/>
	 *&#x9;&#x9;&#x9;return super.ping(message);<br/>
	 *&#x9;&#x9;}<br/>
	 * </code>
	 * <p>
	 * This ping method is used by {@link JFireEjbFactory#getBean(Class, Hashtable)} to test a
	 * cached EJB proxy. If the method cannot be successfully executed on the EJB proxy, it is discarded
	 * and a new one created.
	 * </p>
	 *
	 * @param message the message (can be <code>null</code>).
	 * @return the result - same as message.
	 */
	public String ping(String message) {
		return message;
	}
}
