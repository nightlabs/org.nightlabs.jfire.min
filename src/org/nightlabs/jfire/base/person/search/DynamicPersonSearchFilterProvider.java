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

package org.nightlabs.jfire.base.person.search;

import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jdo.ui.search.SearchFilterItemListMutator;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.prop.search.DynamicPropertySetSearchFilterProvider;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class DynamicPersonSearchFilterProvider extends DynamicPropertySetSearchFilterProvider {

	/**
	 * @param listMutator
	 */
	public DynamicPersonSearchFilterProvider(
			SearchFilterItemListMutator listMutator) {
		super(listMutator);
	}

	/**
	 * @param listMutator
	 * @param resultFetcher
	 * @param login
	 */
	public DynamicPersonSearchFilterProvider(
			SearchFilterItemListMutator listMutator,
			SearchResultFetcher resultFetcher) {
		super(listMutator, resultFetcher);
	}

	
	
	protected SearchFilter createSearchFilter() {
		return new PropSearchFilter();
	}
	
	

}
