/*
 * Created on 18.03.2004
 */
package org.nightlabs.jfire.idgenerator;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGeneratorAssistant;

import org.nightlabs.ModuleException;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/IDGenerator"
 *	jndi-name="jfire/ejb/JFireBaseBean/IDGenerator"
 *	type="Stateless"
 *
 * @ejb.permission role-name = "_Guest_"
 *
 * @ejb.util generate = "physical"
 **/

public abstract class IDGeneratorBean extends BaseSessionBeanImpl implements SessionBean 
{
	/**
	 * @ejb.create-method
	 */
	public void ejbCreate() throws CreateException
	{
		try
		{
			System.out.println("IDGeneratorBean created by " + this.getPrincipalString());
		}
		catch (ModuleException e)
		{
			throw new CreateException(e.getMessage());
		}
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method generates a new ID that is unique in the context of the current
	 * organisationID and has never been used before - means can be used for a new object.
	 * @param key Normally, this is the class name of the object that should be persisted. 
	 * @return The new ID
	 * 
	 * @ejb.interface-method 
	 * @ejb.transaction type = "Required"
	 */
	public long generateIDLong(String key)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{			
			return IDGeneratorAssistant.generateIDLong(pm, key);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method generates a new ID that is unique in the context of the current
	 * organisationID and has never been used before - means can be used for a new object.
	 * @param key Normally, this is the class name of the object that should be persisted. 
	 * @return The new ID
	 * 
	 * @ejb.interface-method 
	 * @ejb.transaction type = "Required" 
	 */
	public int generateIDInt(String key)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {			
			return IDGeneratorAssistant.generateIDInt(pm, key);
		} finally {
			pm.close();
		}
	}

}