/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
package org.nightlabs.jfire.classloader.remote;

public interface JFireRCDLDelegateFilter
{
	/**
	 * This method is called by {@link JFireRCDLDelegate#getResources(String, boolean)}.
	 * Because that method (the implementation of {@link org.nightlabs.classloader.ResourceFinder#getResources(String, boolean)})
	 * is called only once for every resource, this class does not need to remember the already-queried resource names.
	 * <p>
	 * The currently only (known by us) implementation of this filter is
	 * the class <code>org.nightlabs.jfire.base.j2ee.RemoteResourceFilterRegistry</code>
	 * in the project <code>org.nightlabs.jfire.base.j2ee</code>.
	 * </p>
	 * <p>
	 * <b>Important API change:</b> Because of deadlocks, we removed some synchronizations. Now this method might be called by
	 * multiple {@link Thread}s concurrently! Additionally, it might happen, that multiple Threads query the same resource
	 * (while the first one is still obtaining it, before putting it into a <code>Map</code> for later short-cuts).
	 * </p>
	 *
	 * @param name The resource that is searched and could be loaded from the server (it does exist there and is published - that
	 *		has been checked before).
	 * @return Return <code>true</code> if you want to allow that the resource is loaded from the server. Return <code>false</code>,
	 *		if you want to filter the resource out (in order to force a local lookup or a failure, if it doesn't exist locally).
	 */
	boolean includeResource(String name);
}
