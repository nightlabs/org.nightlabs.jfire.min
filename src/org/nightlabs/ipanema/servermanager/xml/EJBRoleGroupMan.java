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
package org.nightlabs.ipanema.servermanager.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.nightlabs.xml.DOMParser;

/**
 * @author marco
 */
public class EJBRoleGroupMan implements Serializable
{
	public static Logger LOGGER = Logger.getLogger(EJBRoleGroupMan.class);

	protected EJBJarMan ejbJarMan = null;
	
	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroup roleGroup
	 */
	protected Map roleGroups = new HashMap();
	
	/**
	 * key: String roleID<br/>
	 * value: RoleDef role
	 * <br/><br/> 
	 * This Map stores all roles that have
	 * been declared in ejb-rolegroup.xml. This is necessary to diff the roles that
	 * are in the default group or if there is no ejbJarMan (in global usage).
	 * 
	 */
	protected Map roles = new HashMap();

	protected String defaultGroupID = null;
	
	private SAXParseException parseException = null;
	
	public EJBRoleGroupMan()
	{
	}
	
	public EJBRoleGroupMan(EJBJarMan _ejbJarMan)
	throws SAXException, IOException, XMLReadException
	{
		this.ejbJarMan = _ejbJarMan;
	}

	/**
	 * If there is no default role group existent, this method generates one.
	 * This default RoleGroup is generated empty and is only virtually filled.
	 * It will only be generated, if ejbJarMan contains roles.
	 */
	public void createBackupDefaultRoleGroup()
	{
		if (defaultGroupID != null)
			return;
		
		if (ejbJarMan.getRoles().isEmpty())
			return;

//		SimpleDateFormat df = new SimpleDateFormat();
//		df.applyPattern("yyyyMMddHHmmss");
//		String id = df.format(new Date());
		String id = ejbJarMan.getJFireModuleName() + ".fallback";

		RoleGroupDef rgd = new RoleGroupDef(this, id, true);
		rgd.setName(null, "Autogenerated default rolegroup " + id);
		rgd.setDescription(null, "This rolegroup has been autogenerated, because the module "+ejbJarMan.getJFireModuleName()+" does not define a default rolegroup in its ejb-rolegroup.xml.");
		defaultGroupID = id;
		addRoleGroup(rgd);
	}
	
	public EJBRoleGroupMan(EJBJarMan _ejbJarMan, InputStream ejbJarIn)
	throws SAXException, IOException, XMLReadException
	{
		this.ejbJarMan = _ejbJarMan;
		InputSource inputSource = new InputSource(ejbJarIn);
		DOMParser parser = new DOMParser();
//		parser.setValidate(false);
//		// list of features: http://xml.apache.org/xerces2-j/features.html
//		parser.setFeature("http://xml.org/sax/features/validation", false);
		parser.setErrorHandler(new ErrorHandler(){
			public void error(SAXParseException exception) throws SAXException {
				LOGGER.error("Parse (ejb-rolegroup.xml): ", exception);
				parseException = exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				LOGGER.fatal("Parse (ejb-rolegroup.xml): ", exception);
				parseException = exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
				LOGGER.warn("Parse (ejb-rolegroup.xml): ", exception);
			}
		});
		parser.parse(inputSource);
		if (parseException != null)
			throw parseException;

		NodeList nl = parser.getDocument().getElementsByTagName("ejb-rolegroup");
		if (nl.getLength() > 0) {
			Element elEjbRoleGroup = (Element)nl.item(0);
			nl = elEjbRoleGroup.getElementsByTagName("rolegroups");
			if (nl.getLength() > 0) {
				Element elRoleGroups = (Element)nl.item(0);
				this.defaultGroupID = elRoleGroups.getAttribute("defaultgroup");

				NodeList roleGroups = elRoleGroups.getElementsByTagName("rolegroup");
				for (int i = 0; i < roleGroups.getLength(); ++i) {
					Element elRoleGroup = (Element)roleGroups.item(i);
					String roleGroupID = elRoleGroup.getAttribute("id");
					if (roleGroupID == null)
						throw new XMLReadException("Attribute \"rolegroup.id\" missing!");

					RoleGroupDef roleGroupDef = new RoleGroupDef(
							this, roleGroupID,
							defaultGroupID == null ? false : defaultGroupID.equals(roleGroupID));
					
					nl = elRoleGroup.getChildNodes();
					for (int k = 0; k < nl.getLength(); ++k) {
						Node n = nl.item(k);
						if (!"name".equals(n.getNodeName()))
							continue;
						Node ntxt = n.getFirstChild();
						if (ntxt instanceof Text) {
							String languageID = ((Element)n).getAttribute("language");
							String name = ((Text)ntxt).getData();
							roleGroupDef.setName(languageID, name);
						}
					}

					nl = elRoleGroup.getChildNodes();
					for (int k = 0; k < nl.getLength(); ++k) {
						Node n = nl.item(k);
						if (!"description".equals(n.getNodeName()))
							continue;
						Node ntxt = n.getFirstChild();
						if (ntxt instanceof Text) {
							String languageID = ((Element)n).getAttribute("language");
							String description = ((Text)ntxt).getData();
							roleGroupDef.setDescription(languageID, description);
						}
					}

					nl = elRoleGroup.getElementsByTagName("includerolegroup");
					for (int j = 0; j < nl.getLength(); ++j) {
						Element el = (Element)nl.item(j);
						String includeRoleGroupID = el.getAttribute("id");
						roleGroupDef.addIncludedRoleGroup(includeRoleGroupID);
					}

					nl = elRoleGroup.getElementsByTagName("roles");
					if (nl.getLength() > 0) {
						Element elRoles = (Element) nl.item(0);
						NodeList nlRoles = elRoles.getElementsByTagName("role");
						for (int j = 0; j < nlRoles.getLength(); ++j) {
							Element elRole = (Element)nlRoles.item(j);
	
							String roleID = elRole.getAttribute("id");
							if (roleID == null)
								throw new XMLReadException("Attribute \"role.id\" missing!");
							
							RoleDef role = ejbJarMan.getRole(roleID);
							if (role == null) {
								LOGGER.warn("Role \""+roleID+"\" declared in ejb-rolegroup.xml not defined in ejb-jar.xml!");
								role = new RoleDef(roleID);
								ejbJarMan.addRole(role);
							}

							nl = elRole.getElementsByTagName("name");
							for (int k = 0; k < nl.getLength(); ++k) {
								Node n = nl.item(k);
								Node ntxt = n.getFirstChild();
								if (ntxt instanceof Text) {
									String languageID = ((Element)n).getAttribute("language");
									String name = ((Text)ntxt).getData();
									role.setName(languageID, name);
								}
							}

							nl = elRole.getElementsByTagName("description");
							for (int k = 0; k < nl.getLength(); ++k) {
								Node n = nl.item(k);
								Node ntxt = n.getFirstChild();
								if (ntxt instanceof Text) {
									String languageID = ((Element)n).getAttribute("language");
									String description = ((Text)ntxt).getData();
									role.setDescription(languageID, description);
								}
							}

							roles.put(roleID, role);
							roleGroupDef.addRole(role);
						} // for (int j = 0; j < roles.getLength(); ++j) { // role
					} // if (nl.getLength() > 0) { // roles

					addRoleGroup(roleGroupDef);
				} // for (int i = 0; i < roleGroups.getLength(); ++i) { // rolegroup
			} //	if (nl.getLength() > 0) { // rolegroups
		} // if (nl.getLength() > 0) { // ejb-rolegroup
	}

	private static String removeLeadingAndTrailingBlanks(String s)
	{
		return s.replaceAll("(^ )*( $)*", "");
	}
	
	public RoleGroupDef getRoleGroup(String roleGroupID)
	{
		return (RoleGroupDef) roleGroups.get(roleGroupID);
	}

	public Collection getRoleGroups()
	{
		return roleGroups.values();
	}

	public void addRoleGroup(RoleGroupDef roleGroup)
	{
		if (this != roleGroup.getOwner())
			throw new IllegalArgumentException("owner of roleGroup is not this EJBRoleGroupMan!");
		roleGroups.put(roleGroup.getRoleGroupID(), roleGroup);
	}

	public void mergeRoleGroupMan(EJBRoleGroupMan other)
	{
		for (Iterator itRoleGroups = other.roleGroups.values().iterator(); itRoleGroups.hasNext(); ) {
			RoleGroupDef otherRoleGroupDef = (RoleGroupDef)itRoleGroups.next();
			String roleGroupID = otherRoleGroupDef.getRoleGroupID();
			RoleGroupDef thisRoleGroupDef = (RoleGroupDef)roleGroups.get(roleGroupID);
			if (thisRoleGroupDef == null) {
				thisRoleGroupDef = new RoleGroupDef(this, roleGroupID, false);
				addRoleGroup(thisRoleGroupDef);
			}
			for (Iterator itNames = otherRoleGroupDef.getNames().entrySet().iterator(); itNames.hasNext(); ) {
				Map.Entry me = (Map.Entry)itNames.next();
				String languageID = (String)me.getKey();
				String name = (String)me.getValue();
				thisRoleGroupDef.setName(languageID, name);
			}
			for (Iterator itDescriptions = otherRoleGroupDef.getDescriptions().entrySet().iterator(); itDescriptions.hasNext(); ) {
				Map.Entry me = (Map.Entry)itDescriptions.next();
				String languageID = (String)me.getKey();
				String description = (String)me.getValue();
				thisRoleGroupDef.setDescription(languageID, description);
			}
			for (Iterator itRoles = otherRoleGroupDef.getAllRoles().iterator(); itRoles.hasNext(); ) {
				RoleDef otherRoleDef = (RoleDef)itRoles.next();
				String roleID = otherRoleDef.getRoleID();
				RoleDef thisRoleDef = thisRoleGroupDef.getRole(roleID);
				if (thisRoleDef == null) {
					thisRoleDef = getRole(roleID);

					if (thisRoleDef == null) {
						thisRoleDef = new RoleDef(roleID);
						roles.put(roleID, thisRoleDef);
					}
					thisRoleGroupDef.addRole(thisRoleDef);
				}
				for (Iterator itNames = otherRoleDef.getNames().entrySet().iterator(); itNames.hasNext(); ) {
					Map.Entry me = (Map.Entry)itNames.next();
					String languageID = (String)me.getKey();
					String name = (String)me.getValue();
					thisRoleDef.setName(languageID, name);
				}
				for (Iterator itDescriptions = otherRoleDef.getDescriptions().entrySet().iterator(); itDescriptions.hasNext(); ) {
					Map.Entry me = (Map.Entry)itDescriptions.next();
					String languageID = (String)me.getKey();
					String description = (String)me.getValue();
					thisRoleDef.setDescription(languageID, description);
				}
			}
		}
	}
	
	public RoleDef getRole(String roleID)
	{
		return (RoleDef)roles.get(roleID);
	}

	/**
	 * This method diffs all the roles that have been declared in ejb-jar.xml, but not in
	 * ejb-rolegroup.xml. They are put into the defaultGroup if one is existent.
	 * 
	 * @return A Collection with instances of type RoleDef.
	 */
	public Collection getRolesInDefaultGroup()
	{
		Set _roles = new HashSet();

		if (ejbJarMan != null) {
			for (Iterator it = ejbJarMan.getRoles().iterator(); it.hasNext(); ) {
				RoleDef roleDef = (RoleDef)it.next();
				if (!roles.containsKey(roleDef.getRoleID()))
					_roles.add(roleDef);
			}
		}

		return _roles;
	}

}
