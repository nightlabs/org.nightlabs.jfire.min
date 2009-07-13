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
import java.util.ArrayList;
import java.util.List;

import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.id.StructFieldID;


/**
 * Representation of a {@link org.nightlabs.jdo.search.SearchFilterItem} for props.
 * <p>
 * Implements {@link org.nightlabs.jdo.search.SearchFilterItem#getSearchField()} and returns
 * the property structFieldID of this filter item.
 * <p>
 * Subclasses have to return an inheritor of {@link org.nightlabs.jfire.prop.DataField}
 * for {@link org.nightlabs.jdo.search.SearchFilterItem#getItemTargetClass()} in order to
 * can be used by PropSearchFilterItem.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class PropSearchFilterItem extends SearchFilterItem
{
	private static final long serialVersionUID = 1L;

	public static final String QUERY_DATAFIELD_VARNAME = "propField";

	protected List<StructFieldID> personStructFieldIDs = new ArrayList<StructFieldID>();

	protected PropSearchFilterItem(StructFieldID personStructFieldID, int matchType, String needle) {
		super(matchType, needle);
		this.personStructFieldIDs.clear();
		this.personStructFieldIDs.add(personStructFieldID);
	}

	protected PropSearchFilterItem(StructFieldID[] personStructFieldIDs, int matchType, String needle) {
		super(matchType, needle);
		if (personStructFieldIDs.length == 0)
			throw new IllegalArgumentException("At least one PersonStructFieldID has to be defined in the given array.");
		this.personStructFieldIDs.clear();
		for (int i = 0; i < personStructFieldIDs.length; i++) {
			this.personStructFieldIDs.add(personStructFieldIDs[i]);
		}
	}

	protected PropSearchFilterItem(int matchType, String needle) {
		super(matchType, needle);
	}

	public StructFieldID getStructFieldID() {
		return personStructFieldIDs.get(0);
	}

	public List<StructFieldID> getStructFieldIDs() {
		return personStructFieldIDs;
	}

	@Override
	public Object getSearchField() {
		throw new UnsupportedOperationException("Do not use getSearchField for PersonSearchFilterItem. Use getPersonStructFieldIDs instead.");
	}
}
