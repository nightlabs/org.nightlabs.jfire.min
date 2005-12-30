/*
 * Created on 02.03.2004
 *
 */
package org.nightlabs.jfire.security;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSearchResult;
import org.nightlabs.jfire.security.SecurityException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.RoleImportSet;

import org.nightlabs.ModuleException;

/**
 * @author nick
 */

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/RoleManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/RoleManager"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical"
 **/
public abstract class RoleManagerBean extends BaseSessionBeanImpl implements SessionBean  
{

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="RoleManager-read"
	 */
	public void ejbCreate() throws CreateException
	{
//		try
//		{
//			System.out.println("RoleManager created by " + this.getPrincipalString());
//		}
//		catch (ModuleException e)
//		{
//			throw new CreateException(e.getMessage());
//		}
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="RoleManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public void createRole(String roleID)
	throws SecurityException
	{
		System.out.println("RoleManagerBean.createRole");
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				Role newRole = new Role(roleID);
				pm.makePersistent(newRole);
				System.out.println("new role created..");
			} finally {
				if (AuthorityManagerBean.CLOSE_PM) pm.close();
			}
		} catch(Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="RoleManager-read"
	 **/
	public RoleGroupSearchResult searchRoleGroups (
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, int roleGroupIncludeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				RoleGroupSearchResult result = RoleGroup.searchRoleGroups(
						pm,
						searchStr, exact, itemsPerPage, pageIndex);
				result.makeTransient(roleGroupIncludeMask);
				return result;
			} finally {
				if (AuthorityManagerBean.CLOSE_PM) pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="RoleManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public RoleImportSet roleImport_prepare()
	throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.roleImport_prepare(getOrganisationID());
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="RoleManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public void roleImport_commit(RoleImportSet roleImportSet)
	throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.roleImport_commit(roleImportSet);
		} finally {
			ism.close();
		}
	}
	
//	/**
//	 * @ejb.interface-method
//	 *	view-type="remote"
//	 *
//	 * @ejb.permission role-name="RoleManager-write"
//	 **/
//	public void addUserToRole(String roleID, String userID)
//	throws SecurityException
//	{
//		System.out.println("RoleManagerBean.addUserToRole");
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try
//			{
//				// initialize jdo meta data
//				pm.getExtent(User.class, true);
//				pm.getExtent(Role.class, true);
//
//				// find user & role and create links between
//				User usr = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
//				Role role = (Role)pm.getObjectById(RoleID.create(roleID), true);
//				role.addUser(usr);
//			} finally {
//				if (AuthorityManagerBean.CLOSE_PM) pm.close();
//			}
//		} catch (Exception x) {
//			throw new SecurityException(x);
//		}
//	}
	
}
