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

package org.nightlabs.jfire.servermanager.xml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.id.RoleID;


/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleDef implements Serializable, Comparable<RoleDef>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
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
	private Map<String, String> names = new HashMap<String, String>();
	
	/**
	 * key: String languageID<br/>
	 * value: String description
	 * <br/><br/>
	 * The key <code>null</code> defines the default value.
	 */
	private Map<String, String> descriptions = new HashMap<String, String>();

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
		return names.get(null);
	}
	
	public String getName(String languageID)
	{
		if ("".equals(languageID))
			languageID = null;
		String res = names.get(languageID);
		if (res == null)
			res = names.get(null);
		return res;
	}
	
	public Map<String, String> getNames()
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
		return descriptions.get(null);
	}

	/**
	 * Returns all the descriptions in all the languages.
	 * @return A Map with <code>key: String languageID</code> and <code>value: String description</code>
	 */
	public Map<String, String> getDescriptions()
	{
		return descriptions;
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription(String languageID) {
		if ("".equals(languageID))
			languageID = null;
		String res = descriptions.get(languageID);
		if (res == null)
			res = descriptions.get(null);
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

//		for (Iterator it = getNames().entrySet().iterator(); it.hasNext(); ) {
//			Map.Entry me = (Map.Entry)it.next();
//			String languageID = (String)me.getKey();
//			String name = (String)me.getValue();
//			role.setName(languageID, name);
//		}
		
//		for (Iterator it = getDescriptions().entrySet().iterator(); it.hasNext(); ) {
//			Map.Entry me = (Map.Entry)it.next();
//			String languageID = (String)me.getKey();
//			String description = (String)me.getValue();
//			role.setDescription(languageID, description);
//		}

		return role;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RoleDef other) {
//		if (!(o instanceof RoleGroupDef))
//			return -1;
//
//		RoleGroupDef other = (RoleGroupDef)o;
		String thisName = this.getName();
		if (thisName == null)
			thisName = this.getRoleID();
		String otherName = other.getName();
		if (otherName == null)
			otherName = this.getRoleID();

		return thisName.compareTo(otherName);
	}
}
