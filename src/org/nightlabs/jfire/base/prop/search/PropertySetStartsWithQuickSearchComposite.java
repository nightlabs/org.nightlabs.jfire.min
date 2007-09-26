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

package org.nightlabs.jfire.base.prop.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.person.search.PersonStartsWithQuickSearch;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.util.IOUtil;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetStartsWithQuickSearchComposite extends Composite {

	private List<PropertySetQuickSearch> quickSearches;
	public PropertySetStartsWithQuickSearchComposite(Composite arg0, int arg1) {
		this(arg0, arg1, null);
	}
	
	public PropertySetStartsWithQuickSearchComposite(Composite arg0, int arg1, SearchResultFetcher resultFetcher, boolean showShowAllButton) {
		super(arg0, arg1);
		try {
			Login.getLogin();
			
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);
			quickSearches = new LinkedList<PropertySetQuickSearch>();
			
			PropertySetQuickSearch pswqs;
			
			if (showShowAllButton) {
				pswqs = getShowAllQuickSearch(resultFetcher);
				pswqs.createComposite(this);
				quickSearches.add(pswqs);
			}
			
			for (int i=97; i<=122; i++) {
				String ch;
				ch = new String(new byte[]{(byte)i}, IOUtil.CHARSET_NAME_UTF_8);
				
				pswqs = createQuickSearch(resultFetcher, ch); 				
				pswqs.createComposite(this);
				quickSearches.add(pswqs);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * @param parent
	 * @param style
	 */
	public PropertySetStartsWithQuickSearchComposite(Composite arg0, int arg1, SearchResultFetcher resultFetcher) {
		this(arg0, arg1, resultFetcher, false);
	}
	
	protected PersonStartsWithQuickSearch createQuickSearch(SearchResultFetcher resultFetcher, String start) {
		return new PersonStartsWithQuickSearch(resultFetcher, start);
	}

	public List<PropertySetQuickSearch> getQuickSearches() {
		return quickSearches;
	}
	
	/**
	 * Overwrite this method in subclasses and return a {@link PropertySetQuickSearch} that is suitable
	 * for the {@link PropertySet} for which quick searches should be provided.
	 * @param resultFetcher The {@link SearchResultFetcher} to be used.
	 * @return A {@link PropertySetQuickSearch} that is suitable for the {@link PropertySet} for which quick searches should be provided.
	 */
	protected PropertySetQuickSearch getShowAllQuickSearch(SearchResultFetcher resultFetcher) {
		return new PropertySetShowAllQuickSearch(resultFetcher);
	}
}
