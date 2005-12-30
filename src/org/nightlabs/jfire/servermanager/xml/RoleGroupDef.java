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
 * Created on 27.06.2004
 */
package org.nightlabs.jfire.servermanager.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.id.RoleGroupID;


/**
 * @author marco
 */
public class RoleGroupDef implements Serializable, Comparable
{
	private EJBRoleGroupMan owner;
	
	private String roleGroupID;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 */
	private Map names = new HashMap();

	/**
	 * key: String languageID<br/>
	 * value: String description
	 */
	private Map descriptions = new HashMap();

	private boolean defaultGroup;

	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroupDef roleGroup
	 */
	protected Map includedRoleGroups = new HashMap();

	/**
	 * key: String roleID<br/>
	 * value: RoleDef roleDef
	 */
	protected Map roles = new HashMap();

	public RoleGroupDef(EJBRoleGroupMan owner) { }

	public RoleGroupDef(EJBRoleGroupMan _owner, String _roleGroupID, boolean _defaultGroup)
	{
		this.owner = _owner;
		this.roleGroupID = _roleGroupID;
		this.defaultGroup = _defaultGroup;
	}

	/**
	 * @return Returns the owner.
	 */
	public EJBRoleGroupMan getOwner() {
		return owner;
	}
	
	/**
	 * @return Returns the defaultGroup.
	 */
	public boolean isDefaultGroup() {
		return defaultGroup;
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
	
	public void setName(String languageID, String name)
	{
		if ("".equals(languageID))
			languageID = null;
		names.put(languageID, name);
	}
	
	/**
	 * Returns a Map with the name for this group in all languages.
	 * @return Returns a Map with key String languageID and value String name.
	 */
	public Map getNames()
	{
		return names;
	}
	
	public Map getDescriptions()
	{
		return descriptions;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return (String)descriptions.get(null);
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
	 * @return Returns the roleGroupID.
	 */
	public String getRoleGroupID() {
		return roleGroupID;
	}
	/**
	 * @param roleGroupID The roleGroupID to set.
	 */
	public void setRoleGroupID(String _roleGroupID) {
		this.roleGroupID = _roleGroupID;
		if (defaultGroup)
			owner.defaultGroupID = _roleGroupID;
	}

	protected void addIncludedRoleGroup(String roleGroupID)
	{
		includedRoleGroups.put(roleGroupID, null);
	}
	
	public RoleGroupDef getIncludedRoleGroup(String roleGroupName)
	{
		RoleGroupDef roleGroupDef = (RoleGroupDef) includedRoleGroups.get(roleGroupName);
		if (roleGroupDef == null && includedRoleGroups.containsKey(roleGroupName)) {
			roleGroupDef = owner.getRoleGroup(roleGroupName);
			if (roleGroupDef == null)
				throw new IllegalStateException("RoleGroupDef not found in owner, but roleGroup \""+roleGroupName+"\" is included in \""+roleGroupName+"\"!");
			
			includedRoleGroups.put(roleGroupName, roleGroupDef);
		}
		return roleGroupDef;
	}
	
	protected void addRole(RoleDef roleDef)
	{
		roles.put(roleDef.getRoleID(), roleDef);
	}
	
	public RoleDef getRole(String roleID)
	{
		return (RoleDef)roles.get(roleID);
	}

	/**
	 * This method does the same as getAllRoles() except that it does not add the 
	 * roles that are indirectly put into this group by inclusion.
	 * @return All the roles of this RoleGroup - no includes.
	 */
	public Collection getRoles()
	{
		return roles.values();
	}

	private static List EMPTYLIST = new ArrayList();
	/**
	 * If this RoleGroup is the default group, this method will return all
	 * roles that have not been declared in ejb-rolegroup.xml, but in ejb-jar.xml.
	 * Otherwise, it returns an empty Collection.
	 * <br/><br/>
	 * This method is expensive!
	 *
	 * @return A Collection with instances of type RoleDef.
	 */
	public Collection getDefaultRoles()
	{
		if (!defaultGroup)
			return EMPTYLIST;
		
		return owner.getRolesInDefaultGroup();
	}

	/**
	 * This method is expensive.
	 * 
	 * @return Returns all roles as instances of RoleDef including the ones that 
	 *   are indirectly included by includedRoleGroups or default roles.
	 */
	public Collection getAllRoles()
	{
		if (includedRoleGroups.isEmpty() && !defaultGroup)
			return roles.values();

		Collection res = new HashSet(roles.values());

		// TODO: Circular reference detection in includes!!!
		for (Iterator it = includedRoleGroups.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry)it.next();
			String roleGroupName = (String) me.getKey();
			RoleGroupDef includedRoleGroup = (RoleGroupDef) me.getValue();
			if (includedRoleGroup == null)
				includedRoleGroup = getIncludedRoleGroup(roleGroupName);
			res.addAll(includedRoleGroup.getAllRoles());
		}

		res.addAll(getDefaultRoles());

		return res;
	}

	public List getAllRolesSorted()
	{
		List sortedList = new ArrayList(getAllRoles());
		Collections.sort(sortedList);
		return sortedList;
	}
	
	/**
	 * This method creates a JDO RoleGroup object with the given persistenceManager
	 * in case it does not yet exist. If it already exists, it adds roles, names
	 * and descriptions that do not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the RoleGroup should be
	 * 	created.
	 */
	public RoleGroup createRoleGroup(PersistenceManager pm)
	{
		// Initialize meta data.
		pm.getExtent(RoleGroup.class, true);

		// Fetch/create RoleGroup instance.
		RoleGroup roleGroup;
		try {
			roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);
		} catch (JDOObjectNotFoundException x) {
			roleGroup = new RoleGroup(getRoleGroupID());
			pm.makePersistent(roleGroup);
		}

		for (Iterator it = getNames().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry)it.next();
			String languageID = (String)me.getKey();
			String name = (String)me.getValue();
      if(languageID != null)
        roleGroup.setName(languageID, name);
      else
        roleGroup.setName(Locale.ENGLISH.getLanguage(), name);
		}
		
		for (Iterator it = getDescriptions().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry me = (Map.Entry)it.next();
			String languageID = (String)me.getKey();
			String description = (String)me.getValue();
      if(languageID != null)
        roleGroup.setDescription(languageID, description);
      else
        roleGroup.setDescription(Locale.ENGLISH.getLanguage(), description);
		}
		
		// Add missing roles.
		for (Iterator it = getAllRoles().iterator(); it.hasNext(); ) {
			RoleDef roleDef = (RoleDef)it.next();
			Role role = roleDef.createRole(pm);
			if (!roleGroup.containsRole(role)) // probably not necessary - only for avoiding problems, in case the jdo implementation is not clean.
				roleGroup.addRole(role);
		}

		return roleGroup;
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
			thisName = this.getRoleGroupID();
		String otherName = other.getName();
		if (otherName == null)
			otherName = this.getRoleGroupID();

		return thisName.compareTo(otherName);
	}
	
}
