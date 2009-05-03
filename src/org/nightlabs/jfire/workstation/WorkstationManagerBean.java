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

package org.nightlabs.jfire.workstation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Niklas Schiffler - nick at nightlabs dot de
 * @author marco schulze - marco at nightlabs dot de
 *
 * @ejb.bean name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  jndi-name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class WorkstationManagerBean extends BaseSessionBeanImpl implements WorkstationManagerRemote
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.workstation.WorkstationManagerRemote#storeWorkstation(org.nightlabs.jfire.workstation.Workstation, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.workstation.storeWorkstation")
	@Override
	public Workstation storeWorkstation(Workstation ws, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

			Workstation ret = Workstation.storeWorkstation(pm, ws);
			if (get)
				return pm.detachCopy(ret);
			else
				return null;
		}
		finally
		{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.workstation.WorkstationManagerRemote#getWorkstationIDs()
	 */
	@RolesAllowed("org.nightlabs.jfire.workstation.queryWorkstations")
	@SuppressWarnings("unchecked")
	@Override
	public Set<WorkstationID> getWorkstationIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Workstation.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<WorkstationID>((Collection<? extends WorkstationID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.workstation.WorkstationManagerRemote#getWorkstations(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.workstation.queryWorkstations")
	@Override
	public List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, workstationIDs, null, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.workstation.WorkstationManagerRemote#getWorkstationIDs(org.nightlabs.jdo.query.QueryCollection)
	 */
	@RolesAllowed("org.nightlabs.jfire.workstation.queryWorkstations")
	@Override
	public Set<WorkstationID> getWorkstationIDs(QueryCollection<? extends WorkstationQuery> workstationQueries)
	{
		if (workstationQueries == null)
			return null;

		if (! Workstation.class.isAssignableFrom(workstationQueries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ workstationQueries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<? extends WorkstationQuery> decoratedCollection;
			if (workstationQueries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<? extends WorkstationQuery>) workstationQueries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<WorkstationQuery>(workstationQueries);
			}

			decoratedCollection.setPersistenceManager(pm);
			Collection<? extends Workstation> workstations = CollectionUtil.castCollection(decoratedCollection.executeQueries());

			return NLJDOHelper.getObjectIDSet(workstations);
		} finally {
			pm.close();
		}
	}
}
