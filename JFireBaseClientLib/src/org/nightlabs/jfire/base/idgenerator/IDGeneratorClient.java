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
package org.nightlabs.jfire.base.idgenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.idgenerator.IDGeneratorHelperRemote;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * This implementation of {@link IDGenerator} is used in on the client side for ID generation.
 * It holds a local cache of IDs and queries IDs from the server via
 * {@link IDGeneratorHelperRemote#clientNextIDs(String, int, int)}.
 * <p>
 * You <b>must not</b> use this class directly! Use the static methods provided by {@link IDGenerator} instead!
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class IDGeneratorClient
		extends IDGenerator
{
	/**
	 * Either <code>null</code> or the organisationID for which we have a local cache.
	 * We throw the cache away and recreate it, if the user's organisation changes (re-login).
	 */
	private String organisationID = null;

	/**
	 * key: String namespace<br/>
	 * value: LinkedList cachedIDs<br/>
	 */
	private Map<String, LinkedList<Long>> namespace2cachedIDs = null;

	private Object namespace2cachedIDsMutex = new Object();

	@Override
	protected String _getOrganisationID()
	{
		try {
			return SecurityReflector.getUserDescriptor().getOrganisationID();
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	protected long[] _nextIDs(String namespace, int quantity)
	{
		try {
			LinkedList<Long> cachedIDs;
			synchronized (namespace2cachedIDsMutex) {
				String currentOrganisationID = getOrganisationID();

				if (!currentOrganisationID.equals(organisationID))
					namespace2cachedIDs = null;

				if (namespace2cachedIDs == null) {
					organisationID = currentOrganisationID;
					namespace2cachedIDs = new HashMap<String, LinkedList<Long>>();
				}

				cachedIDs = namespace2cachedIDs.get(namespace);
				if (cachedIDs == null) {
					cachedIDs = new LinkedList<Long>();
					namespace2cachedIDs.put(namespace, cachedIDs);
				}
			}

			synchronized (cachedIDs) {
				if (quantity > cachedIDs.size()) {
					Logger logger = Logger.getLogger(IDGeneratorClient.class);
					if (logger.isDebugEnabled()) {
						logger.debug("Quering server for new IDs. Have " + cachedIDs.size() + " entries, need " + quantity + ". Namespace: " + namespace);
					}
					IDGeneratorHelperRemote idGeneratorHelper = JFireEjb3Factory.getRemoteBean(IDGeneratorHelperRemote.class, SecurityReflector.getInitialContextProperties());
					long[] nextIDs = idGeneratorHelper.clientNextIDs(namespace, cachedIDs.size(), quantity);
					for (int i = 0; i < nextIDs.length; i++) {
						cachedIDs.add(Long.valueOf(nextIDs[i]));
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
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
