/**
 * Created Jul 26, 2005, 6:45:23 PM by nick
 */
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
	public Collection getWorkstations(String[] fetchGroups)
	throws ModuleException
	{		
    PersistenceManager pm = getPersistenceManager();
    try 
    {
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
