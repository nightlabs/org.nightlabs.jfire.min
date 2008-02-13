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

package org.nightlabs.jfire.servermanager;

import java.io.Serializable;
import java.util.Map;

import org.nightlabs.jfire.servermanager.xml.EJBRoleGroupMan;


/**
 * @author marco
 */
public class RoleImportSet implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String organisationID;
	private EJBRoleGroupMan ejbRoleGroupMan;
	private Map<String, Throwable> jarExceptions;
	
	public RoleImportSet(String _organisationID, EJBRoleGroupMan _ejbRoleGroupMan, Map<String, Throwable> _jarExceptions)
	{
		this.organisationID = _organisationID;
		this.ejbRoleGroupMan = _ejbRoleGroupMan;
		this.jarExceptions = _jarExceptions;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return Returns the ejbRoleGroupMan.
	 */
	public EJBRoleGroupMan getEjbRoleGroupMan() {
		return ejbRoleGroupMan;
	}
	/**
	 * This method returns the exceptions that happened during reading the ejb-jar.xml and
	 * ejb-rolegroup.xml of every deployed jar file.
	 *
	 * @return Returns a Map with key <code>String jarFileName</code> and value <code>Throwable exception</code>.
	 */
	public Map<String, Throwable> getJarExceptions() {
		return jarExceptions;
	}

	/**
	 * Use this method to clear all exceptions that happened during while reading the
	 * jars.
	 * <br/><br/>
	 * You should call this method before returning this object to the server.
	 */
	public void clearJarExceptions()
	{
		jarExceptions.clear();
	}
}
