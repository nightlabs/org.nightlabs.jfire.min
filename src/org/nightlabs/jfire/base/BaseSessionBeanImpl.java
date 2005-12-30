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
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;

import org.nightlabs.ModuleException;

/**
 * This class should be used as anchestor of your session beans. Note, that
 * some methods will probably not work with stateless session beans. This is,
 * because it seems a stateless session bean has no principal stored in its
 * session context.
 *
 * @author nick@nightlabs.de
 * @author marco@nightlabs.de
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
	 * This method returns the JFirePrincipal representing the current user. Note, that
	 * this method probably only works with stateful session beans!
	 * 
	 * @return Returns the principal representing the current user.
	 */
	public JFirePrincipal getPrincipal()
	{
		Principal pr = sessionContext.getCallerPrincipal();
//		System.out.println("*********************************************************");
//		System.out.println("Principal Class: "+pr.getClass().getName());
//		System.out.println("Principal: "+pr);
//		System.out.println("*********************************************************");
		return (JFirePrincipal)pr;
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
	
	protected PersistenceManagerFactory getPersistenceManagerFactory()
	throws ModuleException
	{
		return getPrincipal().getLookup().getPersistenceManagerFactory();
	}

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
	 * It might fail with stateless session beans!
	 * 
	 * @return Returns the PersistenceManager assigned to the current user.
	 * @throws ModuleException
	 *
	 * @see getPrincipal()
	 */
	protected PersistenceManager getPersistenceManager()
		throws ModuleException
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
	 * @throws ModuleException
	 */
	protected InitialContext getInitialContext(String organisationID)
		throws ModuleException 
	{
		try {
			return new InitialContext(getInitialContextProps(organisationID));
		} catch (NamingException e) {
			throw new ModuleException(e);
		} catch (ModuleException e) {
			throw e;
		}
		// return getPrincipal().getLookup().getInitialContext(organisationID);
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
	 * @throws ModuleException
	 * 
	 * @see getInitialContext(String organisationID)
	 */
	protected Hashtable getInitialContextProps(String organisationID)
		throws ModuleException
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
			return Lookup.getInitialContextProps(pm, organisationID);
		} finally {
			if (managePM)
				pm.close();
		}

//		return getPrincipal().getLookup().getInitialContextProps(organisationID);
	}

	protected JFireServerManagerFactory getJFireServerManagerFactory()
		throws ModuleException
	{
		return getPrincipal().getLookup().getJFireServerManagerFactory();
	}
	
	protected JFireServerManager getJFireServerManager()
	throws ModuleException
	{
		return getPrincipal().getLookup().getJFireServerManager();
	}

	protected String getOrganisationID()
	throws ModuleException
	{
		return getPrincipal().getOrganisationID();
	}

	protected String getUserID()
	throws ModuleException
	{
		return new String(getPrincipal().getUserID());
	}

	protected String getSessionID()
	throws ModuleException
	{
		return new String(getPrincipal().getSessionID());
	}

	protected boolean userIsOrganisation()
	throws ModuleException
	{
		return getPrincipal().userIsOrganisation();
	}
	
	protected String getPrincipalString()
		throws ModuleException
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
