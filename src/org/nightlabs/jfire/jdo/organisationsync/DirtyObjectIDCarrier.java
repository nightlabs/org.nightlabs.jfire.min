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

package org.nightlabs.jfire.jdo.organisationsync;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author unassigned
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DirtyObjectIDCarrier
implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String context;

	private Set<Object> objectIDs;

	public DirtyObjectIDCarrier(String context)
	{
		this.context = context;
		this.objectIDs = new HashSet<Object>();
	}

	public String getContext()
	{
		return context;
	}
	public Set getObjectIDs()
	{
		return objectIDs;
	}
	public void addObjectID(Object objectID)
	{
		objectIDs.add(objectID);
	}
}
