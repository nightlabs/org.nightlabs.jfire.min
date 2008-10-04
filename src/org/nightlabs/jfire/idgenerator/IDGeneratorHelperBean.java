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
package org.nightlabs.jfire.idgenerator;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.id.IDNamespaceID;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	jndi-name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class IDGeneratorHelperBean
extends BaseSessionBeanImpl implements SessionBean
{
	private static final Logger logger = Logger.getLogger(IDGeneratorHelperBean.class);
	private static final long serialVersionUID = 1L;

	@Override
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException { }

	public static final long LIMIT_LOG_WARN = Long.MAX_VALUE / 10 * 8; // log a warning above 80 %
	public static final long LIMIT_LOG_ERROR = Long.MAX_VALUE / 10 * 9; // log an error above 90 %
	public static final long LIMIT_LOG_FATAL = Long.MAX_VALUE / 100 * 95; // log an error above 95 %
	public static final long LIMIT_FAIL = Long.MAX_VALUE - 1000; // throw an exception

	/**
	 * This method is called by the {@link IDGeneratorServer} and not visible to a remote client.
	 * Warning: You should not use this method, but instead call {@link IDGenerator#nextIDs(String, int)}!!! This
	 * method is only used internally.
	 *
	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
	 * @param currentCacheSize The current number of cached IDs.
	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="RequiresNew"
	 **/
	public long[] serverNextIDs(String namespace, int currentCacheSize, int minCacheSize)
	throws ModuleException
	{
		try {
			PersistenceManager pm = this.getPersistenceManager();
			try {
				pm.getExtent(IDNamespace.class);
				String organisationID = getOrganisationID();

				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {

					IDNamespace idNamespace = IDNamespace.getIDNamespace(pm, organisationID, namespace);
//					try {
//					idNamespace = (IDNamespace) pm.getObjectById(IDNamespaceID.create(organisationID, namespace));
//					idNamespace.getCacheSizeServer(); // workaround for JPOX bug - the JDOObjectNotFoundException doesn't occur always in the above line
//					} catch (JDOObjectNotFoundException e) {
//					idNamespace = new IDNamespace(
//					getOrganisationID(),
//					namespace,
//					IDNamespaceDefault.getIDNamespaceDefault(pm, organisationID, namespace));
//					idNamespace = (IDNamespace) pm.makePersistent(idNamespace);
//					}

					int quantity = minCacheSize - currentCacheSize + idNamespace.getCacheSizeServer();
					if (quantity <= 0)
						return new long[0];

					long[] res = new long[quantity];
					long nextID = idNamespace.getNextID();

					if (nextID > LIMIT_FAIL - quantity)
						throw new IllegalStateException("nextID too high!!! [organisationID=\""+organisationID+"\", namespace=\""+namespace+"\", nextID=\"" + nextID + "\"]");

					for (int i = 0; i < quantity; ++i) {
						res[i] = nextID++;
					}
					idNamespace.setNextID(nextID);

					if (nextID > LIMIT_LOG_FATAL)
						logger.fatal("nextID above LIMIT_LOG_FATAL (> 95%): [organisationID=\""+organisationID+"\", namespace=\""+namespace+"\", nextID=\"" + nextID + "\", LIMIT_LOG_FATAL=\""+LIMIT_LOG_FATAL+"\"]");
					else if (nextID > LIMIT_LOG_ERROR)
						logger.error("nextID above LIMIT_LOG_ERROR (> 90%): [organisationID=\""+organisationID+"\", namespace=\""+namespace+"\", nextID=\"" + nextID + "\", LIMIT_LOG_ERROR=\""+LIMIT_LOG_ERROR+"\"]");
					else if (nextID > LIMIT_LOG_WARN)
						logger.warn("nextID above LIMIT_LOG_WARN (> 80%): [organisationID=\""+organisationID+"\", namespace=\""+namespace+"\", nextID=\"" + nextID + "\", LIMIT_LOG_WARN=\""+LIMIT_LOG_WARN+"\"]");

					return res;
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}
			} finally {
				pm.close();
			}
		} catch(Exception e) {
			throw new ModuleException(e);
		}
	}

	/**
	 * This method is called by the client side {@link IDGenerator} implementation.
	 * Warning: You should not use this method, but instead call {@link IDGenerator#nextIDs(String, int)}!!! This
	 * method is only used internally.
	 *
	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
	 * @param currentCacheSize The current number of cached IDs.
	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
	 *
	 * @ejb.interface-method view-type="remote"
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 **/
	public long[] clientNextIDs(String namespace, int currentCacheSize, int minCacheSize)
	throws ModuleException
	{
		try {
			PersistenceManager pm = this.getPersistenceManager();
			int quantity;
			try {
				pm.getExtent(IDNamespace.class);

				IDNamespace idNamespace = null;
				int cacheSizeClient = 0;
				try {
					idNamespace = (IDNamespace) pm.getObjectById(IDNamespaceID.create(getOrganisationID(), namespace));
					cacheSizeClient = idNamespace.getCacheSizeClient();
				} catch (JDOObjectNotFoundException e) {
					// no IDNamespace => we request exactly the number of ids that were defined by minCacheSize
				}

				quantity = minCacheSize - currentCacheSize + cacheSizeClient;
				if (quantity <= 0)
					return new long[0];

			} finally {
				pm.close();
			}

			return IDGenerator.nextIDs(namespace, quantity);
		} catch(Exception e) {
			throw new ModuleException(e);
		}
	}
}
