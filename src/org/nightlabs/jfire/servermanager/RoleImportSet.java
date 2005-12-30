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
 * Created on 29.06.2004
 */
package org.nightlabs.jfire.servermanager;

import java.io.Serializable;
import java.util.Map;

import org.nightlabs.jfire.servermanager.xml.EJBRoleGroupMan;


/**
 * @author marco
 */
public class RoleImportSet implements Serializable
{
	private String organisationID;
	private EJBRoleGroupMan ejbRoleGroupMan;
	private Map jarExceptions;
	
	public RoleImportSet(String _organisationID, EJBRoleGroupMan _ejbRoleGroupMan, Map _jarExceptions)
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
	public Map getJarExceptions() {
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
