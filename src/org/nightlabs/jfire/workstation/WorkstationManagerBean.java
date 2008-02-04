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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 * @ejb.bean name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  jndi-name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  type="Stateless" 
 * 
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public class WorkstationManagerBean extends BaseSessionBeanImpl implements SessionBean
{
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"  
	 */
	public void ejbCreate() throws CreateException
	{
	}

	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	public void ejbActivate() throws EJBException, RemoteException
	{
	}

	public void ejbPassivate() throws EJBException, RemoteException
	{
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="WorkstationManagerBean-write"
	 * @ejb.transaction type="Required"
	 **/
	public Workstation storeWorkstation(Workstation ws, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException 
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

	/**
	 * @throws ModuleException 
	 * @ejb.interface-method
	 * @ejb.permission role-name="WorkstationManagerBean-read"
	 * @ejb.transaction type="Required"
	 **/
	public Workstation getWorkstation(WorkstationID workstationID, String [] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try 
		{
			if (fetchGroups != null) 
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			
			Workstation ret = (Workstation) pm.getObjectById(workstationID);
			return pm.detachCopy(ret);
		} 
		finally 
		{
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="WorkstationManagerBean-read"
	 * @ejb.transaction type="Required"
	 **/
	public Collection getWorkstations(String[] fetchGroups, int maxFetchDepth)
	{		
		PersistenceManager pm = getPersistenceManager();
		try 
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null) 
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection ret = Workstation.getWorkstations(pm);
			return pm.detachCopyAll(ret);
		} 
		finally 
		{
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 **/
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 **/
	@SuppressWarnings("unchecked")
	public List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, workstationIDs, null, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */	
	public Set<WorkstationID> getWorkstaionIDs(Collection<WorkstationQuery> workstationQueries) {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			Set<Workstation> workstations = null;
			for (WorkstationQuery query : workstationQueries) {
				query.setPersistenceManager(pm);
				query.setCandidates(workstations);
				workstations = new HashSet<Workstation>(query.getResult());
			}

			return NLJDOHelper.getObjectIDSet(workstations);
		} finally {
			pm.close();
		}		
	}	
}
