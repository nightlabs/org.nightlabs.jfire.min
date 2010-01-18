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

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DisplayNameSearchFilterItem extends TextStructFieldSearchFilterItem
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public DisplayNameSearchFilterItem(MatchType matchType, String searchText) {
		super((Collection<StructFieldID>) null, matchType, searchText);
	}
	
	@Override
	public Class<? extends DataField> getDataFieldClass() {
		return null;
	}

//	@Override
//	public void appendSubQuery(int itemIndex, int itemSubIndex, Set<Class<?>> imports,
//			StringBuffer vars, StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {
//
//		params.append(", ");
//		String needleLowerCase = "needle"+itemIndex+".toLowerCase()";
//		params.append(String.class.getName()+" needle"+itemIndex);
//		paramMap.put("needle"+itemIndex, getSearchString());
//
//		filter.append(PropSearchFilter.PROPERTY_VARNAME+".displayName.toLowerCase()");
//		switch (matchType) {
//			case MATCHTYPE_BEGINSWITH:
//				filter.append(".startsWith("+needleLowerCase+")");
//				break;
//			case MATCHTYPE_ENDSWITH:
//				filter.append(".endsWith("+needleLowerCase+")");
//				break;
//			case MATCHTYPE_CONTAINS:
//				filter.append(".indexOf("+needleLowerCase+") >= 0");
//				break;
//			case MATCHTYPE_NOTCONTAINS:
//				filter.append(".indexOf("+needleLowerCase+") < 0");
//				break;
//			case MATCHTYPE_EQUALS:
//				filter.append(" == "+needleLowerCase);
//				break;
//			case MATCHTYPE_NOTEQUALS:
//				filter.append(" != "+needleLowerCase);
//				break;
//			default:
//				filter.append(" == "+needleLowerCase);
//		}
//	}
	
	@Override
	protected String getComparisonLeftHandSide(int itemSubIndex) {
		return PropSearchFilter.PROPERTY_VARNAME+".displayName.toLowerCase()";
	}
}
