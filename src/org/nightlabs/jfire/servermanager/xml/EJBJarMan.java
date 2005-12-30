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
 * Created on 25.06.2004
 */
package org.nightlabs.jfire.servermanager.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class EJBJarMan implements Serializable
{

	public static Logger LOGGER = Logger.getLogger(EJBJarMan.class);

	protected String jfireModuleName;
	public String getJFireModuleName() {
		return jfireModuleName;
	}
	
	public EJBJarMan(String _jfireModuleName)
	{
		this.jfireModuleName = _jfireModuleName;
	}

	/**
	 * key: String roleID
	 * value: Role role
	 */
	protected Map roles = new HashMap();
	
	private SAXParseException parseException = null;
	
	public EJBJarMan(String _jfireModuleName, InputStream ejbJarIn)
		throws SAXException, IOException
	{
		this.jfireModuleName = _jfireModuleName;
		InputSource inputSource = new InputSource(ejbJarIn);
		DOMParser parser = new DOMParser();
//		parser.setValidate(false);
//		// list of features: http://xml.apache.org/xerces2-j/features.html
//		// We deactivate validation because lomboz generates non-valid xml resources, if
//		// there are no beans in a jar.
//		// And because we might be offline.
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

		NodeList nl = parser.getDocument().getElementsByTagName("ejb-jar");
		if (nl.getLength() > 0) {
			Element element = (Element)nl.item(0);
			nl = element.getElementsByTagName("assembly-descriptor");
			if (nl.getLength() > 0) {
				element = (Element)nl.item(0);
				NodeList securityRoles = element.getElementsByTagName("security-role");
				for (int i = 0; i < securityRoles.getLength(); ++i) {
					Element securityRole = (Element)securityRoles.item(i);
					String roleID = null;
					String description = null;
					nl = securityRole.getElementsByTagName("role-name");
					if (nl.getLength() > 0) {
						Node n = nl.item(0).getFirstChild();
						if (n instanceof Text)
							roleID = ((Text)n).getData();
					}
					nl = securityRole.getElementsByTagName("description");
					if (nl.getLength() > 0) {
						Node n = nl.item(0).getFirstChild();
						if (n instanceof Text)
							description = ((Text)n).getData();
					}

					roleID = removeLeadingAndTrailingBlanks(roleID);
					if (!roleID.startsWith("_")) { // internal roles like _ServerAdmin_
						description = removeLeadingAndTrailingBlanks(description);
						RoleDef role = new RoleDef(roleID);
						role.setName(null, roleID);
						role.setDescription(null, description); 
						addRole(role);
					}
				}
			} //	if (nl.getLength() > 0) { // assembly-descriptor
		} // if (nl.getLength() > 0) { // ejb-jar
		
	}

	private static String removeLeadingAndTrailingBlanks(String s)
	{
		return s.replaceAll("(^ )*( $)*", "");
	}

	public Collection getRoles()
	{
		return roles.values();
	}

	public RoleDef getRole(String roleID)
	{
		return (RoleDef)roles.get(roleID);
	}

	public void addRole(RoleDef role)
	{
		roles.put(role.getRoleID(), role);
	}

	public RoleDef removeRole(String roleID)
	{
		return (RoleDef)roles.remove(roleID);
	}

}
