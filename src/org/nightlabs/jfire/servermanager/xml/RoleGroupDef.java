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
public class RoleGroupDef implements Serializable, Comparable<RoleGroupDef>
{
	private static final long serialVersionUID = 1L;

	private EJBRoleGroupMan owner;
	
	private String roleGroupID;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 */
	private Map<String, String> names = new HashMap<String, String>();

	/**
	 * key: String languageID<br/>
	 * value: String description
	 */
	private Map<String, String> descriptions = new HashMap<String, String>();

	private boolean defaultGroup;

	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroupDef roleGroup
	 */
	protected Map<String, RoleGroupDef> includedRoleGroups = new HashMap<String, RoleGroupDef>();

	/**
	 * key: String roleID<br/>
	 * value: RoleDef roleDef
	 */
	protected Map<String, RoleDef> roles = new HashMap<String, RoleDef>();

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
	public Map<String, String> getNames()
	{
		return names;
	}
	
	public Map<String, String> getDescriptions()
	{
		return descriptions;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return descriptions.get(null);
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
		RoleGroupDef roleGroupDef = includedRoleGroups.get(roleGroupName);
		if (roleGroupDef == null && includedRoleGroups.containsKey(roleGroupName)) {
			roleGroupDef = owner.getRoleGroup(roleGroupName);
			if (roleGroupDef == null)
				throw new IllegalStateException("RoleGroupDef not found in owner, but roleGroup \""+roleGroupName+"\" is included in \""+roleGroupName+"\"!");
			
			includedRoleGroups.put(roleGroupName, roleGroupDef);
		}
		return roleGroupDef;
	}

	protected RoleDef removeRole(String roleID)
	{
		return roles.remove(roleID);
	}

	protected void addRole(RoleDef roleDef)
	{
		roles.put(roleDef.getRoleID(), roleDef);
	}
	
	public RoleDef getRole(String roleID)
	{
		return roles.get(roleID);
	}

	/**
	 * This method does the same as getAllRoles() except that it does not add the
	 * roles that are indirectly put into this group by inclusion.
	 * @return All the roles of this RoleGroup - no includes.
	 */
	public Collection<RoleDef> getRoles()
	{
		return roles.values();
	}

	/**
	 * If this RoleGroup is the default group, this method will return all
	 * roles that have not been declared in ejb-rolegroup.xml, but in ejb-jar.xml.
	 * Otherwise, it returns an empty Collection.
	 * <br/><br/>
	 * This method is expensive!
	 *
	 * @return A Collection with instances of type RoleDef.
	 */
	public Collection<RoleDef> getDefaultRoles()
	{
		if (!defaultGroup)
			return Collections.EMPTY_LIST;
		
		return owner.getRolesInDefaultGroup();
	}

	/**
	 * This method is expensive.
	 * 
	 * @return Returns all roles as instances of RoleDef including the ones that
	 *   are indirectly included by includedRoleGroups or default roles.
	 */
	public Collection<RoleDef> getAllRoles()
	{
		if (includedRoleGroups.isEmpty() && !defaultGroup)
			return roles.values();

		Collection<RoleDef> res = new HashSet<RoleDef>(roles.values());

		// TODO: Circular reference detection in includes!!!
		for (Iterator<Map.Entry<String, RoleGroupDef>> it = includedRoleGroups.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, RoleGroupDef> me = it.next();
			String roleGroupName = me.getKey();
			RoleGroupDef includedRoleGroup = me.getValue();
			if (includedRoleGroup == null)
				includedRoleGroup = getIncludedRoleGroup(roleGroupName);
			res.addAll(includedRoleGroup.getAllRoles());
		}

		res.addAll(getDefaultRoles());

		return res;
	}

	public List<RoleDef> getAllRolesSorted()
	{
		List<RoleDef> sortedList = new ArrayList<RoleDef>(getAllRoles());
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

		for (Iterator<Map.Entry<String, String>> it = getNames().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, String> me = it.next();
			String languageID = me.getKey();
			String name = me.getValue();
      if(languageID != null)
        roleGroup.getName().setText(languageID, name);
      else
        roleGroup.getName().setText(Locale.ENGLISH.getLanguage(), name);
		}
		
		for (Iterator<Map.Entry<String, String>> it = getDescriptions().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, String> me = it.next();
			String languageID = me.getKey();
			String description = me.getValue();
      if(languageID != null)
        roleGroup.getDescription().setText(languageID, description);
      else
        roleGroup.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		}
		
		// Add missing roles.
		for (Iterator<RoleDef> it = getAllRoles().iterator(); it.hasNext(); ) {
			RoleDef roleDef = it.next();
			Role role = roleDef.createRole(pm);
			if (!roleGroup.containsRole(role)) // probably not necessary - only for avoiding problems, in case the jdo implementation is not clean.
				roleGroup.addRole(role);
		}

		return roleGroup;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RoleGroupDef o) {
//		if (!(o instanceof RoleGroupDef))
//			return -1;
		
		RoleGroupDef other = o;
		String thisName = this.getName();
		if (thisName == null)
			thisName = this.getRoleGroupID();
		String otherName = other.getName();
		if (otherName == null)
			otherName = this.getRoleGroupID();

		return thisName.compareTo(otherName);
	}
	
}
