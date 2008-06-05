package org.nightlabs.jfire.servermanager.xml;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.id.AuthorityTypeID;

public class AuthorityTypeDef implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Set<String> roleGroupIDs = new HashSet<String>();
	private Map<String, RoleGroupDef> roleGroupID2roleGroupDef = null; // created during resolve

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

	private String authorityTypeID;

	public AuthorityTypeDef(String authorityTypeID) {
		this.authorityTypeID = authorityTypeID;
	}

	public String getAuthorityTypeID() {
		return authorityTypeID;
	}

	public void mergeFrom(JFireSecurityMan jfireSecurityMan, AuthorityTypeDef other) {
		for (Map.Entry<String, String> me : other.getNames().entrySet())
			this.setName(me.getKey(), me.getValue());

		for (Map.Entry<String, String> me : other.getDescriptions().entrySet())
			this.setDescription(me.getKey(), me.getValue());

		for (String roleGroupID : other.roleGroupIDs)
			this.roleGroupIDs.add(roleGroupID);
	}

	public void resolve(JFireSecurityMan jfireSecurityMan)
	{
		if (roleGroupID2roleGroupDef == null) {
			Map<String, RoleGroupDef> m = new HashMap<String, RoleGroupDef>();

			for (String roleGroupID : roleGroupIDs) {
				RoleGroupDef roleGroupDef = jfireSecurityMan.getRoleGroup(roleGroupID);
				if (roleGroupDef == null)
					throw new IllegalStateException("authority-type with id=" + authorityTypeID + " references the non-existing role-group with id=" + roleGroupID);

				m.put(roleGroupID, roleGroupDef);
			}

			roleGroupID2roleGroupDef = m;
		}
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
	 * @return a Map with key String languageID and value String name.
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

	public Collection<RoleGroupDef> getRoleGroups()
	{
		if (roleGroupID2roleGroupDef == null)
			throw new IllegalStateException("resolve(...) was not yet called!");

		return roleGroupID2roleGroupDef.values();
	}

	public void addRoleGroup(String roleGroupID)
	{
		if (roleGroupID2roleGroupDef != null)
			throw new IllegalStateException("resolve(...) was already called!");

		roleGroupIDs.add(roleGroupID);
	}

	public AuthorityType updateAuthorityType(PersistenceManager pm, boolean removeRoleGroupsFromAuthorityType)
	{
		AuthorityType authorityType;
		try {
			authorityType = (AuthorityType) pm.getObjectById(AuthorityTypeID.create(authorityTypeID));
		} catch (JDOObjectNotFoundException x) {
			authorityType = new AuthorityType(authorityTypeID);
			authorityType = pm.makePersistent(authorityType);
		}

		for (Map.Entry<String, String> me : names.entrySet()) {
			String languageID = me.getKey();
			if (languageID == null)
				languageID = Locale.ENGLISH.getLanguage();

			authorityType.getName().setText(languageID, me.getValue());
		}

		for (Map.Entry<String, String> me : descriptions.entrySet()) {
			String languageID = me.getKey();
			if (languageID == null)
				languageID = Locale.ENGLISH.getLanguage();

			authorityType.getDescription().setText(languageID, me.getValue());
		}

		// add all role-groups that need to be added
		Set<RoleGroup> currentRoleGroups = removeRoleGroupsFromAuthorityType ? new HashSet<RoleGroup>() : null;
		for (RoleGroupDef roleGroupDef : getRoleGroups()) {
			RoleGroup roleGroup = roleGroupDef.updateRoleGroup(pm);

			if (removeRoleGroupsFromAuthorityType)
				currentRoleGroups.add(roleGroup);

			authorityType.addRoleGroup(roleGroup);
		}

		// and remove those that are in the persistent AuthorityType but not in AuthorityTypeDef anymore
		if (removeRoleGroupsFromAuthorityType) {
			for(RoleGroup roleGroup : new HashSet<RoleGroup>(authorityType.getRoleGroups())) {
				if (currentRoleGroups.contains(roleGroup))
					continue;

				// remove it
				authorityType.removeRoleGroup(roleGroup);
			}
		}

		return authorityType;
	}
}
