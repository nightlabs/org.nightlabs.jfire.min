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

package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;

/**
 * This is a descriptor for a client which desires to be notified about
 * the change of a certain JDO object.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ChangeListenerDescriptor
implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String sessionID;
//	private String userID;
	private Serializable objectID;

	public ChangeListenerDescriptor(
			String sessionID,
//			String userID,
			Object objectID)
	{
		if (sessionID == null)
			throw new NullPointerException("sessionID");

//		if (userID == null)
//			throw new NullPointerException("userID");

		if (objectID == null)
			throw new NullPointerException("objectID");

		this.sessionID = sessionID;
//		this.userID = userID;
		this.objectID = (Serializable)objectID;
	}

	/**
	 * @return Returns the sessionID.
	 */
	public String getSessionID()
	{
		return sessionID;
	}

//	public String getUserID()
//	{
//		return userID;
//	}

	/**
	 * @return Returns the objectID.
	 */
	public Object getObjectID()
	{
		return objectID;
	}

	private int _hashCode = 0;

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (_hashCode == 0)
			_hashCode = sessionID.hashCode() ^ objectID.hashCode();

		return _hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof ChangeListenerDescriptor))
			return false;

		ChangeListenerDescriptor other = (ChangeListenerDescriptor)obj;
		return
				this.sessionID.equals(other.sessionID)
				&&
				this.objectID.equals(other.objectID);
	}
}
