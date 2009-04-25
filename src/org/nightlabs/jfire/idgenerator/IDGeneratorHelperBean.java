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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.BaseSessionBeanImplEJB3;
import org.nightlabs.jfire.idgenerator.id.IDNamespaceID;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	jndi-name="jfire/ejb/JFireBaseBean/IDGeneratorHelper"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless(name="org.nightlabs.jfire.idgenerator.IDGeneratorHelperBean")
public class IDGeneratorHelperBean
extends BaseSessionBeanImplEJB3 implements IDGeneratorHelperRemote, IDGeneratorHelperLocal
{
	private static final Logger logger = Logger.getLogger(IDGeneratorHelperBean.class);
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.idgenerator.IDGeneratorHelperRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	public static final long LIMIT_LOG_WARN = Long.MAX_VALUE / 10 * 8; // log a warning above 80 %
	public static final long LIMIT_LOG_ERROR = Long.MAX_VALUE / 10 * 9; // log an error above 90 %
	public static final long LIMIT_LOG_FATAL = Long.MAX_VALUE / 100 * 95; // log an error above 95 %
	public static final long LIMIT_FAIL = Long.MAX_VALUE - 1000; // throw an exception

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.idgenerator.IDGeneratorHelperLocal#serverNextIDs(java.lang.String, int, int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	@Override
	public long[] serverNextIDs(String namespace, int currentCacheSize, int minCacheSize)
	{
		if (logger.isTraceEnabled()) {
			logger.trace("serverNextIDs: entered (namespace=\"" + namespace + "\")");
		}

		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getExtent(IDNamespace.class);
			String organisationID = getOrganisationID();

			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				IDNamespace idNamespace = IDNamespace.getIDNamespace(pm, organisationID, namespace);
				long nextID = idNamespace.getNextID();

				if (logger.isTraceEnabled()) {
					logger.trace("serverNextIDs: Obtained IDNamespace from JDO: " + idNamespace);
				}

				int quantity = minCacheSize - currentCacheSize + idNamespace.getCacheSizeServer();
				if (quantity <= 0)
					return new long[0];

				long[] res = new long[quantity];

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

			if (logger.isTraceEnabled()) {
				logger.trace("serverNextIDs: exiting (namespace=\"" + namespace + "\")");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.idgenerator.IDGeneratorHelperRemote#clientNextIDs(java.lang.String, int, int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public long[] clientNextIDs(String namespace, int currentCacheSize, int minCacheSize)
	{
		if (logger.isTraceEnabled()) {
			logger.trace("clientNextIDs: entered (namespace=\"" + namespace + "\")");
		}

		PersistenceManager pm = this.getPersistenceManager();
		int quantity;
		try {
			pm.getExtent(IDNamespace.class);

			IDNamespace idNamespace = null;
			int cacheSizeClient = 0;
			try {
				idNamespace = (IDNamespace) pm.getObjectById(IDNamespaceID.create(getOrganisationID(), namespace));
				cacheSizeClient = idNamespace.getCacheSizeClient();

				if (logger.isTraceEnabled()) {
					logger.trace("clientNextIDs: obtained IDNamespace for finding out cacheSizeClient=" + cacheSizeClient + ": " + idNamespace);
				}
			} catch (JDOObjectNotFoundException e) {
				// no IDNamespace => we request exactly the number of ids that were defined by minCacheSize
				if (logger.isTraceEnabled()) {
					logger.trace("clientNextIDs: no IDNamespace existing for namespace=\"" + namespace + "\", generating only exactly the requested quantity of IDs.");
				}
			}

			quantity = minCacheSize - currentCacheSize + cacheSizeClient;
			if (quantity <= 0) {
				if (logger.isTraceEnabled()) {
					logger.trace("clientNextIDs: exiting with empty result (namespace=\"" + namespace + "\")");
				}

				return new long[0];
			}

		} finally {
			pm.close();
		}

		long[] res = IDGenerator.nextIDs(namespace, quantity);

		if (logger.isTraceEnabled()) {
			String firstID = res.length > 0 ? ObjectIDUtil.longObjectIDFieldToString(res[0]) : "";
			String lastID = res.length > 0 ? ObjectIDUtil.longObjectIDFieldToString(res[res.length - 1]) : "";
			logger.trace("clientNextIDs: exiting with " + res.length + " IDs (namespace=\"" + namespace + "\" firstID=" + firstID + " lastID=" + lastID + ")");
		}

		return res;
	}
}
