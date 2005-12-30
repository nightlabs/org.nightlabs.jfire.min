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

import org.nightlabs.ModuleException;

/**
 * This exception is thrown by
 * {@link org.nightlabs.jfire.servermanager.JFireServerManager#createOrganisation(String, String, String, String, boolean)}
 * if the user intends to create the first organisation on a server and passes <tt>isServerAdmin = false</tt>.
 * This would result in a server without any server-administrator.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class NoServerAdminException extends ModuleException
{

	public NoServerAdminException()
	{
		super();
	}

	public NoServerAdminException(String message)
	{
		super(message);
	}

	public NoServerAdminException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoServerAdminException(Throwable cause)
	{
		super(cause);
	}

}
