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

package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.config.InitException;
import org.nightlabs.config.Initializable;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.id.RoleGroupID;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleGroupCf implements Serializable, Initializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RoleGroupCf.class);
	
	private String roleGroupID;
//	private String roleGroupName;
//	private String description;
	private List<RoleCf> roles = null;

	public RoleGroupCf() { }
	public RoleGroupCf(String _roleGroupID) // , String _roleGroupName, String _description)
		throws InitException
	{
		if (!ObjectIDUtil.isValidIDString(_roleGroupID))
			throw new IllegalArgumentException("roleGroupID \""+_roleGroupID+"\" is not a valid id!");
		this.roleGroupID = _roleGroupID;
//		this.roleGroupName = _roleGroupName;
//		this.description = _description;
		init();
	}

	/**
	 * @return Returns the roleGroupID.
	 */
	public String getRoleGroupID() {
		return roleGroupID;
	}
	/**
	 * @param roleGroupID The roleGroupID to set.
	 */
	public void setRoleGroupID(String _roleGroupID) {
		if (!ObjectIDUtil.isValidIDString(_roleGroupID))
			throw new IllegalArgumentException("roleGroupID \""+_roleGroupID+"\" is not a valid id!");
		this.roleGroupID = _roleGroupID;
	}
//	/**
//	 * @return Returns the roleGroupName.
//	 */
//	public String getRoleGroupName() {
//		return roleGroupName;
//	}
//	/**
//	 * @param roleGroupName The roleGroupName to set.
//	 */
//	public void setRoleGroupName(String roleGroupName) {
//		this.roleGroupName = roleGroupName;
//	}
//	/**
//	 * @return Returns the description.
//	 */
//	public String getDescription() {
//		return description;
//	}
//	/**
//	 * @param description The description to set.
//	 */
//	public void setDescription(String description) {
//		this.description = description;
//	}
	/**
	 * @return Returns the roles.
	 */
	public List getRoles() {
		return roles;
	}
	/**
	 * @param roles The roles to set.
	 */
	public void setRoles(List<RoleCf> roles) {
		this.roles = roles;
	}

	public void addRoleCf(RoleCf roleCf)
	{
		roles.add(roleCf);
	}

	/**
	 * This method creates a JDO RoleGroup object with the given persistenceManager
	 * in case it does not yet exist. If it already exists, it only adds roles that
	 * do not yet exist, but nothing else is changed.
	 *
	 * @param pm The PersistenceManager in which's datastore the RoleGroup should be
	 * 	created.
	 */
	public RoleGroup createRoleGroup(PersistenceManager pm)
	{
		logger.debug("createRoleGroup(...): before: pm.getExtent(RoleGroup.class, true);");
		// Initialize meta data.
		pm.getExtent(RoleGroup.class, true);

		// Fetch/create RoleGroup instance.
		logger.debug("createRoleGroup(...): before: roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);");
		RoleGroup roleGroup;
		try {
			roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);
			logger.debug("createRoleGroup(...): after (roleGroup exists): roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);");
		} catch (JDOObjectNotFoundException x) {
			logger.debug("createRoleGroup(...): after (roleGroup does NOT exist): roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);");
			roleGroup = new RoleGroup(getRoleGroupID());
			roleGroup.setName(null, getRoleGroupID());
//			roleGroup.setRoleGroupName(getRoleGroupName());
//			roleGroup.setDescription(getDescription());
			logger.debug("createRoleGroup(...): before: pm.makePersistent(roleGroup'"+roleGroup.getRoleGroupID()+"');");
			pm.makePersistent(roleGroup);
			logger.debug("createRoleGroup(...): after: pm.makePersistent(roleGroup'"+roleGroup.getRoleGroupID()+"');");
		}
		
		// Add missing roles.
		logger.debug("createRoleGroup(...): about to create and add roles...");
		for (Iterator it = getRoles().iterator(); it.hasNext(); ) {
			RoleCf roleCf = (RoleCf)it.next();
			Role role = roleCf.createRole(pm);
			// Because we use a Map, the following "if" is not really necessary.
			// ...just in case, the jdo implementation of Map runs into trouble with a duplicate add...
			if (!roleGroup.containsRole(role)) {
				logger.debug("createRoleGroup(...): before: roleGroup.addRole(role'"+role.getRoleID()+"');");
				roleGroup.addRole(role);
				logger.debug("createRoleGroup(...): after: roleGroup.addRole(role'"+role.getRoleID()+"');");
			}
		}
		logger.debug("createRoleGroup(...): creating roles done.");
		
		return roleGroup;
	}

	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	public void init() throws InitException {
		if (roleGroupID == null)
			throw new NullPointerException("roleGroupID must never be null!");
		
		if ("".equals(roleGroupID))
			throw new InitException("roleGroupID must never be empty!");

		if (roles == null)
			roles = new ArrayList<RoleCf>();

		for (Iterator it = roles.iterator(); it.hasNext(); ) {
			RoleCf roleCf = (RoleCf)it.next();
			roleCf.init();
		}
	}
}
