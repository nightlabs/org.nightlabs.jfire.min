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

package org.nightlabs.jfire.security;

import java.util.List;

import org.nightlabs.webapp.MultiPageSearchResult;

/**
 * @deprecated should not be used anymore
 * @author marco
 */
@Deprecated
public class UserSearchResult extends MultiPageSearchResult
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param _itemsFound
	 * @param _itemsPerPage
	 * @param _pageIndex
	 * @param _items
	 */
	public UserSearchResult(
			int _itemsFound, int _itemsPerPage,
			int _pageIndex, List<?> _items)
	{
		super(_itemsFound, _itemsPerPage, _pageIndex, _items);
	}

//	public static final int INCLUDE_NONE = User.INCLUDE_NONE;
//	public static final int INCLUDE_PERSON = User.INCLUDE_PERSON;
//	public static final int INCLUDE_ROLEGROUPS = User.INCLUDE_ROLEGROUPS;
//	public static final int INCLUDE_ROLEGROUPS_ROLES = User.INCLUDE_ROLEGROUPS_ROLES;
//	public static final int INCLUDE_ROLEREFS = User.INCLUDE_ROLEREFS;
//	public static final int INCLUDE_ROLEREFS_ROLES = User.INCLUDE_ROLEREFS_ROLES;

//	public void makeTransient(int includeMask)
//	{
//		List transientItems = new ArrayList();
//		for (Iterator it = getItems().iterator(); it.hasNext(); ) {
//			User user = (User) it.next();
//			user.makeTransient(includeMask);
////			user.setPassword(null);
//			transientItems.add(user);
//		} // for (Iterator it = getItems().iterator(); it.hasNext(); ) {
//		setItems(transientItems);
//	}
}
