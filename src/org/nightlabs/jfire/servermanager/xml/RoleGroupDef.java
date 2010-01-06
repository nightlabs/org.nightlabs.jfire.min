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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

//	private JFireSecurityMan owner;

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

//	/**
//	* key: String roleGroupID<br/>
//	* value: RoleGroupDef roleGroup
//	*/
//	protected Map<String, RoleGroupDef> includedRoleGroups = new HashMap<String, RoleGroupDef>();
	private Set<String> includedRoleGroupIDs = new HashSet<String>();

	private Map<String, RoleGroupDef> includedRoleGroupID2roleGroupDef = null; // created during resolve

	private Set<String> roleIDs = new HashSet<String>();

	/**
	 * key: String roleID<br/>
	 * value: RoleDef roleDef
	 */
	protected Map<String, RoleDef> roleID2role = null; // created during resolve

	public RoleGroupDef(String _roleGroupID)
	{
		if (_roleGroupID == null)
			throw new IllegalArgumentException("roleGroupID must not be null!");

		this.roleGroupID = _roleGroupID;
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

	protected void addIncludedRoleGroup(String roleGroupID)
	{
		if (includedRoleGroupID2roleGroupDef != null)
			throw new IllegalStateException("resolve(...) was already called!");

//		includedRoleGroups.put(roleGroupID, null);
		includedRoleGroupIDs.add(roleGroupID);
	}

//	public RoleGroupDef getIncludedRoleGroup(JFireSecurityMan jfireSecurityMan, String roleGroupID)
//	{
//	RoleGroupDef roleGroupDef = includedRoleGroups.get(roleGroupID);
//	if (roleGroupDef == null && includedRoleGroups.containsKey(roleGroupID)) {
//	roleGroupDef = jfireSecurityMan.getRoleGroup(roleGroupID);
//	if (roleGroupDef == null)
//	throw new IllegalStateException("RoleGroupDef not found in owner, but roleGroup \""+roleGroupID+"\" is included in \""+roleGroupID+"\"!");

//	includedRoleGroups.put(roleGroupID, roleGroupDef);
//	}
//	return roleGroupDef;
//	}

	protected void addRole(String roleID)
	{
		if (roleID2role != null)
			throw new IllegalStateException("resolve(...) was already called!");

		roleIDs.add(roleID);
	}

//	/**
//	* This method does the same as getAllRoles() except that it does not add the
//	* roles that are indirectly put into this group by inclusion.
//	* @return All the roles of this RoleGroup - no includes.
//	*/
//	public Collection<RoleDef> getRoles()
//	{
//	return roles.values();
//	}

//	/**
//	* If this RoleGroup is the default group, this method will return all
//	* roles that have not been declared in jfire-security.xml, but in ejb-jar.xml.
//	* Otherwise, it returns an empty Collection.
//	* <br/><br/>
//	* This method is expensive!
//	*
//	* @return A Collection with instances of type RoleDef.
//	*/
//	public Collection<RoleDef> getDefaultRoles()
//	{
//	if (!defaultGroup)
//	return Collections.EMPTY_LIST;

//	return owner.getRolesInDefaultGroup();
//	}

	public void resolve(JFireSecurityMan jfireSecurityMan)
	{
		if (includedRoleGroupID2roleGroupDef == null) {
			Map<String, RoleGroupDef> m = new HashMap<String, RoleGroupDef>();
			for (String includedRoleGroupID : includedRoleGroupIDs) {
				RoleGroupDef roleGroupDef = jfireSecurityMan.getRoleGroup(includedRoleGroupID);
				if (roleGroupDef == null)
					throw new IllegalStateException("role-group with id=" + roleGroupID + " declares non-existent included-role-group with id=" + includedRoleGroupID);

				m.put(includedRoleGroupID, roleGroupDef);
			}
			includedRoleGroupID2roleGroupDef = m;
		}

		if (roleID2role == null) {
			Map<String, RoleDef> m = new HashMap<String, RoleDef>();
			for (String roleID : roleIDs) {
				RoleDef roleDef = jfireSecurityMan.getRole(roleID);
				if (roleDef == null)
					throw new IllegalStateException("role-group with id=" + roleGroupID + " declares non-existent role with id=" + roleID);

				m.put(roleID, roleDef);
			}
			roleID2role = m;
		}
	}

	public Set<String> getIncludedRoleGroupIDs() {
		return Collections.unmodifiableSet(includedRoleGroupIDs);
	}
	public Set<String> getRoleIDs() {
		return Collections.unmodifiableSet(roleIDs);
	}

	/**
	 * This method is expensive.
	 *
	 * @return all roles as instances of RoleDef including the ones that
	 *   are indirectly included by includedRoleGroups.
	 */
	public Collection<RoleDef> getAllRoles()
	{
		Set<RoleDef> roles = new HashSet<RoleDef>();
		populateRolesRecursively(roles, new HashSet<String>());
		return roles;
	}

	private void populateRolesRecursively(Set<RoleDef> roles, Set<String> processedRoleGroupIDs)
	{
		if (this.roleGroupID == null)
			throw new IllegalStateException("this.roleGroupID == null");

		processedRoleGroupIDs.add(this.roleGroupID);

		if (includedRoleGroupID2roleGroupDef == null)
			throw new IllegalStateException("resolve(...) was not yet called!");

		roles.addAll(roleID2role.values());

		for (RoleGroupDef includedRoleGroup : includedRoleGroupID2roleGroupDef.values()) {
			if (!processedRoleGroupIDs.contains(includedRoleGroup.getRoleGroupID()))
				includedRoleGroup.populateRolesRecursively(roles, processedRoleGroupIDs);
		}
	}

//	public List<RoleDef> getAllRolesSorted()
//	{
//	List<RoleDef> sortedList = new ArrayList<RoleDef>(getAllRoles());
//	Collections.sort(sortedList);
//	return sortedList;
//	}

	/**
	 * This method creates a JDO RoleGroup object with the given persistenceManager
	 * in case it does not yet exist. If it already exists, it adds roles, names
	 * and descriptions that do not yet exist.
	 *
	 * @param pm The PersistenceManager in which's datastore the RoleGroup should be
	 * 	created.
	 */
	public RoleGroup updateRoleGroup(PersistenceManager pm)
	{
		// Initialize meta data.
		pm.getExtent(RoleGroup.class);

		// Fetch/create RoleGroup instance.
		RoleGroup roleGroup;
		try {
			roleGroup = (RoleGroup)pm.getObjectById(RoleGroupID.create(getRoleGroupID()), true);
		} catch (JDOObjectNotFoundException x) {
			roleGroup = new RoleGroup(getRoleGroupID());
			roleGroup = pm.makePersistent(roleGroup);
		}

		for (Map.Entry<String, String> me : getNames().entrySet()) {
			String languageID = me.getKey();
			if(languageID == null)
				languageID = Locale.ENGLISH.getLanguage();

			roleGroup.getName().setText(languageID, me.getValue());
		}

		for (Map.Entry<String, String> me : getDescriptions().entrySet()) {
			String languageID = me.getKey();
			if(languageID == null)
				languageID = Locale.ENGLISH.getLanguage();

			roleGroup.getDescription().setText(languageID, me.getValue());
		}

		Set<Role> currentRoles = new HashSet<Role>();

		// add roles
		for (RoleDef roleDef : getAllRoles()) {
			Role role = roleDef.createRole(pm);
			currentRoles.add(role);
			roleGroup.addRole(role);
		}

		// remove roles that are no more in the role-group
		for (Role role : new HashSet<Role>(roleGroup.getRoles())) {
			if (currentRoles.contains(role))
				continue;

			roleGroup.removeRole(role);
		}

		return roleGroup;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RoleGroupDef o) {
//		if (!(o instanceof RoleGroupDef))
//		return -1;

		RoleGroupDef other = o;
		String thisName = this.getName();
		if (thisName == null)
			thisName = this.getRoleGroupID();
		String otherName = other.getName();
		if (otherName == null)
			otherName = this.getRoleGroupID();

		return thisName.compareTo(otherName);
	}

	public void mergeFrom(JFireSecurityMan jfireSecurityMan, RoleGroupDef other) {
		for (Map.Entry<String, String> me : other.getNames().entrySet())
			this.setName(me.getKey(), me.getValue());

		for (Map.Entry<String, String> me : other.getDescriptions().entrySet())
			this.setDescription(me.getKey(), me.getValue());

		for (String includedRoleGroupID : other.includedRoleGroupIDs)
			this.includedRoleGroupIDs.add(includedRoleGroupID);

		for (String roleID : other.roleIDs)
			this.roleIDs.add(roleID);
	}
}
