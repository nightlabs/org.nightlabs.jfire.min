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

package org.nightlabs.jfire.security;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityNotFoundException;
import org.nightlabs.jfire.security.AuthoritySearchResult;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.RoleGroupRefNotFoundException;
import org.nightlabs.jfire.security.RoleGroupRefSearchResult;
import org.nightlabs.jfire.security.SecurityException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserNotFoundException;
import org.nightlabs.jfire.security.UserRef;
import org.nightlabs.jfire.security.UserRefNotFoundException;
import org.nightlabs.jfire.security.UserRefSearchResult;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;

import org.nightlabs.ModuleException;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/AuthorityManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/AuthorityManager"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical"
 **/
public abstract class AuthorityManagerBean extends BaseSessionBeanImpl implements SessionBean
{
  // TODO hier weitermachen mit umschreiben von makeTransient nach detachCopy
  
	// TODO this should be removed. The PersistenceManagers MUST always be closed! It was for testing reasons,
	// only (because of a JPOX bug).
	public static final boolean CLOSE_PM = true;
	
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
	 * @ejb.permission role-name="AuthorityManager-read"
	 */
	public void ejbCreate() throws CreateException
	{
//		try
//		{
//			System.out.println("UserManagerBean by " + this.getPrincipalString());
//		}
//		catch (Exception e)
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

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="AuthorityManager-write"
//	 **/
//	public void createAuthority(String authorityID, String name, String description)
//		throws SecurityException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				Authority authority = new Authority(getOrganisationID(), authorityID);
//				authority.setName(null, name);
//				authority.setDescription(null, description);
//				pm.makePersistent(authority);
//			} finally {
//				if (CLOSE_PM) pm.close();
//			}
//		} catch (Exception x) {
//			throw new SecurityException(x);
//		}
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 **/
	public Authority getAuthority(String authorityID, String [] fetchGroups)
		throws SecurityException
	{
		try 
		{
			PersistenceManager pm = getPersistenceManager();

			if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);

			try 
			{
				pm.getExtent(Authority.class, true);
				try 
				{
					Object o = pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					return (Authority)pm.detachCopy(o);
				} 
				catch (JDOObjectNotFoundException x) 
				{
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
			} 
			finally 
			{
				if (CLOSE_PM) pm.close();
			}
		} 
		catch (Exception x) 
		{
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 **/
	public AuthoritySearchResult searchAuthorities (
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, String[] fetchGroups, int maxFetchDepth)
		throws SecurityException
	{
		try 
		{
			PersistenceManager pm = getPersistenceManager();
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
      if (fetchGroups != null) 
      	pm.getFetchPlan().setGroups(fetchGroups);
			
      try 
			{
				AuthoritySearchResult result = Authority.searchAuthorities(pm, searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);

				result.detachItems(pm);
				return result;
			} 
			finally 
			{
				if (CLOSE_PM) pm.close();
			}
		} 
		catch (Exception x) 
		{
			throw new SecurityException(x);
		}
	}

	/**
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 **/
	public List getAllAuthorities() 
	throws ModuleException
	{
	  PersistenceManager pm = getPersistenceManager();
	  try 
	  {
	    Query query = pm.newQuery(pm.getExtent(Authority.class, true));
	    Collection c = (Collection)query.execute();
	    List result = new ArrayList(pm.detachCopyAll(c));
	    return result;
	  } 
	  finally 
	  {
	    pm.close();
	  }
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 **/
	public RoleGroupRefSearchResult searchRoleGroupRefs(
			String authorityID,
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" could not be found in organisation \""+getOrganisationID()+"\"!");
				}
				
				RoleGroupRefSearchResult result = authority.searchRoleGroupRefs(
						searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);
				return result;
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 **/
	public UserRefSearchResult searchUserRefs(
			String authorityID,
			String searchStr, boolean exact, int itemsPerPage, int pageIndex, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" could not be found in organisation \""+getOrganisationID()+"\"!");
				}
				
				UserRefSearchResult result = authority.searchUserRefs(
						searchStr, exact, itemsPerPage, pageIndex);
//				result.makeTransient(includeMask);
				return result;
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	// ******************************************************************
	// *** Methods for management of links between UserRefs and RoleRefs 
	// ******************************************************************

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void createUserRef(String authorityID, String userID)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				User user;
				try {
					user = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new UserNotFoundException("User \""+userID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}

				authority.createUserRef(user);
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void destroyUserRef(String authorityID, String userID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
					pm.getExtent(Authority.class, true);
	//				pm.getExtent(User.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
	//				User user;
	//				try {
	//					user = (User)pm.getObjectById(UserID.create(getOrganisationID(), userID), true);
	//				} catch (JDOObjectNotFoundException x) {
	//					throw new UserNotFoundException("User \""+userID+"\" not found in organisation \""+getOrganisationID()+"\"!");
	//				}
	
					authority.destroyUserRef(userID);
					ism.jfireSecurity_flushCache();
				} finally {
					if (CLOSE_PM) pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void destroyRoleGroupRef(String authorityID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
					pm.getExtent(Authority.class, true);
	//				pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
	//				RoleGroup roleGroup;
	//				try {
	//					roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(roleGroupID), true);
	//				} catch (JDOObjectNotFoundException x) {
	//					throw new UserNotFoundException("RoleGroup \""+roleGroupID+"\" not found in organisation \""+getOrganisationID()+"\"!");
	//				}
	
					authority.destroyRoleGroupRef(roleGroupID);
					ism.jfireSecurity_flushCache();
				} finally {
					if (CLOSE_PM) pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public UserRef getUserRef(String authorityID, String userID, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				UserRef userRef = authority.getUserRef(userID);
				if (userRef == null)
					throw new UserRefNotFoundException("UserRef for User \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
				
//				userRef.makeTransient(includeMask);
				
				return userRef;
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void createRoleGroupRef(String authorityID, String roleGroupID)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(RoleGroup.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				RoleGroup roleGroup;
				try {
					roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(roleGroupID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new UserNotFoundException("RoleGroup \""+roleGroupID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}

				authority.createRoleGroupRef(roleGroup);
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public RoleGroupRef getRoleGroupRef(String authorityID, String roleGroupID, int includeMask)
		throws SecurityException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Authority.class, true);
				pm.getExtent(User.class, true);

				Authority authority;
				try {
					authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
				} catch (JDOObjectNotFoundException x) {
					throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
				}
				RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
				if (roleGroupRef == null)
					throw new RoleGroupRefNotFoundException("RoleGroupRef for RoleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
				
//				roleGroupRef.makeTransient(includeMask);
				
				return roleGroupRef;
			} finally {
				if (CLOSE_PM) pm.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @param authorityID 
	 * @param userID
	 * @param roleGroupID
	 * @throws SecurityException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void addRoleGroupRefToUserRef(String authorityID, String userID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
	//				pm.getExtent(Authority.class, true);
					pm.getExtent(User.class, true);
					pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
					
					UserRef userRef = authority.getUserRef(userID);
					if (userRef == null)
						throw new UserRefNotFoundException("UserRef for user \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
					if (roleGroupRef == null)
						throw new RoleGroupRefNotFoundException("RoleGroupRef for roleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					userRef.addRoleGroupRef(roleGroupRef);
					ism.jfireSecurity_flushCache(userID);
				} finally {
					if (CLOSE_PM) pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}

	/**
	 * @param String authorityID
	 * @param userID
	 * @param roleGroupID
	 * @throws SecurityException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="AuthorityManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public void removeRoleGroupRefFromUserRef(String authorityID, String userID, String roleGroupID)
		throws SecurityException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				PersistenceManager pm = getPersistenceManager();
				try {
	//				pm.getExtent(Authority.class, true);
					pm.getExtent(User.class, true);
					pm.getExtent(RoleGroup.class, true);
	
					Authority authority;
					try {
						authority = (Authority)pm.getObjectById(AuthorityID.create(getOrganisationID(), authorityID), true);
					} catch (JDOObjectNotFoundException x) {
						throw new AuthorityNotFoundException("Authority \""+authorityID+"\" not found in organisation \""+getOrganisationID()+"\"!");
					}
					
					UserRef userRef = authority.getUserRef(userID);
					if (userRef == null)
						throw new UserRefNotFoundException("UserRef for user \""+userID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
					
					RoleGroupRef roleGroupRef = authority.getRoleGroupRef(roleGroupID);
					if (roleGroupRef == null)
						throw new UserRefNotFoundException("RoleGroupRef for roleGroup \""+roleGroupID+"\" not found in authority \""+authorityID+"\" in organisation \""+getOrganisationID()+"\"!");
	
					userRef.removeRoleGroupRef(roleGroupRef);
					ism.jfireSecurity_flushCache(userID);
				} finally {
					if (CLOSE_PM) pm.close();
				}
			} finally {
				ism.close();
			}
		} catch (SecurityException x) {
			throw x;
		} catch (Exception x) {
			throw new SecurityException(x);
		}
	}
}
