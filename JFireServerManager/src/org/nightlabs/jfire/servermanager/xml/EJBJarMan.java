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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.xml.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author marco
 */
public class EJBJarMan implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(EJBJarMan.class);

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
	protected Map<String, RoleDef> roles = new HashMap<String, RoleDef>();
	
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
				logger.error("Parse (ejb-jar.xml): ", exception);
				parseException = exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				logger.fatal("Parse (ejb-jar.xml): ", exception);
				parseException = exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
				logger.warn("Parse (ejb-jar.xml): ", exception);
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

					roleID = roleID.trim();
					if (!roleID.startsWith("_")) { // internal roles like _ServerAdmin_
						description = description.trim();
						RoleDef role = new RoleDef(roleID);
						role.setName(null, roleID);
						role.setDescription(null, description);
						addRole(role);
					}
				}
			} //	if (nl.getLength() > 0) { // assembly-descriptor
		} // if (nl.getLength() > 0) { // ejb-jar
		
	}

//	private static String removeLeadingAndTrailingBlanks(String s)
//	{
//		return s.replaceAll("(^ )*( $)*", "");
//	}

	public Collection<RoleDef> getRoles()
	{
		return roles.values();
	}

	public RoleDef getRole(String roleID)
	{
		return roles.get(roleID);
	}

	public void addRole(RoleDef role)
	{
		roles.put(role.getRoleID(), role);
	}

	public RoleDef removeRole(String roleID)
	{
		return roles.remove(roleID);
	}

}
