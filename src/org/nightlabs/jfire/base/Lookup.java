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

import org.apache.log4j.Logger;
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
 * This class is a util to make it easier to look up objects from JNDI.
 * 
 * @author marco
 */
///*// this is now ignored - we expect the system persistence manager to be always "java:/jfire/persistenceManagers/system" 
// * @ejb.env-entry name = "jfire/persistenceManagers/system"
// * 	type = "java.lang.String"
// * 	value = "java:/jfire/persistenceManagers/system"
// **/
public class Lookup 
{
	public static final Logger LOGGER = Logger.getLogger(Lookup.class);

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
	public JFireServerManagerFactory getJFireServerManagerFactory()
		throws ModuleException
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
				throw new ModuleException(e);
			}
		
		return jfireServerManagerFactory;
	}

	public JFireServerManager getJFireServerManager()
	throws ModuleException
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
	throws InitException
	{	
		this.organisationID = _organisationId;
		
//		try {
//			this.initialContext = new InitialContext();
//			
////			this.jfireServerManagerFactory = (JFireServerManagerFactory)
////					getInitialContext().lookup(IPANEMA_SERVER_MANAGER_FACTORY_JNDI_NAME);
////			this.persistenceManagerFactory = getPersistenceManagerFactory(organisationID);
//		} catch (Exception x) {
//			throw new InitException(x);
//		}
	}
	
//	private InitialContext initialContext;
//	
//	/**
//	 * @return The default initial context.
//	 * @throws ModuleException
//	 */
//	public InitialContext getInitialContext()
//		throws ModuleException
//	{
//		if (initialContext == null) {
//			try {
//				this.initialContext = new InitialContext();
//			} catch (Exception x) {
//				throw new ModuleException(x);
//			}
//		}
//
//		return initialContext; 
//	}

//	private PersistenceManagerFactory persistenceManagerFactory;

//	private static final String PMF_PROPS_KEY_INITIALIZED = "__"+Lookup.class.getName()+".isInitialized"; 

	/**
	 * @return the pmfactory for the organisationID of the working user.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory()
		throws ModuleException
	{
//		if (!CACHE_PERSISTENCEMANAGERFACTORIES || persistenceManagerFactory == null || persistenceManagerFactory.isClosed())
//			persistenceManagerFactory = getPersistenceManagerFactory(organisationID);
//
//		return persistenceManagerFactory;
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
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//		// TO DO remove this WORKAROUND: the PersistenceManagerFactory should reset the fetchplan! Not necessary?!
//		pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
//		pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);
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
	 * @throws NamingException if a naming exception is encountered. 
	 */
	protected PersistenceManagerFactory getPersistenceManagerFactory(String organisationID)
	throws ModuleException
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
			throw new ModuleException(e);
		}
		return pmf;
	}
	
	protected PersistenceManager getPersistenceManager(String organisationId)
	throws ModuleException
	{
		return getPersistenceManagerFactory(organisationId).getPersistenceManager();
	}

//	/**
//	 * key: String organisationID<br/>
//	 * value: InitialContext initialContext 
//	 */
//	protected Map initCtxMap = new HashMap();
//
//	/**
//	 * Do NOT forget to close the InitialContext you retrieved!
//	 */
//	public InitialContext getInitialContext(String _organisationID)
//	throws ModuleException
//	{
////		InitialContext ctx = (InitialContext)initCtxMap.get(_organisationID);
////		
////		if (ctx == null) {						
////			try {
////				ctx = new InitialContext(getInitialContextProps(_organisationID));
////			} catch (NamingException e) {
////				throw new ModuleException(e);
////			}			
////			initCtxMap.put(_organisationID, ctx);
////		}
////		
////		return ctx;
//		try {
//			return new InitialContext(getInitialContextProps(_organisationID));
//		} catch (NamingException e) {
//			throw new ModuleException(e);
//		}			
//	}
//
//	/**
//	 * key: String organisationID
//	 * value: Properties initialContextProperties
//	 */
//	protected HashMap initialContextPropsMap = new HashMap();
//
//	/**
//	 * @param organisationID
//	 * @return This method returns <tt>null</tt> if the given organisationID is the local one (we don't login into ourselves).
//	 * @throws ModuleException
//	 */
//	public Hashtable getInitialContextProps(String organisationID)
//		throws ModuleException
//	{
//		if (this.getOrganisationID().equals(organisationID))
//			return null;
//
//		java.util.Hashtable props = (Properties) initialContextPropsMap.get(organisationID);
//		if (props == null) {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				props = _getInitialContextProps(pm, organisationID);
//			} finally {
//				pm.close();
//			}
//			initialContextPropsMap.put(organisationID, props);
//		} // if (props == null) {
//		return props;
//	}

	/**
	 * This method reads the properties out of the datastore managed by pm, that are necessary
	 * to connect and login to the organisation defined by _organisationID. If _organisationID
	 * is the local organisation (managed by pm), this method returns <tt>null</tt>! 
	 */
	public static Hashtable getInitialContextProperties(PersistenceManager pm, String _organisationID) throws ModuleException
	{
		try {
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
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
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

	public CacheManagerFactory getCacheManagerFactory()
	throws ModuleException
	{
		try {
			InitialContext ctx = new InitialContext();
			try {
				return CacheManagerFactory.getCacheManagerFactory(ctx, getOrganisationID());
			} finally {
				ctx.close();
			}
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * @see CacheManagerFactory#getCacheManager()
	 */
	public CacheManager getCacheManager()
	throws ModuleException
	{
		return getCacheManagerFactory().getCacheManager();
	}

	/**
	 * @see CacheManagerFactory#getCacheManager(String)
	 */
	public CacheManager getCacheManager(String cacheSessionID)
	throws ModuleException
	{
		return getCacheManagerFactory().getCacheManager(cacheSessionID);
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
