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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.workstation.Workstation;

import org.nightlabs.ModuleException;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 * @ejb.bean name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  jndi-name="jfire/ejb/JFireBaseBean/WorkstationManager"
 *  type="Stateless" 
 * 
 * @ejb.util generate = "physical"
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
   * @throws ModuleException 
   * @ejb.interface-method
   * @ejb.permission role-name="WorkstationManagerBean-write"
   * @ejb.transaction type="Required"
   **/
  public Workstation saveWorkstation(Workstation ws, String [] fetchGroups) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
        pm.getFetchPlan().setGroups(fetchGroups);

      Workstation ret = Workstation.storeWorkstation(pm, ws);
      return (Workstation)pm.detachCopy(ret);
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
  public Workstation getWorkstation(String organisationID, String workstationID, String [] fetchGroups) 
    throws ModuleException
  {
    PersistenceManager pm = getPersistenceManager();
    try 
    {
      if (fetchGroups != null) 
        pm.getFetchPlan().setGroups(fetchGroups);

      Workstation ret = Workstation.getWorkstation(pm, organisationID, workstationID);
      return (Workstation)pm.detachCopy(ret);
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
	public Collection getWorkstations(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{		
    PersistenceManager pm = getPersistenceManager();
    try 
    {
    	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
      if (fetchGroups != null) 
        pm.getFetchPlan().setGroups(fetchGroups);

      Collection ret = Workstation.getWorkstations(pm);
      return (Collection)pm.detachCopyAll(ret);
    } 
    finally 
    {
      pm.close();
    }
	}
	
}