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


/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ModuleDef
	implements Comparable<ModuleDef>
{

	public ModuleDef() { }

	private String resourceURI;
	private String contextPath;
	private String name;
	private String description;

	/**
	 * @return Returns the contextPath.
	 */
	public String getContextPath() {
		return contextPath;
	}
	/**
	 * @param contextPath The contextPath to set.
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the resourceURI.
	 */
	public String getResourceURI() {
		return resourceURI;
	}
	/**
	 * @param resourceURI The resourceURI to set.
	 */
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ModuleDef other) {
//		if (!(o instanceof ModuleDef))
//			return -1;
//		ModuleDef other = (ModuleDef)o;
		return this.getName().compareTo(other.getName());
	}
}
