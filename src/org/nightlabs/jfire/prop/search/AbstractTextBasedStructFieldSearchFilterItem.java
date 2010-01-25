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

package org.nightlabs.jfire.prop.search;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public abstract class AbstractTextBasedStructFieldSearchFilterItem extends AbstractStructFieldSearchFilterItem
{
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(
			MatchType.EQUALS,
			MatchType.BEGINSWITH,
			MatchType.ENDSWITH,
			MatchType.CONTAINS,
			MatchType.NOTEQUALS,
			MatchType.MATCHES,
			MatchType.NOTCONTAINS);
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20080811L;
	
	private String searchText;

	public AbstractTextBasedStructFieldSearchFilterItem(Collection<StructFieldID> collection, MatchType matchType, String searchText) {
		super(collection, matchType);

		this.searchText = searchText;
	}
	
	protected String getSearchString() {
		return searchText;
	}

	private boolean addedParams = false;

	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter,
			StringBuffer params, Map<String, Object> paramMap) {

		String searchTextLowerCase = "searchText"+itemIndex+".toLowerCase()";
		if (!addedParams) {
			params.append(", ");
			params.append(String.class.getName()+" searchText"+itemIndex);
			paramMap.put("searchText"+itemIndex, getSearchString());
			addedParams = true;
		}

		filter.append(getComparisonLeftHandSide(itemIndex));
		switch (matchType) {
			case BEGINSWITH:
				filter.append(".startsWith("+searchTextLowerCase+")");
				break;
			case ENDSWITH:
				filter.append(".endsWith("+searchTextLowerCase+")");
				break;
			case CONTAINS:
				filter.append(".indexOf("+searchTextLowerCase+") >= 0");
				break;
			case NOTCONTAINS:
				filter.append(".indexOf("+searchTextLowerCase+") < 0");
				break;
			case EQUALS:
				filter.append(" == "+searchTextLowerCase);
				break;
			case NOTEQUALS:
				filter.append(" != "+searchTextLowerCase);
				break;
			case MATCHES:
				params.append(", ");
				params.append(String.class.getName()+" regex"+itemIndex);
				paramMap.put("regex"+itemIndex, getSearchString());
				filter.append(".toLowerCase().matches(regex"+itemIndex+".toLowerCase())");
				break;
			default:
				filter.append(" == "+searchTextLowerCase);
		}
	}

	/**
	 * Extendors may return a different string here to represent the left-hand-side of the
	 * comparison in the query for the given <code>itemSubIndex</code>.
	 * 
	 * @see DisplayNameSearchFilterItem#getComparisonLeftHandSide(int)
	 * @param itemIndex The index of the search filter item in the list.
	 * @return
	 */
	protected String getComparisonLeftHandSide(int itemIndex) {
		return PropSearchFilter.QUERY_DATAFIELD_VARNAME_PREFIX+itemIndex+".text.toLowerCase()";
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}
}
