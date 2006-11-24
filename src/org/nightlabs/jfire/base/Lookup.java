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

import java.util.Hashtable;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.jdo.cache.CacheManager;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;

/**
 * This class is a util to make it easier to look up important JFire objects from JNDI.
 * 
 * @author marco
 */
public class Lookup 
{
	private String organisationID;
	/**
	 * @return The organisationID with which the current session is working locally.
	 *   This organisationID is the target of all actions within this session.
	 */
	public String getOrganisationID() { return organisationID; }

	private JFirePrincipal jfirePrincipal = null;
	protected void setJFirePrincipal(JFirePrincipal _jfirePrincipal) {
		if (this.jfirePrincipal != null)
			throw new IllegalStateException("Why the hell does this instance of Lookup has already an JFirePrincipal (\""+this.jfirePrincipal+"\") set?");
		this.jfirePrincipal = _jfirePrincipal;
	}

	private JFireServerManagerFactory jfireServerManagerFactory = null;

	/**
	 * @throws RuntimeException A properly configured server should not have problems with JNDI. Therefore,
	 *		we wrap the NamingException in a RuntimeException.
	 */
	public JFireServerManagerFactory getJFireServerManagerFactory()
	{
		if (jfireServerManagerFactory == null)
			try {
				InitialContext ctx = new InitialContext();
				try {
					jfireServerManagerFactory = (JFireServerManagerFactory)ctx.lookup(JFireServerManagerFactory.JNDI_NAME);
				} finally {
					ctx.close();
				}
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
		
		return jfireServerManagerFactory;
	}

	public JFireServerManager getJFireServerManager()
	{
		return getJFireServerManagerFactory().getJFireServerManager(jfirePrincipal);
	}

	/**
	 * Do NOT call this constructor! Your principal has an instance of Lookup already!
	 *
	 * @param _organisationId The target organisationID onto which all actions are executed
	 *   locally.   
	 * @throws NamingException if a naming exception occurs while creating an initial context.
	 */
	public Lookup(String _organisationId)
	{	
		this.organisationID = _organisationId;
	}
	
	/**
	 * @return the pmfactory for the organisationID of the working user.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory()
	{
		return getPersistenceManagerFactory(organisationID);
	}

	/**
	 * This method returns a PersistenceManager that is providing access to
	 * the database that is linked to the organisationID of the current user.
	 * Every sessionbean is
	 * 
	 * @return
	 */
	public PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
		return pm;
	}


//	/**
//	 * key: String organisationID<br/>
//	 * value: PersistenceManagerFactory persistenceManagerFactory 
//	 */
//	protected Map pmfMap = new HashMap(); 
	
	/**
	 * Returns the PersistenceManagerFactory for the given organisationID. If it cannot be
	 * found in JNDI, an exception is thrown. 
	 *  
	 * @param organisationID The unique identifier for the organisationID
	 * @return the PersistenceManagerFactory belonging to the given organisationID.
	 * @throws RuntimeException Because a properly configured server should not have any JNDI (or other) problems,
	 *		we wrap any exception in a RuntimeException.
	 */
	protected PersistenceManagerFactory getPersistenceManagerFactory(String organisationID)
	{
		PersistenceManagerFactory pmf;
		try {
			InitialContext ctx = new InitialContext();
			try {
				pmf = (PersistenceManagerFactory)ctx.lookup(
						OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
		return pmf;
	}
	
	protected PersistenceManager getPersistenceManager(String organisationId)
	{
		return getPersistenceManagerFactory(organisationId).getPersistenceManager();
	}

	/**
	 * This method reads the properties out of the datastore managed by pm, that are necessary
	 * to connect and login to the organisation defined by _organisationID. If _organisationID
	 * is the local organisation (managed by pm), this method returns <tt>null</tt>! 
	 * @throws NamingException 
	 */
	public static Hashtable getInitialContextProperties(PersistenceManager pm, String _organisationID)
	throws NamingException
	{
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
		if (_organisationID.equals(localOrganisation.getOrganisationID()))
			return null;

		InitialContext initCtx = new InitialContext();
		try {
			JFireServerManagerFactory jfireServerManagerFactory = (JFireServerManagerFactory)
			initCtx.lookup(JFireServerManagerFactory.JNDI_NAME);

			String password = localOrganisation.getPassword(_organisationID);

			Organisation organisation = (Organisation)pm.getObjectById(OrganisationID.create(_organisationID), true);
			Server server = organisation.getServer();
			String initialContextFactory = jfireServerManagerFactory.getInitialContextFactory(server.getJ2eeServerType(), true);
			return _getInitialContextProps(
					initialContextFactory, server.getInitialContextURL(),
					localOrganisation.getOrganisationID(),
					_organisationID, password);
		} finally {
			initCtx.close();
		}
	}

	protected Hashtable _getInitialContextProps(PersistenceManager pm, String _organisationID) throws ModuleException
	{
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
		if (_organisationID.equals(localOrganisation.getOrganisationID()))
			return null;

		String password = localOrganisation.getPassword(_organisationID);

		Organisation organisation = (Organisation)pm.getObjectById(OrganisationID.create(_organisationID), true);
		Server server = organisation.getServer();
		String initialContextFactory = getJFireServerManagerFactory().getInitialContextFactory(server.getJ2eeServerType(), true);
		return _getInitialContextProps(
				initialContextFactory, server.getInitialContextURL(),
				localOrganisation.getOrganisationID(),
				_organisationID, password);
	}

	protected static Hashtable _getInitialContextProps(
			String initialContextFactory, String initialContextURL,
			String localOrganisationID, String remoteOrganisationID, String password)
	{
		String username = User.USERID_PREFIX_TYPE_ORGANISATION
			+ localOrganisationID
			+ '@'
			+ remoteOrganisationID;

		Hashtable props = new Properties();
		props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		props.put(InitialContext.PROVIDER_URL, initialContextURL);
		props.put(InitialContext.SECURITY_PRINCIPAL, username);
		props.put(InitialContext.SECURITY_CREDENTIALS, password);
		props.put(InitialContext.SECURITY_PROTOCOL, "jfire");
		return props;
	}

	/**
	 * @throws RuntimeException There might be a {@link NamingException}, but in a properly
	 *		configured server, this should never happen. Therefore, we throw a {@link RuntimeException} in
	 *		this very rare situation.
	 */
	public CacheManagerFactory getCacheManagerFactory()
	{
		try {
			InitialContext ctx = new InitialContext();
			try {
				return CacheManagerFactory.getCacheManagerFactory(ctx, getOrganisationID());
			} finally {
				ctx.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * @see CacheManagerFactory#getCacheManager()
	 */
	public CacheManager getCacheManager()
	{
		return getCacheManagerFactory().getCacheManager();
	}

	/**
	 * @see CacheManagerFactory#getCacheManager(JFirePrincipal)
	 */
	public CacheManager getCacheManager(JFirePrincipal principal)
	{
		return getCacheManagerFactory().getCacheManager(principal);
	}

//	public TransactionManager getTransactionManager()
//		throws ModuleException
//	{
//		try {
//			// TODO: Make configurable where to find the user transaction - maybe delegate this to the
//			// j2ee adapter.
//			return (TransactionManager)getInitialContext().lookup("java:/TransactionManager");
//		} catch (NamingException e) {
//			throw new ModuleException(e);
//		}
//	}

}
