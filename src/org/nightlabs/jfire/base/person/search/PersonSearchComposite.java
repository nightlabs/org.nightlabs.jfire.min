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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.prop.PropertySetSearchComposite;
import org.nightlabs.jfire.base.prop.PropertySetTable;
import org.nightlabs.jfire.base.prop.search.PropertySetSearchFilterItemListMutator;
import org.nightlabs.jfire.person.Person;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonSearchComposite extends PropertySetSearchComposite<Person> {
	/**
	 * See {@link PropertySetSearchComposite#PropertySetSearchComposite(Composite, int, String)} 
	 */
	public PersonSearchComposite(Composite parent, int style,
			String quickSearchText) {
		super(parent, style, quickSearchText);
	}
		
	/**
	 * See {@link PropertySetSearchComposite#PropertySetSearchComposite(Composite, int, String, boolean)} 
	 */
	public PersonSearchComposite(Composite parent, int style,
			String quickSearchText, boolean doIDSearchAndUsePropertySetCache) {
		super(parent, style, quickSearchText, doIDSearchAndUsePropertySetCache);
	}

	/**
	 * {@inheritDoc}
	 */
	protected PropertySetTable<Person> createResultTable(Composite parent) {
		return new PersonResultTable(parent, SWT.NONE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new StaticPersonSearchFilterProvider(resultFetcher, false);
	}
	/**
	 * {@inheritDoc}
	 */
	protected SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new DynamicPersonSearchFilterProvider(new PropertySetSearchFilterItemListMutator(), resultFetcher);
	}
	
}
