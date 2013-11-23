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

package org.nightlabs.jfire.base;

import java.security.Principal;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SimplePrincipal implements Principal, java.io.Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;

	public SimplePrincipal(String name)
	{
		this.name = name;
	}

	/**
	 * @param other The object with which to compare
	 * @return true if other implements Principal and name equals other.getName();
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;

		if (!(other instanceof Principal))
			return false;

		String otherName = ((Principal)other).getName();

		if (name == null)
			return otherName == null;
	 
		return name.equals(otherName);
	}

	@Override
	public int hashCode()
	{
		return (name == null ? 0 : name.hashCode());
	}

	@Override
	public String toString()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}
}
