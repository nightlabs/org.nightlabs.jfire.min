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
