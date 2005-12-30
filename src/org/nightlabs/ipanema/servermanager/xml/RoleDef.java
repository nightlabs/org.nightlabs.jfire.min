/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 26.06.2004
 */
package org.nightlabs.ipanema.servermanager.xml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.security.Role;
import org.nightlabs.ipanema.security.id.RoleID;


/**
 * @author marco
 */
public class RoleDef implements Serializable, Comparable
{
	public RoleDef() { }

	public RoleDef(String _roleID)
	{
		this.roleID = _roleID;
	}

	private String roleID;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * <br/><br/>
	 * The key <code>null</code> defines the default value.
	 */
	private Map names = new HashMap();
	
	/**
	 * key: String languageID<br/>
	 * value: String description
	 * <br/><br/>
	 * The key <code>null</code> defines the default value.
	 */
	private Map descriptions = new HashMap();

	/**
	 * @return Returns the roleID.
	 */
	public String getRoleID() {
		return roleID;
	}
	/**
	 * @param roleID The roleID to set.
	 */
	public void setRoleID(String roleName) {
		this.roleID = roleName;
	}

	public String getName()
	{
		return (String)names.get(null);
	}
	
	public String getName(String languageID)
	{
		if ("".equals(languageID))
			languageID = null;
		String res = (String)names.get(languageID);
		if (res == null)
			res = (String)names.get(null);
		return res;
	}
	
	public Map getNames()
	{
		return names;
	}

	public void setName(String languageID, String name)
	{
		if ("".equals(languageID))
			languageID = null;
		names.put(languageID, name);
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return (String)descriptions.get(null);
	}

	/**
	 * Returns all the descriptions in all the languages.
	 * @return A Map with <code>key: String languageID</code> and <code>value: String description</code>
	 */
	public Map getDescriptions()
	{
		return descriptions;
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription(String languageID) {
		if ("".equals(languageID))
			languageID = null;
		String res = (String)descriptions.get(languageID);
		if (res == null)
			res = (String)descriptions.get(null);
		return res;
	}
	
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String languageID, String description) {
		if ("".equals(languageID))
			languageID = null;
		descriptions.put(languageID, description);
	}
	
	/**
	 * This method creates a JDO Role object with the given persistenceManager
	 * in case it does not yet exist. If it already exists, it adds names
	 * and descriptions that do not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the Role should be
	 * 	created.
	 */
	public Role createRole(PersistenceManager pm)
	{
		// Initialize meta data.
		pm.getExtent(Role.class, true);

		// Fetch/create Role instance.
		Role role;
		try {
			role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);
		} catch (JDOObjectNotFoundException x) {
			role = new Role(getRoleID());
			pm.makePersistent(role);
		}

		for (Iterator it = getNames().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry)it.next();
			String languageID = (String)me.getKey();
			String name = (String)me.getValue();
//			role.setName(languageID, name);
		}
		
		for (Iterator it = getDescriptions().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry)it.next();
			String languageID = (String)me.getKey();
			String description = (String)me.getValue();
//			role.setDescription(languageID, description);
		}

		return role;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof RoleGroupDef))
			return -1;
		
		RoleGroupDef other = (RoleGroupDef)o;
		String thisName = this.getName();
		if (thisName == null)
			thisName = this.getRoleID();
		String otherName = other.getName();
		if (otherName == null)
			otherName = this.getRoleID();

		return thisName.compareTo(otherName);
	}
}
