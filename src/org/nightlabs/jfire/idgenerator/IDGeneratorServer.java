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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.naming.InitialContext;

import org.nightlabs.jfire.security.SecurityReflector;

/**
 * This implementation of {@link IDGenerator} is used in on the server side for ID generation.
 * It holds a local cache of IDs and queries IDs via a local EJB call to
 * {@link IDGeneratorHelperBean#serverNextIDs(String, int, int)}.
 * <p>
 * You <b>must not</b> use this class directly! Use the static methods provided by {@link IDGenerator} instead!
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class IDGeneratorServer
		extends IDGenerator
{

	/**
	 * key: String organisationID<br/>
	 * value: Map {<br/>
	 * 		key: String namespace<br/>
	 * 		value: LinkedList cachedIDs<br/>
	 * }
	 */
	private Map<String, Map<String, LinkedList<Long>>> organisationID2IDCache = new HashMap<String, Map<String, LinkedList<Long>>>();

	private SecurityReflector securityReflector = null;

	@Override
	protected String _getOrganisationID()
	{
		try {
			InitialContext initialContext = null;
			try {
				if (securityReflector == null) {
					if (initialContext == null)
						initialContext = new InitialContext();

					securityReflector = SecurityReflector.lookupSecurityReflector(initialContext);
				}
				return securityReflector._getUserDescriptor().getOrganisationID();
			} finally {
				if (initialContext != null)
					initialContext.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	protected long[] _nextIDs(String namespace, int quantity)
	{
		// We have for sure no problem as long as the server has only one VM. But,
		// if it's running in a cluster and thus has many VMs, the synchronized blocks here
		// are not sufficient. In this case, it is necessary that the JDO backend (e.g. JPOX)
		// ensures that no two transactions retrieve the same IDs.
		// In case there are issues in a cluster which cannot be solved by JDO, we have to implement
		// a way to synchronise the different VMs.

		try {
			String organisationID;

			InitialContext initialContext = null;
			try {
				if (securityReflector == null) {
					if (initialContext == null)
						initialContext = new InitialContext();

					securityReflector = SecurityReflector.lookupSecurityReflector(initialContext);
				}
				organisationID = securityReflector._getUserDescriptor().getOrganisationID();

				Map<String, LinkedList<Long>> namespace2cachedIDs;
				synchronized (organisationID2IDCache) {
					namespace2cachedIDs = organisationID2IDCache.get(organisationID);
					if (namespace2cachedIDs == null) {
						namespace2cachedIDs = new HashMap<String, LinkedList<Long>>();
						organisationID2IDCache.put(organisationID, namespace2cachedIDs);
					}
				} // synchronized (organisationID2IDCache) {

				LinkedList<Long> cachedIDs;
				synchronized (namespace2cachedIDs) {
					cachedIDs = namespace2cachedIDs.get(namespace);
					if (cachedIDs == null) {
						cachedIDs = new LinkedList<Long>();
						namespace2cachedIDs.put(namespace, cachedIDs);
					}
				} // synchronized (namespace2cachedIDs) {

				synchronized (cachedIDs) {
					if (quantity > cachedIDs.size()) {
						if (initialContext == null)
							initialContext = new InitialContext();

						Object objRef = initialContext.lookup(IDGeneratorHelperLocalHome.JNDI_NAME);
						IDGeneratorHelperLocalHome home;
		        // only narrow if necessary
//		        if (java.rmi.Remote.class.isAssignableFrom(IDGeneratorHelperLocalHome.class)) // this was wrong - wasn't it?
						if (!(objRef instanceof IDGeneratorHelperLocalHome))
		           home = (IDGeneratorHelperLocalHome) javax.rmi.PortableRemoteObject.narrow(objRef, IDGeneratorHelperLocalHome.class);
		        else
		           home = (IDGeneratorHelperLocalHome) objRef;

						IDGeneratorHelperLocal idGeneratorHelper = home.create();
						long[] nextIDs = idGeneratorHelper.serverNextIDs(namespace, cachedIDs.size(), quantity);
						for (int i = 0; i < nextIDs.length; i++) {
							cachedIDs.add(new Long(nextIDs[i]));
						}
					}

					if (quantity > cachedIDs.size())
						throw new IllegalStateException("Number of cached IDs not sufficient!");

					long[] res = new long[quantity];
					for (int i = 0; i < quantity; ++i) {
						res[i] = cachedIDs.poll().longValue();
					}
					return res;
				} // synchronized (cachedIDs) {

			} finally {
				if (initialContext != null)
					initialContext.close();
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
