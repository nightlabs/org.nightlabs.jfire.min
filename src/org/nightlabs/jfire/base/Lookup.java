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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.jdo.cache.CacheManager;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.TemporaryOrganisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.organisation.id.TemporaryOrganisationID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;

/**
 * This class is a util to make it easier to look up important JFire objects from JNDI.
 *
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class Lookup
{
	private static final Logger logger = Logger.getLogger(Lookup.class);

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

	protected static JFireServerManagerFactory _getJFireServerManagerFactory()
	{
		try {
			InitialContext ctx = new InitialContext();
			try {
				return (JFireServerManagerFactory)ctx.lookup(JFireServerManagerFactory.JNDI_NAME);
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @throws RuntimeException A properly configured server should not have problems with JNDI. Therefore,
	 *		we wrap the NamingException in a RuntimeException.
	 */
	public JFireServerManagerFactory getJFireServerManagerFactory()
	{
		if (jfireServerManagerFactory == null)
			jfireServerManagerFactory = _getJFireServerManagerFactory();

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
	protected PersistenceManagerFactory getPersistenceManagerFactory()
	{
		return getPersistenceManagerFactory(organisationID);
	}

	/**
	 * This method returns a PersistenceManager that is providing access to
	 * the database that is linked to the organisationID of the current user.
	 * <p>
	 * <b>Important:</b> You must call {@link PersistenceManager#close()} at the end of your EJB method!
	 * </p>
	 *
	 * @return a new {@link PersistenceManager} obtained from the {@link PersistenceManagerFactory} accessible via {@link #getPersistenceManagerFactory()}.
	 * @deprecated Use {@link #createPersistenceManager()} instead. Deprecated since 2009-06-12 in order to emphasize by "create" that the returned {@link PersistenceManager} must be closed.
	 */
	@Deprecated
	public PersistenceManager getPersistenceManager()
	{
		return createPersistenceManager();
	}

	/**
	 * This method returns a PersistenceManager that is providing access to
	 * the database that is linked to the organisationID of the current user.
	 * <p>
	 * <b>Important:</b> You must call {@link PersistenceManager#close()} at the end of your EJB method!
	 * </p>
	 *
	 * @return a new {@link PersistenceManager} obtained from the {@link PersistenceManagerFactory} accessible via {@link #getPersistenceManagerFactory()}.
	 */
	public PersistenceManager createPersistenceManager()
	{
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

		// TODO check whether we can set these options as configuration settings of the JDO implementation
		pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);

		// JPOX WORKAROUND - some pk-fields are sometimes null otherwise
		pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		// example stack trace for this:
//		17:09:19,475 ERROR [LogInterceptor] RuntimeException in method: public abstract void org.nightlabs.jfire.editlock.EditLockManager.releaseEditLock(org.nightlabs.jdo.ObjectID,org.nightlabs.jfire.editlock.ReleaseReason) throws org.nightlabs.ModuleException,java.rmi.RemoteException:
//			javax.jdo.JDOObjectNotFoundException: No such database row
//			FailedObject:jdo/org.nightlabs.jfire.editlock.id.EditLockID?organisationID=null&editLockID=44355
//			        at org.jpox.store.rdbms.request.FetchRequest.execute(FetchRequest.java:194)
//			        at org.jpox.store.rdbms.table.ClassTable.fetch(ClassTable.java:2552)
//			        at org.jpox.store.StoreManager.fetch(StoreManager.java:941)
//			        at org.jpox.state.StateManagerImpl.loadNonDFGFields(StateManagerImpl.java:1734)
//			        at org.jpox.state.StateManagerImpl.isLoaded(StateManagerImpl.java:2034)
//			        at org.nightlabs.jfire.editlock.EditLock.jdoGeteditLockType(EditLock.java)
//			        at org.nightlabs.jfire.editlock.EditLock.getEditLockType(EditLock.java:329)
//			        at org.nightlabs.jfire.editlock.EditLock.releaseEditLock(EditLock.java:193)
//			        at org.nightlabs.jfire.editlock.EditLockManagerBean.releaseEditLock(EditLockManagerBean.java:180)
//			        at sun.reflect.GeneratedMethodAccessor163.invoke(Unknown Source)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//			        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//			        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//			        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//			        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//			        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//			        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//			        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//			        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//			        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//			        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:138)
//			        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//			        at org.jboss.ejb.Container.invoke(Container.java:960)
//			        at sun.reflect.GeneratedMethodAccessor111.invoke(Unknown Source)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			        at org.jboss.invocation.unified.server.UnifiedInvoker.invoke(UnifiedInvoker.java:231)
//			        at sun.reflect.GeneratedMethodAccessor148.invoke(Unknown Source)
//			        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//			        at java.lang.reflect.Method.invoke(Method.java:585)
//			        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//			        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//			        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//			        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//			        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//			        at javax.management.MBeanServerInvocationHandler.invoke(MBeanServerInvocationHandler.java:201)
//			        at $Proxy16.invoke(Unknown Source)
//			        at org.jboss.remoting.ServerInvoker.invoke(ServerInvoker.java:734)
//			        at org.jboss.remoting.transport.socket.ServerThread.processInvocation(ServerThread.java:560)
//			        at org.jboss.remoting.transport.socket.ServerThread.dorun(ServerThread.java:383)
//			        at org.jboss.remoting.transport.socket.ServerThread.run(ServerThread.java:165)

		if (logger.isDebugEnabled())
			logger.debug("getPersistenceManager: " + pm);

		NLJDOHelper.setThreadPersistenceManager(pm);

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
		Map<String, PersistenceManagerFactory> organisationID2PersistenceManagerFactory = organisationID2PersistenceManagerFactoryTL.get();
		PersistenceManagerFactory pmf = organisationID2PersistenceManagerFactory.get(organisationID);
		if (pmf != null && pmf.isClosed()) {
			if (logger.isDebugEnabled())
				logger.debug("getPersistenceManagerFactory(organisationID=" + organisationID + "): Found PMF in ThreadLocal, but it is closed: " + pmf);

			pmf = null;
		}

		if (pmf == null) {
			try {
				InitialContext ctx = new InitialContext();
				try {
					pmf = (PersistenceManagerFactory)ctx.lookup(OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
				} finally {
					ctx.close();
				}
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}

			if (logger.isDebugEnabled())
				logger.debug("getPersistenceManagerFactory(organisationID=" + organisationID + "): Obtained PMF from JNDI: " + pmf);

			organisationID2PersistenceManagerFactory.put(organisationID, pmf);
		} // if (pmf == null) {
		else {
			if (logger.isDebugEnabled())
				logger.debug("getPersistenceManagerFactory(organisationID=" + organisationID + "): Found usable PMF in ThreadLocal: " + pmf);
		}
		return pmf;
	}

	private static ThreadLocal<Map<String, PersistenceManagerFactory>> organisationID2PersistenceManagerFactoryTL = new ThreadLocal<Map<String,PersistenceManagerFactory>>() {
		@Override
		protected Map<String, PersistenceManagerFactory> initialValue() {
			return new HashMap<String, PersistenceManagerFactory>();
		}
	};

//	protected PersistenceManager getPersistenceManager(String organisationId)
//	{
//		return getPersistenceManagerFactory(organisationId).getPersistenceManager();
//	}

	/**
	 * Get the initial-context-properties for connecting via an EJB to any organisation
	 * known to the current (=local) organisation (including itself).
	 * <p>
	 * This method reads the properties from the datastore managed by the given
	 * {@link PersistenceManager} (i.e. the local organisation's datastore). These
	 * properties can be passed to an EJB-Util class in order to obtain an EJB instance that
	 * allows communication with the specified organisation. If <code>organisationID</code>
	 * is the local organisation (managed by <code>pm</code>), this method returns <code>null</code>
	 * (which is a valid value for said EJB-Util classes and causes creation of an EJB that communicates
	 * with the local organisation).
	 * </p>
	 *
	 * @param pm the door to the datastore.
	 * @param organisationID the identifier of the organisation to connect+login to.
	 * @throws NamingException if accessing JNDI fails.
	 */
	public static Hashtable<?, ?> getInitialContextProperties(PersistenceManager pm, String organisationID)
	throws NamingException
	{
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
		if (organisationID.equals(localOrganisation.getOrganisationID()))
			return null;

		Server localServer = localOrganisation.getOrganisation().getServer();

		InitialContext initCtx = new InitialContext();
		try {
			JFireServerManagerFactory jfireServerManagerFactory = (JFireServerManagerFactory)initCtx.lookup(JFireServerManagerFactory.JNDI_NAME);

			String password = localOrganisation.getPassword(organisationID);
			if (password == null)
				throw new IllegalStateException("localOrganisation.getPassword(organisationID) returned null! localOrganisationID=" + localOrganisation.getOrganisationID() + " organisationID=" + organisationID);

			Server server = null;
			try {
				Organisation organisation = (Organisation) pm.getObjectById(OrganisationID.create(organisationID));
				server = organisation.getServer();
			} catch (JDOObjectNotFoundException x) {
				// the organisation doesn't exist - try it with a TemporaryOrganisation instance
			}

			if (server == null) {
				try {
					TemporaryOrganisation temporaryOrganisation = (TemporaryOrganisation) pm.getObjectById(TemporaryOrganisationID.create(organisationID));
					server = temporaryOrganisation.getOrganisation().getServer();
				} catch (JDOObjectNotFoundException x) {
					throw new IllegalStateException("There is neither an Organisation nor a TemporaryOrganisation existing with the organisationID=" + organisationID);
				}
			}

			String protocol;
			if (localServer.getDistinctiveDataCentreID().equals(server.getDistinctiveDataCentreID()))
				protocol = Server.PROTOCOL_JNP;
			else
				protocol = Server.PROTOCOL_HTTPS;

			String initialContextFactory = jfireServerManagerFactory.getLoginInitialContextFactory(
					server.getJ2eeServerType(), protocol, true
			);
			String initialContextURL = server.getInitialContextURL(protocol, true);

			return InvokeUtil.getInitialContextProperties(
					initialContextFactory,
					initialContextURL,
					UserID.create(
							organisationID,
							User.USER_ID_PREFIX_TYPE_ORGANISATION + localOrganisation.getOrganisationID()
					),
					password
			);
		} finally {
			initCtx.close();
		}
	}

//	protected Properties _getInitialContextProps(PersistenceManager pm, String _organisationID) throws ModuleException
//	{
//		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
//		if (_organisationID.equals(localOrganisation.getOrganisationID()))
//			return null;
//
//		String password = localOrganisation.getPassword(_organisationID);
//
//		Organisation organisation = (Organisation)pm.getObjectById(OrganisationID.create(_organisationID), true);
//		Server server = organisation.getServer();
//		JFireServerManagerFactory jFireServerManagerFactory = getJFireServerManagerFactory();
//		return InvokeUtil.getInitialContextProperties(
//				jFireServerManagerFactory.getInitialContextFactory(server.getJ2eeServerType(), true),
//				server.getInitialContextURL(),
//				_organisationID,
//				User.USER_ID_PREFIX_TYPE_ORGANISATION + localOrganisation.getOrganisationID(), password);
////		String initialContextFactory = getJFireServerManagerFactory().getInitialContextFactory(server.getJ2eeServerType(), true);
////		return _getInitialContextProps(
////				initialContextFactory, server.getInitialContextURL(),
////				localOrganisation.getOrganisationID(),
////				_organisationID, password);
//	}

//	protected static Hashtable _getInitialContextProps(
//			String initialContextFactory, String initialContextURL,
//			String localOrganisationID, String remoteOrganisationID, String password)
//	{
//		String username = User.USER_ID_PREFIX_TYPE_ORGANISATION
//			+ localOrganisationID
//			+ '@'
//			+ remoteOrganisationID;
//
//		Properties props = new Properties();
//		props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
//		props.setProperty(InitialContext.PROVIDER_URL, initialContextURL);
//		props.setProperty(InitialContext.SECURITY_PRINCIPAL, username);
//		props.setProperty(InitialContext.SECURITY_CREDENTIALS, password);
//		props.setProperty(InitialContext.SECURITY_PROTOCOL, "jfire");
//
//		props.setProperty("jnp.multi-threaded", String.valueOf(true));
//		props.setProperty("jnp.restoreLoginIdentity", String.valueOf(true));
//		return props;
//	}

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
