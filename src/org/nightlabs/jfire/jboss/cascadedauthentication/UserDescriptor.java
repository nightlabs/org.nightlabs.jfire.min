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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.jboss.cascadedauthentication;

import java.io.Serializable;

import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UserDescriptor
	implements Serializable
{
	public static final long serialVersionUID = 1L;

	public static final String CONTEXT_KEY = "JFIRE_USER_DESCRIPTOR";

//	protected static Map<Thread, UserDescriptor> userDescriptors = new HashMap<Thread, UserDescriptor>();
	private static ThreadLocal<UserDescriptor> userDescriptors = new ThreadLocal<UserDescriptor>();

	/**
	 * Clears the current user for current thread.
	 */
	public static synchronized void unsetUserDescriptor()
	{
//		Thread t = Thread.currentThread();
//		userDescriptors.remove(t);
		userDescriptors.remove();
	}
	
	/**
	 * Sets the user for the current thread. You must not forget to call unsetUser()
	 * on the same thread as soon as you're done (best in a finally).
	 *
	 * @see #unsetUser()
	 */
	public static synchronized void setUserDescriptor(UserDescriptor userDescriptor)
	{
//		Thread t = Thread.currentThread();
//		userDescriptors.put(t, userDescriptor);
		userDescriptors.set(userDescriptor);
	}
	
	/**
	 * @return Returns the UserDescriptor that has been associated to
	 * the current thread.
	 */
	protected static synchronized UserDescriptor getUserDescriptor()
	{
//		Thread t = Thread.currentThread();
//		return userDescriptors.get(t);
		return userDescriptors.get();
	}

	public UserDescriptor(String userName, String password)
	{
		this.userName = userName;
		this.password = password;
	}

	public String userName;
	public String password;

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof UserDescriptor)) return false;
		UserDescriptor o = (UserDescriptor) obj;
		return
			Util.equals(o.userName, this.userName) &&
			Util.equals(o.password, this.password);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(userName) ^ + Util.hashCode(password);
	}
}
